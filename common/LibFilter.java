import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


public class LibFilter extends DefaultHandler implements Runnable{

	String fn,ofn;

	float ipro,ptm;

	PrintWriter pw;

	Pattern mod=null;
	
	
	static TreeMap<Integer,TreeMap<String,ArrayList<Double>>> modmap=new TreeMap<>(); 

	static TreeMap<Integer,TreeMap<String,Boolean>> usage=new TreeMap<>(); 
	
	static TreeMap<String, HashSet<String>> fidx=new TreeMap<>();
	

	public LibFilter(String fn, String ofn, float ipro, float ptm, String mod) {
		this.fn=fn;
		this.ofn=ofn;
		this.ipro=ipro;
		this.ptm=ptm;
		if (mod.length()>0){
		this.mod=Pattern.compile("^PTMProphet_"+"["+mod.replaceAll("[^A-Za-z]","")+"]"+mod.replaceAll("[A-Za-z]",""));
		}
	}

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException { 

 
		String[] arg=new String[] {"lib.ptm.pep.xml","lib.pep.xml","0.9","STY79","0.7"};
		if (args.length==3||args.length==5) {
			arg=args;
		}
		float ipro,ptm;
		String mod="";
		ipro=ptm=-1;
		if(arg.length==3) {
			ipro=Float.valueOf(arg[2]);
		}
		if (arg.length==5) {
			ipro=Float.valueOf(arg[2]);
			ptm=Float.valueOf(arg[4]);
			mod=arg[3];
		}
		LibFilter lib=new LibFilter(arg[0],arg[1],ipro,ptm,mod);
		lib.run();
		
		fidx.keySet().parallelStream().forEach(fn->{
			
			
			HashSet<String> set=fidx.get(fn);
		    	for (int chg=2;chg<=5;++chg){
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(new File(fn));
				

				NodeList queries = doc.getElementsByTagName("spectrum_query");
				

				ArrayList<Node> del=new ArrayList<>();
		 
				for (int i=0;i<queries.getLength();++i) {
					if (queries.item(i).getNodeType()!=Node.ELEMENT_NODE) {
						continue;
					}
					Element query = (Element) queries.item(i);
					int c=Integer.valueOf(query.getAttribute("assumed_charge"));

					String sp=query.getAttribute("spectrum");
					if (c!=chg||!set.contains(sp)) {
						del.add(queries.item(i));
					}					
				}				

				del.forEach(n->{
					Node p = n.getParentNode();
					p.removeChild(n);
				});
				

				DOMSource src = new DOMSource(doc);
				StreamResult sr = new StreamResult(new File(fn).getParentFile().getAbsolutePath()+"/clean-"+chg+"-"+new File(fn).getName());
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer t = tf.newTransformer();
				t.setOutputProperty(OutputKeys.METHOD, "xml");
				t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				t.setOutputProperty(OutputKeys.INDENT, "yes");
				t.transform(src, sr);
				
			}catch(Exception e) {
				e.printStackTrace();
			}
			}
		});
		
	}

	@Override
	public void run() {


		try {
			
			
			XMLReader parser = XMLReaderFactory.createXMLReader();

			parser.setContentHandler(this);

			pw = new PrintWriter(ofn);
			
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			
			parser.parse(new InputSource(new BufferedInputStream(new FileInputStream(this.fn))));
			
			pw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		sb.append("</"+localName+">");
		//sb.append(System.lineSeparator());
		if (!wait) {
			if (sb.length()>0) {
				pw.print(sb.toString());
				sb=new StringBuffer();
			}
		}
		if (localName.equals("spectrum_query")) {
			 wait=false; 
			 if (curr_ipro<ipro||(curr_ptm>=0&&curr_ptm<ptm)) {
				 sb=new StringBuffer();
			 }
			 else {
				 pw.print(sb.toString());
				 if (!prot.contains("DECOY_")) {
					 curr_idx.add(sp);
				 }
				 sb=new StringBuffer();
			 }
			 return;
		}
		if (localName.equals("ptmprophet_result")) {
			mod_begin=false;
			return;
		}
	}

	boolean wait=false;
	boolean mod_begin=false;
	float curr_ipro;
	float curr_ptm;
	String sp;
	String prot;
	HashSet<String> curr_idx=null;
	StringBuffer sb=new StringBuffer();
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		
		if (localName.equals("spectrum_query")) {
			wait=true; 
			mod_begin=false;
			curr_ipro=0;
			curr_ptm=-1;
			if (sb.length()>0) {
				pw.print(sb);
				sb=new StringBuffer();
			}
		} 
		
		sb.append("<"+localName);
		for (int i=0;i<attributes.getLength();++i) {
			sb.append(" "+attributes.getLocalName(i));
			sb.append("=");
			sb.append("\""+attributes.getValue(i)+"\"");				
		}
		sb.append(">");
		sb.append(System.lineSeparator());
		if (localName.equals("interact_summary")) {
			String fn=attributes.getValue("filename");
			if (!fidx.containsKey(fn)) {
				curr_idx=new HashSet<>();
				fidx.put(fn, curr_idx);
			}
			return ;
		}
		if (localName.equals("spectrum_query")) {
			sp=attributes.getValue("spectrum");
			return;
		}
		if (localName.equals("search_hit")&&attributes.getValue("hit_rank").equals("1")) {
			prot=attributes.getValue("protein");
			return ;
		}
		if (localName.equals("interprophet_result")) {
			curr_ipro=Float.valueOf(attributes.getValue("probability"));
			return ;
		}
		if (mod!=null&&localName.equals("ptmprophet_result")&&mod.matcher(attributes.getValue("ptm")).find()) {
			mod_begin=true;
			return ;
		}
		if (mod_begin&&localName.equals("mod_aminoacid_probability")) {
			curr_ptm=Math.max(curr_ptm,Float.valueOf(attributes.getValue("probability")));
			return ;
		}
	}
	

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (wait) {
			sb.append(new String(ch,start,length));
		}
		else {
			pw.print(new String(ch,start,length));
		}
	}
	
}
