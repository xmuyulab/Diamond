import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class PTM2CSV extends DefaultHandler{

	PrintWriter pw=null;
	String fn;
	Pattern ptm_prefix;
	double cut;
	public PTM2CSV(String fn, double cut,String ptm_prefix) { 
		this.fn=fn;
		this.cut=cut;
		if (ptm_prefix==null) {
			this.ptm_prefix=null;
		}
		else {
			this.ptm_prefix=Pattern.compile("^PTMProphet_"+"["+ptm_prefix.replaceAll("[^A-Za-z]","")+"]"+ptm_prefix.replaceAll("[A-Za-z]",""));
		}	
	}

	public static void main(String[] args) throws FileNotFoundException{
		
		String[] arg=new String[] {"db.fasta","ptm.pep.xml","ptm.csv","0.9","STY79"};
 
		
		if (args.length==5||args.length==4) {
			arg=args;
		}
		
		Scanner s=new Scanner(new File(arg[0]));

		HashMap<String,String> db=new HashMap<>();
		String k="",v="";
		while (s.hasNextLine()) {
			String line=s.nextLine().trim();
			if (line.isEmpty()) {
				continue;
			}
			if (line.startsWith(">")) {
				if (!v.isEmpty()) {
					db.put(k,v.replaceAll("I", "L"));
					v="";
				}
				k=line.split(" ")[0].substring(1);
				continue;
			}
			v=v+line;			
		}
		if (!v.isEmpty()) {
			db.put(k,v.replaceAll("I", "L"));
		}
		
		s.close();
		
		PTM2CSV ptm=null;
		if (arg.length==5) {
			ptm=new PTM2CSV(arg[2],Double.valueOf(arg[3]),arg[4]);
		}
		else {
			ptm=new PTM2CSV(arg[2],Double.valueOf(arg[3]),null);
		}
		ptm.db=db;
		
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();

			parser.setContentHandler(ptm);

			parser.parse(new InputSource(new BufferedInputStream(new FileInputStream(arg[1]))));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	static public double proton =1.00794;
	
	HashMap<String,String> db;
	
	int chg;
	double mz;
	int miss;
	String pep;
	String mpep;
	String pro;
	String pros;
	//String ptm_pep;
	String prev,next;
	int pro_cnt;
	double pepp;
	double ip;
	boolean ptm_flag;

	TreeMap<Integer,Double> ptm_score=new TreeMap<>();
	
	
	HashMap<String,ArrayList<String>> map=new HashMap<>();


	@Override
	public void endDocument() throws SAXException {
		for (ArrayList<String> e:map.values()) {
			e.sort(String::compareTo);
			String s=e.get(e.size()-1).split(" ",3)[2];		
			pw.println(s);
		}
		pw.close();
	}

	
	@Override
	public void startDocument() throws SAXException {
		try {
			pw=new PrintWriter(fn);
			pw.print("protein,peptide,mz,charge,ipro_prob,missed_cleavage");
			if (ptm_prefix!=null) {
				pw.print(",ptm_peptide,Amino acid,PTMscore,position");
			}
			pw.println();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("ptmprophet_result")){
			ptm_flag=false;
			return;
		}
		if (localName.equals("spectrum_query")) {
						
			if (pros.contains("DECOY_")||ip<cut||!db.containsKey(pro)) {
				//ptm_pep=""; 
				return;
			}
			String seq=db.get(pro);
			String q=prev+pep+next;
			int p=seq.indexOf(q.replaceAll("[^A-Z]", "").replaceAll("I", "L"));
			if (p<0) {
				return ;
			}
			if (p>0) {
				p+=1;
			}
			if (!map.containsKey(mpep+"\t"+chg)) {
				map.put(mpep+"\t"+chg, new ArrayList<>());
			}
			ArrayList<String> arr=map.get(mpep+"\t"+chg);
/*/
第一列，protein，
第二列，peptide，
第三列，mz, 
第四列，电荷，
第五列，iprobility，
第六列missed_cleavage, 
第七列ptm_peptide,
第八列，修饰的氨基酸，
第九列，修饰氨基酸的PTMprophet打分，
第十列，修饰氨基酸在蛋白的位置
/*/
			StringBuffer sb=new StringBuffer();
			sb.append(pro_cnt+"/"+pros);
			sb.append(",");
			sb.append(prev+"."+mpep+"."+next);
			sb.append(",");
			sb.append(mz);
			sb.append(",");
			sb.append(chg);
			sb.append(",");
			sb.append(ip);
			sb.append(",");
			sb.append(miss);
			double max=-1;
			if (!ptm_score.isEmpty()) {
				sb.append(",");
				for (int i=0;i<pep.length();++i){
					char aa=pep.charAt(i);
					if (ptm_score.containsKey(i+1)){
						sb.append(String.format("%c(%.3f)",aa,ptm_score.get(i+1)));
					}
					else{
						sb.append(aa);
					}
				}
				//sb.append(ptm_pep);
				sb.append(",");
				int T=0;
	
				for (Entry<Integer, Double> e:ptm_score.entrySet()) {
					if (e.getValue()>max) {
						max=e.getValue();
					}
				}
				
				for (Entry<Integer, Double> e:ptm_score.entrySet()) {
					if (e.getValue()<max) {
						continue;
					}
					if (T>0) {
						sb.append(";");
					}
					sb.append(pep.charAt(e.getKey()-1));
					++T;
				}
				sb.append(","); 
				if(max>=0) { 
					sb.append(max);
				} 
				sb.append(",");
				T=0;
				for (Entry<Integer, Double> e:ptm_score.entrySet()) {
					if (e.getValue()<max) {
						continue;
					}
					if (T>0) {
						sb.append(";");
					}
					sb.append(p+e.getKey());
					++T;
				}
			}
			if (max<0) {
				max=1;
			}
			arr.add(String.format("%.7f %.7f %s", ip,max,sb.toString())); 

			//ptm_pep=""; 

			return;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		 
		if (localName.equals("spectrum_query")) {
			chg=Integer.valueOf(attributes.getValue("assumed_charge"));
			mz=Double.valueOf(attributes.getValue("precursor_neutral_mass"))/chg+proton;
			ptm_score.clear();
			ptm_flag=false;
			return;
		}
		if (localName.equals("search_hit")&&attributes.getValue("hit_rank").equals("1")) {
			pep=attributes.getValue("peptide");
			mpep=pep;
			prev=attributes.getValue("peptide_prev_aa");
			next=attributes.getValue("peptide_next_aa");
			pro=attributes.getValue("protein");
			miss=Integer.valueOf(attributes.getValue("num_missed_cleavages"));
			pros=pro;
			pro_cnt=1;
			return;
		}
		if (localName.equals("alternative_protein")) {
			++pro_cnt;
			pros=pros+"/"+attributes.getValue("protein");
			return;
		}
		if (localName.equals("modification_info")) {
			mpep=attributes.getValue("modified_peptide");
		}
		if (localName.equals("peptideprophet_result")) {
			pepp=Double.valueOf(attributes.getValue("probability"));
			return;
		}
		if (localName.equals("interprophet_result")) {
			ip=Double.valueOf(attributes.getValue("probability"));
			return;
		}
		
		if (ptm_prefix!=null&&localName.equals("ptmprophet_result")&&ptm_prefix.matcher(attributes.getValue("ptm")).find()){
			//ptm_pep=attributes.getValue("ptm_peptide");
			ptm_flag=true;
			return;
		}
		if (localName.equals("mod_aminoacid_probability")&&ptm_flag) {
			ptm_score.put(Integer.valueOf(attributes.getValue("position")),Double.valueOf(attributes.getValue("probability")));
			return;
		}
	}

	
	
}
