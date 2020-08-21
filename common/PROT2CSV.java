import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.HashSet;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class PROT2CSV extends DefaultHandler implements Runnable{

	String fn;
	String ofn;
	String decoy;
	PrintWriter pw;
	public PROT2CSV(String fn,String ofn,String decoy)
	{
		this.fn=fn;
		this.ofn=ofn;
		this.decoy=decoy;		
	}
	
	public static void main(String[] args) {
		
		String[] arg=new String[] {"./lib.prot.xml","./prot.csv","DECOY_"};
		if (args.length==3) {
			arg=args;
		}
		PROT2CSV prot=new PROT2CSV(arg[0], arg[1], arg[2]);
		
		prot.run();
		
	}

	@Override
	public void run() {

		try {
			
			
			XMLReader parser = XMLReaderFactory.createXMLReader();

			parser.setContentHandler(this);

			pw = new PrintWriter(ofn);
			
			pw.println("protein,probability,unique peptides,length");
			
			parser.parse(new InputSource(new BufferedInputStream(new FileInputStream(this.fn))));
			
			pw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

 

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("protein")) {
			if (prot.startsWith(decoy)) {
				return;
			}
			pw.println(prot+","+prob+","+peps.size()+","+length);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (localName.equals("protein")) {
			prot=attributes.getValue("protein_name");
			prob=attributes.getValue("probability");
			peps.clear();
			return ;
		}
		if (localName.equals("parameter")&&attributes.getValue("name").equals("prot_length")) {
			length=Integer.valueOf(attributes.getValue("value"));
			return;
		}
		if (localName.equals("peptide")) {
			peps.add(attributes.getValue("peptide_sequence"));
			return;
		}
	}

	String prot="";	
	String prob="";
	HashSet<String> peps=new HashSet<>();	
	int length=0;
}
