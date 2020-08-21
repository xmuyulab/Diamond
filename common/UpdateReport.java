import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

public class UpdateReport {

	
	private static String toPepFilename(String name) {
		String fn = name;

		String[] fnt = fn.split("[/]");
		String t3 = null;

		if (fnt.length > 4) {
			t3 = fnt[0] + "/" + fnt[1] + "/" + fnt[2] + "/" + fnt[3];
		} else {
			t3 = fn;
		}

		fn = t3.replaceAll("[\\\\/:*?\"<>|]", "_");
		if (fn.length() > 240) {
			fn = (fnt[0] + "/" + fnt[1]).replaceAll("[\\\\/:*?\"<>|]",
					"_");
		}

		return fn;
	}


	public static void main(String[] args) throws FileNotFoundException{
		
		// ../draw/ aligned.tsv
		String dir=args[0];
		String fn=args[1];
		String clean="";
		if (args.length>2){
		    clean=args[2];
		}
		
		//{filename} -> {filepath}
		TreeMap<String,String> txtfns=new TreeMap<>();
		for (File f:new File(dir).listFiles()){
			String fname=f.getName();
			if (fname.endsWith(".txt.out")){
				txtfns.put(fname.replace(".txt.out", ""),f.getAbsolutePath().replace(".txt.out",""));
			}			
		}


		Scanner s = new Scanner(new File(fn));

		s.nextLine();
		//{filename}
		TreeSet<String> fns = new TreeSet<>();
		
		
		//{ProteinName} -> "{filename}	{FullPeptideName}@{Charge}	{Intensity}	{ProteinNametoPepFilename}"	
		TreeMap<String, TreeSet<String>> prot = new TreeMap<>();

		while (s.hasNextLine()) {
			String[] line = s.nextLine().split("\t", 14);
			fns.add(line[3]);
			if (!line[11].contains("DECOY_") && !line[11].contains("cont_")) {

				if (!prot.containsKey(line[11])) {
					prot.put(line[11], new TreeSet<>());
				}
				TreeSet<String> st = prot.get(line[11]);

				st.add(new File(line[3]).getName() + "\t" + line[7] + "@" + line[8] + "\t" + line[10]+ "\t" + toPepFilename(line[11]));
			}

		}
		s.close();

		//{filename} -> { {ProteinName} -> {50%top rank intensity} }
		TreeMap<String,TreeMap<String,Double>> txtdata=new TreeMap<>();
		
		for (Entry<String, String> e:txtfns.entrySet()){
			txtdata.put(e.getKey(), new TreeMap<>());
			Scanner st=new Scanner(new FileInputStream(e.getValue()+".txt.out"));
			TreeMap<String, Double> map = txtdata.get(e.getKey());
			while (st.hasNextLine()){
				String l =st.nextLine();
				String pro=null;
				
				if (l.startsWith("#BEG")){
					String[] it = l.split("\t");
					pro=it[1].split("-")[0];
					if (!map.containsKey(pro)){
						map.put(pro, 0.0);
					}
					st.nextLine();
					st.nextLine();
					l=st.nextLine(); 
					if(l.trim().isEmpty()){
						continue;
					}
					Scanner tt=new Scanner(l);
					int ist=0;
					int ied=0;
					while (tt.hasNextDouble()){
						tt.nextDouble();
						++ied;
					}
					tt.close();
					
					double[] data=new double[ied-ist];
					while (true){
						String line=st.nextLine();
						if (line.startsWith("#END")){
							break;
						}
						Scanner ls=new Scanner(line);
						for (int i=0;ls.hasNextDouble();++i){
							data[i]+=ls.nextDouble();
						}
						ls.close();
					}
					Arrays.sort(data);
					map.put(pro, map.get(pro)+data[data.length/2]);
				}				
			}
			
			st.close();
		}
		

		PrintWriter rppw = new PrintWriter(fn + "_report.csv");

		StringBuffer out = new StringBuffer();		

		//{index} -> {filename}
		TreeMap<Integer, String> fnmap = new TreeMap<>();
		
		rppw.print("ProteinName,UniquePeptideCount");
		int n = 0;
		for (String f : fns) {
			rppw.print(",");
			rppw.print(new File(f).getName());			
			fnmap.put(n, new File(f).getName());
			++n;
		}
		rppw.print(",");
		for (String f : fns) {
			rppw.print(",");
			rppw.print(new File(f).getName());	
		}
		rppw.println();
		
//		double min=-1;
		
		TreeSet<String> uniq=new TreeSet<>();

		//{ProteinName} -> "{filename}	{FullPeptideName}@{Charge}	{Intensity}	{ProteinNametoPepFilename}"	
		//TreeMap<String, TreeSet<String>> prot = new TreeMap<>();
		for (Entry<String, TreeSet<String>> e : prot.entrySet()){
			TreeSet<String> set = new TreeSet<>();
			for (String l : e.getValue()) {
				set.add(l.split("\t")[1]);
			}
			if (set.isEmpty()) {
				continue;			
			}
			
			String[] pros = e.getKey().split("[/]");
			if (pros.length==2){
				uniq.add(pros[1]);
			}
			
			out.append(e.getKey() + "," + set.size());
			for (Entry<Integer, String> et : fnmap.entrySet()) {
				out.append(",");
				ArrayList<Double> arr = new ArrayList<>();

				for (String l : e.getValue()) {
					String[] items = l.split("\t");
					if (et.getValue().equals(items[0])) {
						arr.add(Double.parseDouble(items[2]));
					}
				}
				double sum = 0;
				for (double d : arr) {
					sum += d;
				}
				if (arr.size() > 3) {

					arr.sort((a, b) -> {
						return Double.compare(b, a);
					});
					for (int i = 3; i < arr.size(); ++i) {
						sum -= arr.get(i);
					}
				}
				if (sum<1){
					TreeMap<String, Double> mp = txtdata.get(clean+et.getValue());
					String name=toPepFilename(e.getKey());
					
					for (String k:mp.keySet()){
						if (name.startsWith(k)){
							sum=mp.get(k)*5.7;
							break;
						}
					}
					if(sum<1){
						out.append(-0.1);
					}
					else{
						out.append(-sum);
					}
				}
				else{
					out.append(sum);
				}

			}

			out.append(System.lineSeparator());
		}
		
		TreeMap<String,double[]> omap=new TreeMap<>();
		
		s=new Scanner(out.toString());
		while (s.hasNextLine()){
			String[] items = s.nextLine().split(",");

			boolean flag=false;
			String[] pros = items[0].split("[/]");
			if (pros.length>2){
				for (int i=1;i<pros.length;++i){
					if (uniq.contains(pros[i])){
						flag=true;
						break;
					}
				}
			}
			if (flag){
				continue;
			}
			         
			String p=items[0].split("[/]")[1];
			
			if (!omap.containsKey(p)){
				omap.put(p, new double[1+fns.size()]);
			}
			
			double[] v=omap.get(p);
			
			for (int i=0;i<v.length;++i){
				v[i]+=Double.parseDouble(items[i+1]);
			}
		}
		s.close();
		
		double[] sumintn=new double[fns.size()];
		

		for (Entry<String, double[]> e:omap.entrySet()){
			
			for (int i=0;i<fns.size();++i) {

				double v=e.getValue()[i+1];
				if (v<0){
					v=-v;
					if (v<1){
						v=0;
					}
				}
				sumintn[i]+=v;
			}

		}
		
		
		for (Entry<String, double[]> e:omap.entrySet()){
			rppw.print(e.getKey());
			for (int i=0;i<e.getValue().length;++i) {
				double v=e.getValue()[i];
				rppw.print(',');
				if (v<0){
					v=-v;
					if (v<1){
						v=0;
					}
					rppw.print("\""+v+"\"");
				}
				else{
					rppw.print(v);
				}
			}
			rppw.print(",");

			for (int i=1;i<e.getValue().length;++i) {
				double v=e.getValue()[i];
				double r=1;
				if (i>0) {
					r=sumintn[0]/sumintn[i-1];
				}
				rppw.print(',');
				if (v<0){
					v=-v;
					if (v<1){
						v=0;
					}
					rppw.print("\""+v*r+"\"");
				}
				else{
					rppw.print(v*r);
				}
			}
			rppw.println();
		}

		rppw.close();
		
		
	}

	
}

