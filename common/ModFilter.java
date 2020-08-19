import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ModFilter {

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		
		File fppro=new File(args[0]);
		File fmod=new File("./modifications.tsv");
		File fumod=new File("./unimod.xml");
		
		//uid - tpp.flag mass,nl
		TreeMap<Integer,TreeMap<String,ArrayList<Double>>> modmap=new TreeMap<>(); 
 
		Scanner s=new Scanner(fmod);
		
		s.nextLine();
		while (s.hasNextLine()) {
			String line=s.nextLine().trim();
			if (line.isEmpty()) {
				continue;
			}
			String[] items = line.split("\t");
			int mid=Integer.valueOf(items[2]);
			if (modmap.containsKey(mid)) {
				modmap.get(mid).put(items[1],new ArrayList<>());
			}
			else {
				TreeMap<String,ArrayList<Double>> m=new TreeMap<>();
				m.put(items[1],new ArrayList<>()); 
				modmap.put(mid,m);
			}
		}
		
		s.close();
		

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(fumod);

		NodeList mods = doc.getElementsByTagName("umod:mod");
		for (int i=0;i<mods.getLength();++i) {
			if (mods.item(i).getNodeType()!=Node.ELEMENT_NODE) {
				continue;
			}
			Element mod=(Element) mods.item(i);
			int mid=Integer.valueOf(mod.getAttribute("record_id"));
			
			if (!modmap.containsKey(mid)) {
				continue;
			}
			TreeMap<String, ArrayList<Double>> list = modmap.get(mid);
			NodeList dmods = mod.getElementsByTagName("umod:delta");
			for (int j=0;j<dmods.getLength();++j) {
				if (dmods.item(j).getNodeType()==Node.ELEMENT_NODE) {
					Element dmod = (Element)dmods.item(j);
					double dmass=Double.valueOf(dmod.getAttribute("mono_mass"));
					for (ArrayList<Double> e:list.values()) {
						e.add(dmass);
					}
				}
			}
			NodeList modsps = mod.getElementsByTagName("umod:specificity");
			for (int j=0;j<modsps.getLength();++j) {
				if (modsps.item(j).getNodeType()!=Node.ELEMENT_NODE) {
					continue;
				}
				
				Element sp = (Element) modsps.item(j);
				
				for (Entry<String, ArrayList<Double>> et:modmap.get(mid).entrySet()) {
					if (et.getKey().contains(sp.getAttribute("site"))) {

						NodeList nls = sp.getElementsByTagName("umod:NeutralLoss");
						for (int k=0;k<nls.getLength();++k) {
							if (nls.item(k).getNodeType()!=Node.ELEMENT_NODE) {
								continue;
							}
							Element nl=(Element) nls.item(k);
							double nm=Double.valueOf(nl.getAttribute("mono_mass"));
							if (nm<=0.1) {
								continue;
							}
							et.getValue().add(-nm);
						}
					}
				}
				 
			}
		}
		

		doc = builder.parse(fppro);

		NodeList infos = doc.getElementsByTagName("modification_info");
		
		ArrayList<Node> del=new ArrayList<>();
 
		for (int i=0;i<infos.getLength();++i) {
			if (infos.item(i).getNodeType()!=Node.ELEMENT_NODE) {
				continue;
			}
			Element info = (Element) infos.item(i);
			String mpep=info.getAttribute("modified_peptide");
			TreeMap<Integer,TreeSet<String>> use=new TreeMap<>();
			mpep=mpep.replaceAll("\\[", "_").replaceAll("\\]", "_");
			for (Entry<Integer, TreeMap<String, ArrayList<Double>>> em:modmap.entrySet()) {
				for (Entry<String, ArrayList<Double>> e:em.getValue().entrySet()) {
					String mk=e.getKey().replaceAll("\\[", "_").replaceAll("\\]", "_");
					if (mpep.contains(mk)) {
						mpep=mpep.replaceAll(mk, "");
					}					
				}
			}
			if (mpep.contains("_")) {
				del.add(infos.item(i).getParentNode().getParentNode().getParentNode());
			}
		}
 
		del.forEach(n->{
			Node p = n.getParentNode();
			p.removeChild(n);
		});

		fppro.renameTo(new File(fppro.getAbsolutePath()+".orig.xml"));

		DOMSource src = new DOMSource(doc);
		StreamResult sr = new StreamResult(new File(args[0]));
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		t.setOutputProperty(OutputKeys.METHOD, "xml");
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.transform(src, sr);
 
		
		
	}

}
