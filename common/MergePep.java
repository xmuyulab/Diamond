import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

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

public class MergePep {

	public static void main(String[] args)
			throws IOException, SAXException, ParserConfigurationException, TransformerException {

		File fsplit = new File(args[0]);

		File root = fsplit.getParentFile();

		String fnsplit = fsplit.getName();

		String fprefix = fnsplit.substring(0, fnsplit.length() - 6);

		String mzml = fsplit.getAbsolutePath();
		mzml = mzml.substring(0, mzml.length() - 6);

		Pattern pattern = Pattern.compile("_part[0-9]+of[0-9]+[.]mzML[.]");

		TreeMap<String, TreeSet<String>> fns = new TreeMap<>();

		for (File f : root.listFiles()) {
			String fn = f.getName();
			if (!fn.endsWith(".pep.xml")) {
				continue;
			}
			if (!pattern.matcher(fn).find()) {
				continue;
			}
			String[] its = pattern.split(fn);
			if (!its[0].equals(fprefix)) {
				continue;
			}
			String ofn = root.getAbsolutePath() + "/" + its[0] + ".mzML." + its[1];
			TreeSet<String> set;
			if (fns.containsKey(ofn)) {
				set = fns.get(ofn);
			} else {
				set = new TreeSet<>();
				fns.put(ofn, set);
			}
			set.add(f.getAbsolutePath());
		}

		ArrayList<Integer> start = new ArrayList<>();
		ArrayList<Integer> size = new ArrayList<>();

		Scanner s = new Scanner(fsplit);

		while (s.hasNextInt()) {
			size.add(s.nextInt());
			start.add(0);
		}

		s.close();

		for (int i = 0; i < size.size() - 1; ++i) {
			start.set(i + 1, start.get(i) + size.get(i));
		}

		for (Entry<String, TreeSet<String>> e : fns.entrySet()) {

			TreeSet<String> set = e.getValue();
			String first = set.first();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(first));

			Element msrun = null;

			while (true) {
				NodeList elems = doc.getElementsByTagName("msms_pipeline_analysis");
				int sz = elems.getLength();
				for (int i = 0; i < sz; ++i) {
					Node et = elems.item(i);
					if (et.getNodeType() == Node.ELEMENT_NODE) {
						Element em = (Element) et;
						if (em.hasAttribute("summary_xml")) {
							em.setAttribute("summary_xml", e.getKey());
						}
					}
				}
				break;
			}

			while (true) {
				NodeList elems = doc.getElementsByTagName("msms_run_summary");
				int sz = elems.getLength();
				for (int i = 0; i < sz; ++i) {
					Node et = elems.item(i);
					if (et.getNodeType() == Node.ELEMENT_NODE) {
						Element em = (Element) et;
						if (em.hasAttribute("base_name")) {
							msrun = em;
							em.setAttribute("base_name", mzml);
						}
					}
				}
				break;
			}

			while (true) {
				NodeList elems = doc.getElementsByTagName("search_summary");
				int sz = elems.getLength();
				for (int i = 0; i < sz; ++i) {
					Node et = elems.item(i);
					if (et.getNodeType() == Node.ELEMENT_NODE) {
						Element em = (Element) et;
						if (em.hasAttribute("base_name")) {
							em.setAttribute("base_name", mzml);
						}
					}
				}
				break;
			}

			int idx = 0;

			int curr = -1;
			for (String fs : set) {
				++curr;
				if (curr == 0) {

					NodeList nds = msrun.getChildNodes();
					for (int i = 0; i < nds.getLength(); ++i) {
						Node it = nds.item(i);
						if (it.getNodeType() != Node.ELEMENT_NODE) {
							continue;
						}
						if (!it.getNodeName().equals("spectrum_query")) {
							continue;
						}
						Element eit = (Element) it;

						int chg = Integer.valueOf(eit.getAttribute("assumed_charge"));
						int sts = Integer.valueOf(eit.getAttribute("start_scan"));
						int eds = Integer.valueOf(eit.getAttribute("end_scan"));
						eit.setAttribute("spectrum", fprefix + "." + sts + "." + eds + "." + chg);
						
						++idx;
						eit.setAttribute("index", "" + idx);
					}

					File rn = new File(fs);
					rn.renameTo(new File(rn.getAbsolutePath()+".part"));
					continue;
				}

				ArrayList<Node> list = new ArrayList<>();

				Document d = builder.parse(new File(fs));
				Element run = null;
				while (true) {
					NodeList elems = d.getElementsByTagName("msms_run_summary");
					int sz = elems.getLength();
					for (int i = 0; i < sz; ++i) {
						Node et = elems.item(i);
						if (et.getNodeType() == Node.ELEMENT_NODE) {
							Element em = (Element) et;
							if (em.hasAttribute("base_name")) {
								run = em;
							}
						}
					}
					break;
				}
				NodeList all = run.getChildNodes();
				for (int i = 0; i < all.getLength(); ++i) {
					Node it = all.item(i);
					if (it.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					if (!it.getNodeName().equals("spectrum_query")) {
						continue;
					}
					Element t = (Element) it;
					int chg = Integer.valueOf(t.getAttribute("assumed_charge"));
					int sts = Integer.valueOf(t.getAttribute("start_scan")) + start.get(curr);
					int eds = Integer.valueOf(t.getAttribute("end_scan")) + start.get(curr);
					t.setAttribute("spectrum", fprefix + "." + sts + "." + eds + "." + chg);
					t.setAttribute("start_scan", "" + sts);
					t.setAttribute("end_scan", "" + eds);
					++idx;
					t.setAttribute("index", "" + idx);

					Element add = (Element) doc.importNode(t, true);
					list.add(add);
				}

				list.forEach(msrun::appendChild);

				File rn = new File(fs);
				rn.renameTo(new File(rn.getAbsolutePath()+".part"));
			}

			DOMSource src = new DOMSource(doc);
			StreamResult sr = new StreamResult(new File(e.getKey()));
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(src, sr);

		}

	}

}
