
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

//生成画图的数据

class Draw {

	public float[] rt;
	// Entry<String, ArrayList<Double>> e;
	String name;
	ArrayList<Float> mz;
	public ArrayList<float[]> data = new ArrayList<>();
	int pos;

	@SuppressWarnings("unchecked")
	public ArrayList<StringBuffer>[] save() throws FileNotFoundException {

		ArrayList<StringBuffer>[] ret = new ArrayList[] { new ArrayList<>(), new ArrayList<>() };

		String fn = name;

		ArrayList<StringBuffer> infobuf = ret[0];
		ArrayList<StringBuffer> txtbuf = ret[1];

		if (infobuf.isEmpty()) {
			for (int i = 0; i < 5; ++i) {
				infobuf.add(new StringBuffer());
			}
			for (int i = 0; i < 2 + mz.size() + 2; ++i) {
				txtbuf.add(new StringBuffer());
			}
			infobuf.get(0).append("#BEG\t").append(fn).append("\t").append(rt[0]).append("\t").append(rt[1]);
			infobuf.get(infobuf.size() - 1).append("#END");
			txtbuf.get(0).append("#BEG\t").append(fn).append("\t").append(rt[0]).append("\t").append(rt[1]);
			txtbuf.get(txtbuf.size() - 1).append("#END");
			infobuf.trimToSize();
			txtbuf.trimToSize();
		}

		infobuf.get(1).append(data.size()).append("\t");

		if (infobuf.get(2).length() == 0) {
			ArrayList<Float> pmz = mz;
			for (int i = 1; i < pmz.size(); ++i) {
				infobuf.get(2).append(pmz.get(i)).append("\t");
			}
		}

		infobuf.get(3).append("1\t").append(data.size() - 2).append("\t");

		txtbuf.get(1).append("\t");
		txtbuf.get(2).append("\t");

		data.forEach((v) -> {
			txtbuf.get(3).append(v[0]).append("\t");
		});

		for (int i = 4, j = 1; i < txtbuf.size() - 1; ++i, ++j) {
			for (float[] v : data) {
				txtbuf.get(i).append(v[j]).append("\t");
			}
		}
		for (StringBuffer sb : infobuf) {
			sb.trimToSize();
		}
		for (StringBuffer sb : txtbuf) {
			sb.trimToSize();
		}

		data = null;
		return ret;

	}

	public void clear() {
		rt = null;
		data = null;
		mz = null;
	}

}

public class PrepareDraw {

	static public HashMap<String, String> unimods;

	static {
		unimods = new HashMap<>();
		unimods.put("1", "Acetyl");
		unimods.put("2", "Amidated");
		unimods.put("3", "Biotin");
		unimods.put("4", "Carbamidomethyl");
		unimods.put("5", "Carbamyl");
		unimods.put("6", "Carboxymethyl");
		unimods.put("7", "Deamidated");
		unimods.put("8", "ICAT-G");
		unimods.put("9", "ICAT-G_2H(8)");
		unimods.put("10", "Met-_Hse");
		unimods.put("11", "Met-_Hsl");
		unimods.put("12", "ICAT-D_2H(8)");
		unimods.put("13", "ICAT-D");
		unimods.put("17", "NIPCAM");
		unimods.put("20", "PEO-Iodoacetyl-LC-Biotin");
		unimods.put("21", "Phospho");
		unimods.put("23", "Dehydrated");
		unimods.put("24", "Propionamide");
		unimods.put("25", "Pyridylacetyl");
		unimods.put("26", "Pyro-carbamidomethyl");
		unimods.put("27", "Glu-_pyro-Glu");
		unimods.put("28", "Gln-_pyro-Glu");
		unimods.put("29", "SMA");
		unimods.put("30", "Cation_Na");
		unimods.put("31", "Pyridylethyl");
		unimods.put("34", "Methyl");
		unimods.put("35", "Oxidation");
		unimods.put("36", "Dimethyl");
		unimods.put("37", "Trimethyl");
		unimods.put("39", "Methylthio");
		unimods.put("40", "Sulfo");
		unimods.put("41", "Hex");
		unimods.put("42", "Lipoyl");
		unimods.put("43", "HexNAc");
		unimods.put("44", "Farnesyl");
		unimods.put("45", "Myristoyl");
		unimods.put("46", "PyridoxalPhosphate");
		unimods.put("47", "Palmitoyl");
		unimods.put("48", "GeranylGeranyl");
		unimods.put("49", "Phosphopantetheine");
		unimods.put("50", "FAD");
		unimods.put("51", "Tripalmitate");
		unimods.put("52", "Guanidinyl");
		unimods.put("53", "HNE");
		unimods.put("54", "Glucuronyl");
		unimods.put("55", "Glutathione");
		unimods.put("56", "Acetyl_2H(3)");
		unimods.put("58", "Propionyl");
		unimods.put("59", "Propionyl_13C(3)");
		unimods.put("60", "GIST-Quat");
		unimods.put("61", "GIST-Quat_2H(3)");
		unimods.put("62", "GIST-Quat_2H(6)");
		unimods.put("63", "GIST-Quat_2H(9)");
		unimods.put("64", "Succinyl");
		unimods.put("65", "Succinyl_2H(4)");
		unimods.put("66", "Succinyl_13C(4)");
		unimods.put("357", "probiotinhydrazide");
		unimods.put("359", "Pro-_pyro-Glu");
		unimods.put("348", "His-_Asn");
		unimods.put("349", "His-_Asp");
		unimods.put("350", "Trp-_Hydroxykynurenin");
		unimods.put("256", "Delta_H(4)C(3)");
		unimods.put("255", "Delta_H(4)C(2)");
		unimods.put("368", "Cys-_Dha");
		unimods.put("344", "Arg-_GluSA");
		unimods.put("345", "Trioxidation");
		unimods.put("89", "Iminobiotin");
		unimods.put("90", "ESP");
		unimods.put("91", "ESP_2H(10)");
		unimods.put("92", "NHS-LC-Biotin");
		unimods.put("93", "EDT-maleimide-PEO-biotin");
		unimods.put("94", "IMID");
		unimods.put("95", "IMID_2H(4)");
		unimods.put("353", "Lysbiotinhydrazide");
		unimods.put("97", "Propionamide_2H(3)");
		unimods.put("354", "Nitro");
		unimods.put("105", "ICAT-C");
		unimods.put("254", "Delta_H(2)C(2)");
		unimods.put("351", "Trp-_Kynurenin");
		unimods.put("352", "Lys-_Allysine");
		unimods.put("106", "ICAT-C_13C(9)");
		unimods.put("107", "FormylMet");
		unimods.put("108", "Nethylmaleimide");
		unimods.put("112", "OxLysBiotinRed");
		unimods.put("119", "IBTP");
		unimods.put("113", "OxLysBiotin");
		unimods.put("114", "OxProBiotinRed");
		unimods.put("115", "OxProBiotin");
		unimods.put("116", "OxArgBiotin");
		unimods.put("117", "OxArgBiotinRed");
		unimods.put("118", "EDT-iodoacetyl-PEO-biotin");
		unimods.put("121", "GG");
		unimods.put("122", "Formyl");
		unimods.put("123", "ICAT-H");
		unimods.put("124", "ICAT-H_13C(6)");
		unimods.put("530", "Cation_K");
		unimods.put("126", "Thioacyl");
		unimods.put("127", "Fluoro");
		unimods.put("128", "Fluorescein");
		unimods.put("129", "Iodo");
		unimods.put("130", "Diiodo");
		unimods.put("131", "Triiodo");
		unimods.put("134", "Myristoleyl");
		unimods.put("360", "Pro-_Pyrrolidinone");
		unimods.put("135", "Myristoyl+Delta_H(-4)");
		unimods.put("136", "Benzoyl");
		unimods.put("137", "Hex(5)HexNAc(2)");
		unimods.put("139", "Dansyl");
		unimods.put("140", "a-type-ion");
		unimods.put("141", "Amidine");
		unimods.put("142", "HexNAc(1)dHex(1)");
		unimods.put("143", "HexNAc(2)");
		unimods.put("144", "Hex(3)");
		unimods.put("145", "HexNAc(1)dHex(2)");
		unimods.put("146", "Hex(1)HexNAc(1)dHex(1)");
		unimods.put("147", "HexNAc(2)dHex(1)");
		unimods.put("148", "Hex(1)HexNAc(2)");
		unimods.put("149", "Hex(1)HexNAc(1)NeuAc(1)");
		unimods.put("150", "HexNAc(2)dHex(2)");
		unimods.put("151", "Hex(1)HexNAc(2)Pent(1)");
		unimods.put("152", "Hex(1)HexNAc(2)dHex(1)");
		unimods.put("153", "Hex(2)HexNAc(2)");
		unimods.put("154", "Hex(3)HexNAc(1)Pent(1)");
		unimods.put("155", "Hex(1)HexNAc(2)dHex(1)Pent(1)");
		unimods.put("156", "Hex(1)HexNAc(2)dHex(2)");
		unimods.put("157", "Hex(2)HexNAc(2)Pent(1)");
		unimods.put("158", "Hex(2)HexNAc(2)dHex(1)");
		unimods.put("159", "Hex(3)HexNAc(2)");
		unimods.put("160", "Hex(1)HexNAc(1)NeuAc(2)");
		unimods.put("161", "Hex(3)HexNAc(2)P(1)");
		unimods.put("162", "Delta_S(-1)Se(1)");
		unimods.put("171", "NBS_13C(6)");
		unimods.put("329", "Methyl_2H(3)13C(1)");
		unimods.put("330", "Dimethyl_2H(6)13C(2)");
		unimods.put("172", "NBS");
		unimods.put("170", "Delta_H(1)N(-1)18O(1)");
		unimods.put("195", "QAT");
		unimods.put("176", "BHT");
		unimods.put("327", "Delta_H(4)C(2)O(-1)S(1)");
		unimods.put("178", "DAET");
		unimods.put("369", "Pro-_Pyrrolidone");
		unimods.put("184", "Label_13C(9)");
		unimods.put("185", "Label_13C(9)+Phospho");
		unimods.put("188", "Label_13C(6)");
		unimods.put("186", "HPG");
		unimods.put("187", "2HPG");
		unimods.put("196", "QAT_2H(3)");
		unimods.put("193", "Label_18O(2)");
		unimods.put("194", "AccQTag");
		unimods.put("199", "Dimethyl_2H(4)");
		unimods.put("197", "EQAT");
		unimods.put("198", "EQAT_2H(5)");
		unimods.put("200", "Ethanedithiol");
		unimods.put("212", "NEIAA_2H(5)");
		unimods.put("205", "Delta_H(6)C(6)O(1)");
		unimods.put("206", "Delta_H(4)C(3)O(1)");
		unimods.put("207", "Delta_H(2)C(3)");
		unimods.put("208", "Delta_H(4)C(6)");
		unimods.put("209", "Delta_H(8)C(6)O(2)");
		unimods.put("213", "ADP-Ribosyl");
		unimods.put("211", "NEIAA");
		unimods.put("214", "iTRAQ4plex");
		unimods.put("253", "Crotonaldehyde");
		unimods.put("340", "Bromo");
		unimods.put("342", "Amino");
		unimods.put("343", "Argbiotinhydrazide");
		unimods.put("258", "Label_18O(1)");
		unimods.put("259", "Label_13C(6)15N(2)");
		unimods.put("260", "Thiophospho");
		unimods.put("261", "SPITC");
		unimods.put("243", "IGBP");
		unimods.put("270", "Cytopiloyne");
		unimods.put("271", "Cytopiloyne+water");
		unimods.put("267", "Label_13C(6)15N(4)");
		unimods.put("269", "Label_13C(9)15N(1)");
		unimods.put("262", "Label_2H(3)");
		unimods.put("268", "Label_13C(5)15N(1)");
		unimods.put("264", "PET");
		unimods.put("272", "CAF");
		unimods.put("273", "Xlink_SSD");
		unimods.put("275", "Nitrosyl");
		unimods.put("276", "AEBS");
		unimods.put("278", "Ethanolyl");
		unimods.put("987", "Label_13C(6)15N(2)+Dimethyl");
		unimods.put("371", "HMVK");
		unimods.put("280", "Ethyl");
		unimods.put("281", "CoenzymeA");
		unimods.put("528", "Methyl+Deamidated");
		unimods.put("529", "Delta_H(5)C(2)");
		unimods.put("284", "Methyl_2H(2)");
		unimods.put("285", "SulfanilicAcid");
		unimods.put("286", "SulfanilicAcid_13C(6)");
		unimods.put("289", "Biotin-PEO-Amine");
		unimods.put("288", "Trp-_Oxolactone");
		unimods.put("290", "Biotin-HPDP");
		unimods.put("291", "Delta_Hg(1)");
		unimods.put("292", "IodoU-AMP");
		unimods.put("293", "CAMthiopropanoyl");
		unimods.put("294", "IED-Biotin");
		unimods.put("295", "dHex");
		unimods.put("298", "Methyl_2H(3)");
		unimods.put("299", "Carboxy");
		unimods.put("301", "Bromobimane");
		unimods.put("302", "Menadione");
		unimods.put("303", "DeStreak");
		unimods.put("305", "dHex(1)Hex(3)HexNAc(4)");
		unimods.put("307", "dHex(1)Hex(4)HexNAc(4)");
		unimods.put("308", "dHex(1)Hex(5)HexNAc(4)");
		unimods.put("309", "Hex(3)HexNAc(4)");
		unimods.put("310", "Hex(4)HexNAc(4)");
		unimods.put("311", "Hex(5)HexNAc(4)");
		unimods.put("312", "Cysteinyl");
		unimods.put("313", "Lys-loss");
		unimods.put("314", "Nmethylmaleimide");
		unimods.put("494", "CyDye-Cy3");
		unimods.put("316", "DimethylpyrroleAdduct");
		unimods.put("318", "Delta_H(2)C(5)");
		unimods.put("319", "Delta_H(2)C(3)O(1)");
		unimods.put("320", "Nethylmaleimide+water");
		unimods.put("768", "Methyl+Acetyl_2H(3)");
		unimods.put("323", "Xlink_B10621");
		unimods.put("324", "DTBP");
		unimods.put("325", "FP-Biotin");
		unimods.put("332", "Thiophos-S-S-biotin");
		unimods.put("333", "Can-FP-biotin");
		unimods.put("335", "HNE+Delta_H(2)");
		unimods.put("361", "Thrbiotinhydrazide");
		unimods.put("337", "Methylamine");
		unimods.put("362", "Diisopropylphosphate");
		unimods.put("363", "Isopropylphospho");
		unimods.put("364", "ICPL_13C(6)");
		unimods.put("893", "CarbamidomethylDTT");
		unimods.put("365", "ICPL");
		unimods.put("366", "Deamidated_18O(1)");
		unimods.put("372", "Arg-_Orn");
		unimods.put("531", "Cation_Cu[I]");
		unimods.put("374", "Dehydro");
		unimods.put("375", "Diphthamide");
		unimods.put("376", "Hydroxyfarnesyl");
		unimods.put("377", "Diacylglycerol");
		unimods.put("378", "Carboxyethyl");
		unimods.put("379", "Hypusine");
		unimods.put("380", "Retinylidene");
		unimods.put("381", "Lys-_AminoadipicAcid");
		unimods.put("382", "Cys-_PyruvicAcid");
		unimods.put("385", "Ammonia-loss");
		unimods.put("387", "Phycocyanobilin");
		unimods.put("388", "Phycoerythrobilin");
		unimods.put("389", "Phytochromobilin");
		unimods.put("390", "Heme");
		unimods.put("391", "Molybdopterin");
		unimods.put("392", "Quinone");
		unimods.put("393", "Glucosylgalactosyl");
		unimods.put("394", "GPIanchor");
		unimods.put("395", "PhosphoribosyldephosphoCoA");
		unimods.put("396", "GlycerylPE");
		unimods.put("397", "Triiodothyronine");
		unimods.put("398", "Thyroxine");
		unimods.put("400", "Tyr-_Dha");
		unimods.put("401", "Didehydro");
		unimods.put("402", "Cys-_Oxoalanine");
		unimods.put("403", "Ser-_LacticAcid");
		unimods.put("451", "GluGlu");
		unimods.put("405", "Phosphoadenosine");
		unimods.put("450", "Glu");
		unimods.put("407", "Hydroxycinnamyl");
		unimods.put("408", "Glycosyl");
		unimods.put("409", "FMNH");
		unimods.put("410", "Archaeol");
		unimods.put("411", "Phenylisocyanate");
		unimods.put("412", "Phenylisocyanate_2H(5)");
		unimods.put("413", "Phosphoguanosine");
		unimods.put("414", "Hydroxymethyl");
		unimods.put("415", "MolybdopterinGD+Delta_S(-1)Se(1)");
		unimods.put("416", "Dipyrrolylmethanemethyl");
		unimods.put("417", "PhosphoUridine");
		unimods.put("419", "Glycerophospho");
		unimods.put("420", "Carboxy-_Thiocarboxy");
		unimods.put("421", "Sulfide");
		unimods.put("422", "PyruvicAcidIminyl");
		unimods.put("423", "Delta_Se(1)");
		unimods.put("424", "MolybdopterinGD");
		unimods.put("425", "Dioxidation");
		unimods.put("426", "Octanoyl");
		unimods.put("428", "PhosphoHexNAc");
		unimods.put("429", "PhosphoHex");
		unimods.put("431", "Palmitoleyl");
		unimods.put("432", "Cholesterol");
		unimods.put("433", "Didehydroretinylidene");
		unimods.put("434", "CHDH");
		unimods.put("435", "Methylpyrroline");
		unimods.put("436", "Hydroxyheme");
		unimods.put("437", "MicrocinC7");
		unimods.put("438", "Cyano");
		unimods.put("439", "Diironsubcluster");
		unimods.put("440", "Amidino");
		unimods.put("442", "FMN");
		unimods.put("443", "FMNC");
		unimods.put("444", "CuSMo");
		unimods.put("445", "Hydroxytrimethyl");
		unimods.put("447", "Deoxy");
		unimods.put("448", "Microcin");
		unimods.put("449", "Decanoyl");
		unimods.put("452", "GluGluGlu");
		unimods.put("453", "GluGluGluGlu");
		unimods.put("454", "HexN");
		unimods.put("455", "Xlink_DMP-s");
		unimods.put("456", "Xlink_DMP");
		unimods.put("457", "NDA");
		unimods.put("464", "SPITC_13C(6)");
		unimods.put("477", "TMAB_2H(9)");
		unimods.put("476", "TMAB");
		unimods.put("478", "FTC");
		unimods.put("472", "AEC-MAEC");
		unimods.put("493", "BADGE");
		unimods.put("481", "Label_2H(4)");
		unimods.put("490", "Hep");
		unimods.put("495", "CyDye-Cy5");
		unimods.put("488", "DHP");
		unimods.put("498", "BHTOH");
		unimods.put("499", "IGBP_13C(2)");
		unimods.put("500", "Nmethylmaleimide+water");
		unimods.put("501", "PyMIC");
		unimods.put("503", "LG-lactam-K");
		unimods.put("519", "BisANS");
		unimods.put("520", "Piperidine");
		unimods.put("518", "Diethyl");
		unimods.put("504", "LG-Hlactam-K");
		unimods.put("510", "Dimethyl_2H(4)13C(2)");
		unimods.put("513", "C8-QAT");
		unimods.put("512", "Hex(2)");
		unimods.put("505", "LG-lactam-R");
		unimods.put("1036", "Withaferin");
		unimods.put("1037", "Biotin_Thermo-88317");
		unimods.put("525", "CLIP_TRAQ_2");
		unimods.put("506", "LG-Hlactam-R");
		unimods.put("522", "Maleimide-PEO2-Biotin");
		unimods.put("523", "Sulfo-NHS-LC-LC-Biotin");
		unimods.put("515", "FNEM");
		unimods.put("514", "PropylNAGthiazoline");
		unimods.put("526", "Dethiomethyl");
		unimods.put("532", "iTRAQ4plex114");
		unimods.put("533", "iTRAQ4plex115");
		unimods.put("534", "Dibromo");
		unimods.put("535", "LRGG");
		unimods.put("536", "CLIP_TRAQ_3");
		unimods.put("537", "CLIP_TRAQ_4");
		unimods.put("538", "Biotin_Cayman-10141");
		unimods.put("539", "Biotin_Cayman-10013");
		unimods.put("540", "Ala-_Ser");
		unimods.put("541", "Ala-_Thr");
		unimods.put("542", "Ala-_Asp");
		unimods.put("543", "Ala-_Pro");
		unimods.put("544", "Ala-_Gly");
		unimods.put("545", "Ala-_Glu");
		unimods.put("546", "Ala-_Val");
		unimods.put("547", "Cys-_Phe");
		unimods.put("548", "Cys-_Ser");
		unimods.put("549", "Cys-_Trp");
		unimods.put("550", "Cys-_Tyr");
		unimods.put("551", "Cys-_Arg");
		unimods.put("552", "Cys-_Gly");
		unimods.put("553", "Asp-_Ala");
		unimods.put("554", "Asp-_His");
		unimods.put("555", "Asp-_Asn");
		unimods.put("556", "Asp-_Gly");
		unimods.put("557", "Asp-_Tyr");
		unimods.put("558", "Asp-_Glu");
		unimods.put("559", "Asp-_Val");
		unimods.put("560", "Glu-_Ala");
		unimods.put("561", "Glu-_Gln");
		unimods.put("562", "Glu-_Asp");
		unimods.put("563", "Glu-_Lys");
		unimods.put("564", "Glu-_Gly");
		unimods.put("565", "Glu-_Val");
		unimods.put("566", "Phe-_Ser");
		unimods.put("567", "Phe-_Cys");
		unimods.put("568", "Phe-_Xle");
		unimods.put("569", "Phe-_Tyr");
		unimods.put("570", "Phe-_Val");
		unimods.put("571", "Gly-_Ala");
		unimods.put("572", "Gly-_Ser");
		unimods.put("573", "Gly-_Trp");
		unimods.put("574", "Gly-_Glu");
		unimods.put("575", "Gly-_Val");
		unimods.put("576", "Gly-_Asp");
		unimods.put("577", "Gly-_Cys");
		unimods.put("578", "Gly-_Arg");
		unimods.put("698", "dNIC");
		unimods.put("580", "His-_Pro");
		unimods.put("581", "His-_Tyr");
		unimods.put("582", "His-_Gln");
		unimods.put("697", "NIC");
		unimods.put("584", "His-_Arg");
		unimods.put("585", "His-_Xle");
		unimods.put("1125", "Xle-_Ala");
		unimods.put("588", "Xle-_Thr");
		unimods.put("589", "Xle-_Asn");
		unimods.put("590", "Xle-_Lys");
		unimods.put("594", "Lys-_Thr");
		unimods.put("595", "Lys-_Asn");
		unimods.put("596", "Lys-_Glu");
		unimods.put("597", "Lys-_Gln");
		unimods.put("598", "Lys-_Met");
		unimods.put("599", "Lys-_Arg");
		unimods.put("600", "Lys-_Xle");
		unimods.put("601", "Xle-_Ser");
		unimods.put("602", "Xle-_Phe");
		unimods.put("603", "Xle-_Trp");
		unimods.put("604", "Xle-_Pro");
		unimods.put("605", "Xle-_Val");
		unimods.put("606", "Xle-_His");
		unimods.put("607", "Xle-_Gln");
		unimods.put("608", "Xle-_Met");
		unimods.put("609", "Xle-_Arg");
		unimods.put("610", "Met-_Thr");
		unimods.put("611", "Met-_Arg");
		unimods.put("613", "Met-_Lys");
		unimods.put("614", "Met-_Xle");
		unimods.put("615", "Met-_Val");
		unimods.put("616", "Asn-_Ser");
		unimods.put("617", "Asn-_Thr");
		unimods.put("618", "Asn-_Lys");
		unimods.put("619", "Asn-_Tyr");
		unimods.put("620", "Asn-_His");
		unimods.put("621", "Asn-_Asp");
		unimods.put("622", "Asn-_Xle");
		unimods.put("623", "Pro-_Ser");
		unimods.put("624", "Pro-_Ala");
		unimods.put("625", "Pro-_His");
		unimods.put("626", "Pro-_Gln");
		unimods.put("627", "Pro-_Thr");
		unimods.put("628", "Pro-_Arg");
		unimods.put("629", "Pro-_Xle");
		unimods.put("630", "Gln-_Pro");
		unimods.put("631", "Gln-_Lys");
		unimods.put("632", "Gln-_Glu");
		unimods.put("633", "Gln-_His");
		unimods.put("634", "Gln-_Arg");
		unimods.put("635", "Gln-_Xle");
		unimods.put("636", "Arg-_Ser");
		unimods.put("637", "Arg-_Trp");
		unimods.put("638", "Arg-_Thr");
		unimods.put("639", "Arg-_Pro");
		unimods.put("640", "Arg-_Lys");
		unimods.put("641", "Arg-_His");
		unimods.put("642", "Arg-_Gln");
		unimods.put("643", "Arg-_Met");
		unimods.put("644", "Arg-_Cys");
		unimods.put("645", "Arg-_Xle");
		unimods.put("646", "Arg-_Gly");
		unimods.put("647", "Ser-_Phe");
		unimods.put("648", "Ser-_Ala");
		unimods.put("649", "Ser-_Trp");
		unimods.put("650", "Ser-_Thr");
		unimods.put("651", "Ser-_Asn");
		unimods.put("652", "Ser-_Pro");
		unimods.put("653", "Ser-_Tyr");
		unimods.put("654", "Ser-_Cys");
		unimods.put("655", "Ser-_Arg");
		unimods.put("656", "Ser-_Xle");
		unimods.put("657", "Ser-_Gly");
		unimods.put("658", "Thr-_Ser");
		unimods.put("659", "Thr-_Ala");
		unimods.put("660", "Thr-_Asn");
		unimods.put("661", "Thr-_Lys");
		unimods.put("662", "Thr-_Pro");
		unimods.put("663", "Thr-_Met");
		unimods.put("664", "Thr-_Xle");
		unimods.put("665", "Thr-_Arg");
		unimods.put("666", "Val-_Phe");
		unimods.put("667", "Val-_Ala");
		unimods.put("668", "Val-_Glu");
		unimods.put("669", "Val-_Met");
		unimods.put("670", "Val-_Asp");
		unimods.put("671", "Val-_Xle");
		unimods.put("672", "Val-_Gly");
		unimods.put("673", "Trp-_Ser");
		unimods.put("674", "Trp-_Cys");
		unimods.put("675", "Trp-_Arg");
		unimods.put("676", "Trp-_Gly");
		unimods.put("677", "Trp-_Xle");
		unimods.put("678", "Tyr-_Phe");
		unimods.put("679", "Tyr-_Ser");
		unimods.put("680", "Tyr-_Asn");
		unimods.put("681", "Tyr-_His");
		unimods.put("682", "Tyr-_Asp");
		unimods.put("683", "Tyr-_Cys");
		unimods.put("684", "BDMAPP");
		unimods.put("685", "NA-LNO2");
		unimods.put("686", "NA-OA-NO2");
		unimods.put("687", "ICPL_2H(4)");
		unimods.put("894", "CarboxymethylDTT");
		unimods.put("730", "iTRAQ8plex");
		unimods.put("695", "Label_13C(6)15N(1)");
		unimods.put("696", "Label_2H(9)13C(6)15N(2)");
		unimods.put("720", "HNE-Delta_H(2)O");
		unimods.put("721", "4-ONE");
		unimods.put("723", "O-Dimethylphosphate");
		unimods.put("724", "O-Methylphosphate");
		unimods.put("725", "Diethylphosphate");
		unimods.put("726", "Ethylphosphate");
		unimods.put("727", "O-pinacolylmethylphosphonate");
		unimods.put("728", "Methylphosphonate");
		unimods.put("729", "O-Isopropylmethylphosphonate");
		unimods.put("731", "iTRAQ8plex_13C(6)15N(2)");
		unimods.put("735", "DTT_ST");
		unimods.put("734", "Ethanolamine");
		unimods.put("737", "TMT6plex");
		unimods.put("736", "DTT_C");
		unimods.put("738", "TMT2plex");
		unimods.put("739", "TMT");
		unimods.put("740", "ExacTagThiol");
		unimods.put("741", "ExacTagAmine");
		unimods.put("744", "NO_SMX_SEMD");
		unimods.put("743", "4-ONE+Delta_H(-2)O(-1)");
		unimods.put("745", "NO_SMX_SMCT");
		unimods.put("746", "NO_SMX_SIMD");
		unimods.put("747", "Malonyl");
		unimods.put("748", "3sulfo");
		unimods.put("750", "trifluoro");
		unimods.put("751", "TNBS");
		unimods.put("774", "Biotin-phenacyl");
		unimods.put("764", "DTT_C_2H(6)");
		unimods.put("771", "lapachenole");
		unimods.put("772", "Label_13C(5)");
		unimods.put("773", "maleimide");
		unimods.put("762", "IDEnT");
		unimods.put("763", "DTT_ST_2H(6)");
		unimods.put("765", "Met-loss");
		unimods.put("766", "Met-loss+Acetyl");
		unimods.put("767", "Menadione-HQ");
		unimods.put("775", "Carboxymethyl_13C(2)");
		unimods.put("776", "NEM_2H(5)");
		unimods.put("822", "Gly-loss+Amide");
		unimods.put("827", "TMPP-Ac");
		unimods.put("799", "Label_13C(6)+GG");
		unimods.put("837", "Arg-_Npo");
		unimods.put("834", "Label_2H(4)+Acetyl");
		unimods.put("801", "Pentylamine");
		unimods.put("800", "Biotin_Thermo-21345");
		unimods.put("830", "Dihydroxyimidazolidine");
		unimods.put("825", "DFDNB");
		unimods.put("821", "Cy3b-maleimide");
		unimods.put("793", "Hex1HexNAc1");
		unimods.put("792", "AEC-MAEC_2H(4)");
		unimods.put("824", "BMOE");
		unimods.put("811", "Biotin_Thermo-21360");
		unimods.put("835", "Label_13C(6)+Acetyl");
		unimods.put("836", "Label_13C(6)15N(2)+Acetyl");
		unimods.put("846", "EQIGG");
		unimods.put("849", "cGMP");
		unimods.put("851", "cGMP+RMP-loss");
		unimods.put("888", "mTRAQ");
		unimods.put("848", "Arg2PG");
		unimods.put("853", "Label_2H(4)+GG");
		unimods.put("854", "Label_13C(8)15N(2)");
		unimods.put("862", "Label_13C(1)2H(3)");
		unimods.put("861", "ZGB");
		unimods.put("859", "MG-H1");
		unimods.put("860", "G-H1");
		unimods.put("864", "Label_13C(6)15N(2)+GG");
		unimods.put("866", "ICPL_13C(6)2H(4)");
		unimods.put("890", "DyLight-maleimide");
		unimods.put("889", "mTRAQ_13C(3)15N(1)");
		unimods.put("891", "Methyl-PEO12-Maleimide");
		unimods.put("887", "MDCC");
		unimods.put("877", "QQQTGG");
		unimods.put("876", "QEQTGG");
		unimods.put("886", "HydroxymethylOP");
		unimods.put("884", "Biotin_Thermo-21325");
		unimods.put("885", "Label_13C(1)2H(3)+Oxidation");
		unimods.put("878", "Bodipy");
		unimods.put("895", "Biotin-PEG-PRA");
		unimods.put("896", "Met-_Aha");
		unimods.put("897", "Label_15N(4)");
		unimods.put("898", "pyrophospho");
		unimods.put("899", "Met-_Hpg");
		unimods.put("901", "4AcAllylGal");
		unimods.put("902", "DimethylArsino");
		unimods.put("903", "Lys-_CamCys");
		unimods.put("904", "Phe-_CamCys");
		unimods.put("905", "Leu-_MetOx");
		unimods.put("906", "Lys-_MetOx");
		unimods.put("907", "Galactosyl");
		unimods.put("908", "SMCC-maleimide");
		unimods.put("910", "Bacillosamine");
		unimods.put("911", "MTSL");
		unimods.put("912", "HNE-BAHAH");
		unimods.put("915", "Ethoxyformyl");
		unimods.put("914", "Methylmalonylation");
		unimods.put("938", "AROD");
		unimods.put("939", "Cys-_methylaminoAla");
		unimods.put("940", "Cys-_ethylaminoAla");
		unimods.put("923", "Label_13C(4)15N(2)+GG");
		unimods.put("926", "ethylamino");
		unimods.put("928", "MercaptoEthanol");
		unimods.put("935", "Atto495Maleimide");
		unimods.put("934", "AMTzHexNAc2");
		unimods.put("931", "Ethyl+Deamidated");
		unimods.put("932", "VFQQQTGG");
		unimods.put("933", "VIEVYQEQTGG");
		unimods.put("936", "Chlorination");
		unimods.put("937", "dichlorination");
		unimods.put("941", "DNPS");
		unimods.put("942", "SulfoGMBS");
		unimods.put("943", "DimethylamineGMBS");
		unimods.put("944", "Label_15N(2)2H(9)");
		unimods.put("946", "LG-anhydrolactam");
		unimods.put("947", "LG-pyrrole");
		unimods.put("948", "LG-anhyropyrrole");
		unimods.put("949", "3-deoxyglucosone");
		unimods.put("950", "Cation_Li");
		unimods.put("951", "Cation_Ca[II]");
		unimods.put("952", "Cation_Fe[II]");
		unimods.put("953", "Cation_Ni[II]");
		unimods.put("954", "Cation_Zn[II]");
		unimods.put("955", "Cation_Ag");
		unimods.put("956", "Cation_Mg[II]");
		unimods.put("957", "2-succinyl");
		unimods.put("958", "Propargylamine");
		unimods.put("959", "Phosphopropargyl");
		unimods.put("960", "SUMO2135");
		unimods.put("961", "SUMO3549");
		unimods.put("975", "Chlorpyrifos");
		unimods.put("978", "BITC");
		unimods.put("977", "Carbofuran");
		unimods.put("979", "PEITC");
		unimods.put("967", "thioacylPA");
		unimods.put("971", "maleimide3");
		unimods.put("972", "maleimide5");
		unimods.put("973", "Puromycin");
		unimods.put("981", "glucosone");
		unimods.put("986", "Label_13C(6)+Dimethyl");
		unimods.put("984", "cysTMT");
		unimods.put("985", "cysTMT6plex");
		unimods.put("991", "ISD_z+2_ion");
		unimods.put("989", "Ammonium");
		unimods.put("998", "BHAc");
		unimods.put("993", "Biotin_Sigma-B1267");
		unimods.put("994", "Label_15N(1)");
		unimods.put("995", "Label_15N(2)");
		unimods.put("996", "Label_15N(3)");
		unimods.put("997", "sulfo+amino");
		unimods.put("1000", "AHA-Alkyne");
		unimods.put("1001", "AHA-Alkyne-KDDDD");
		unimods.put("1002", "EGCG1");
		unimods.put("1003", "EGCG2");
		unimods.put("1004", "Label_13C(6)15N(4)+Methyl");
		unimods.put("1005", "Label_13C(6)15N(4)+Dimethyl");
		unimods.put("1006", "Label_13C(6)15N(4)+Methyl_2H(3)13C(1)");
		unimods.put("1007", "Label_13C(6)15N(4)+Dimethyl_2H(6)13C(2)");
		unimods.put("1008", "SecCarbamidomethyl");
		unimods.put("1009", "Thiazolidine");
		unimods.put("1010", "DEDGFLYMVYASQETFG");
		unimods.put("1012", "Biotin_Invitrogen-M1602");
		unimods.put("1020", "Xlink_DSS");
		unimods.put("1017", "DMPO");
		unimods.put("1014", "glycidamide");
		unimods.put("1015", "Ahx2+Hsl");
		unimods.put("1018", "ICDID");
		unimods.put("1019", "ICDID_2H(6)");
		unimods.put("1021", "Xlink_EGS");
		unimods.put("1022", "Xlink_DST");
		unimods.put("1023", "Xlink_DTSSP");
		unimods.put("1024", "Xlink_SMCC");
		unimods.put("1032", "2-nitrobenzyl");
		unimods.put("1027", "Xlink_DMP-de");
		unimods.put("1028", "Xlink_EGScleaved");
		unimods.put("1033", "SecNEM");
		unimods.put("1034", "SecNEM_2H(5)");
		unimods.put("1035", "Thiadiazole");
		unimods.put("1031", "Biotin_Thermo-88310");
		unimods.put("1038", "TAMRA-FP");
		unimods.put("1039", "Biotin_Thermo-21901+H2O");
		unimods.put("1041", "Deoxyhypusine");
		unimods.put("1042", "Acetyldeoxyhypusine");
		unimods.put("1043", "Acetylhypusine");
		unimods.put("1044", "Ala-_Cys");
		unimods.put("1045", "Ala-_Phe");
		unimods.put("1046", "Ala-_His");
		unimods.put("1047", "Ala-_Xle");
		unimods.put("1048", "Ala-_Lys");
		unimods.put("1049", "Ala-_Met");
		unimods.put("1050", "Ala-_Asn");
		unimods.put("1051", "Ala-_Gln");
		unimods.put("1052", "Ala-_Arg");
		unimods.put("1053", "Ala-_Trp");
		unimods.put("1054", "Ala-_Tyr");
		unimods.put("1055", "Cys-_Ala");
		unimods.put("1056", "Cys-_Asp");
		unimods.put("1057", "Cys-_Glu");
		unimods.put("1058", "Cys-_His");
		unimods.put("1059", "Cys-_Xle");
		unimods.put("1060", "Cys-_Lys");
		unimods.put("1061", "Cys-_Met");
		unimods.put("1062", "Cys-_Asn");
		unimods.put("1063", "Cys-_Pro");
		unimods.put("1064", "Cys-_Gln");
		unimods.put("1065", "Cys-_Thr");
		unimods.put("1066", "Cys-_Val");
		unimods.put("1067", "Asp-_Cys");
		unimods.put("1068", "Asp-_Phe");
		unimods.put("1069", "Asp-_Xle");
		unimods.put("1070", "Asp-_Lys");
		unimods.put("1071", "Asp-_Met");
		unimods.put("1072", "Asp-_Pro");
		unimods.put("1073", "Asp-_Gln");
		unimods.put("1074", "Asp-_Arg");
		unimods.put("1075", "Asp-_Ser");
		unimods.put("1076", "Asp-_Thr");
		unimods.put("1077", "Asp-_Trp");
		unimods.put("1078", "Glu-_Cys");
		unimods.put("1079", "Glu-_Phe");
		unimods.put("1080", "Glu-_His");
		unimods.put("1081", "Glu-_Xle");
		unimods.put("1082", "Glu-_Met");
		unimods.put("1083", "Glu-_Asn");
		unimods.put("1084", "Glu-_Pro");
		unimods.put("1085", "Glu-_Arg");
		unimods.put("1086", "Glu-_Ser");
		unimods.put("1087", "Glu-_Thr");
		unimods.put("1088", "Glu-_Trp");
		unimods.put("1089", "Glu-_Tyr");
		unimods.put("1090", "Phe-_Ala");
		unimods.put("1091", "Phe-_Asp");
		unimods.put("1092", "Phe-_Glu");
		unimods.put("1093", "Phe-_Gly");
		unimods.put("1094", "Phe-_His");
		unimods.put("1095", "Phe-_Lys");
		unimods.put("1096", "Phe-_Met");
		unimods.put("1097", "Phe-_Asn");
		unimods.put("1098", "Phe-_Pro");
		unimods.put("1099", "Phe-_Gln");
		unimods.put("1100", "Phe-_Arg");
		unimods.put("1101", "Phe-_Thr");
		unimods.put("1102", "Phe-_Trp");
		unimods.put("1103", "Gly-_Phe");
		unimods.put("1104", "Gly-_His");
		unimods.put("1105", "Gly-_Xle");
		unimods.put("1106", "Gly-_Lys");
		unimods.put("1107", "Gly-_Met");
		unimods.put("1108", "Gly-_Asn");
		unimods.put("1109", "Gly-_Pro");
		unimods.put("1110", "Gly-_Gln");
		unimods.put("1111", "Gly-_Thr");
		unimods.put("1112", "Gly-_Tyr");
		unimods.put("1113", "His-_Ala");
		unimods.put("1114", "His-_Cys");
		unimods.put("1115", "His-_Glu");
		unimods.put("1116", "His-_Phe");
		unimods.put("1117", "His-_Gly");
		unimods.put("1119", "His-_Lys");
		unimods.put("1120", "His-_Met");
		unimods.put("1121", "His-_Ser");
		unimods.put("1122", "His-_Thr");
		unimods.put("1123", "His-_Val");
		unimods.put("1124", "His-_Trp");
		unimods.put("1126", "Xle-_Cys");
		unimods.put("1127", "Xle-_Asp");
		unimods.put("1128", "Xle-_Glu");
		unimods.put("1129", "Xle-_Gly");
		unimods.put("1130", "Xle-_Tyr");
		unimods.put("1131", "Lys-_Ala");
		unimods.put("1132", "Lys-_Cys");
		unimods.put("1133", "Lys-_Asp");
		unimods.put("1134", "Lys-_Phe");
		unimods.put("1135", "Lys-_Gly");
		unimods.put("1136", "Lys-_His");
		unimods.put("1137", "Lys-_Pro");
		unimods.put("1138", "Lys-_Ser");
		unimods.put("1139", "Lys-_Val");
		unimods.put("1140", "Lys-_Trp");
		unimods.put("1141", "Lys-_Tyr");
		unimods.put("1142", "Met-_Ala");
		unimods.put("1143", "Met-_Cys");
		unimods.put("1144", "Met-_Asp");
		unimods.put("1145", "Met-_Glu");
		unimods.put("1146", "Met-_Phe");
		unimods.put("1147", "Met-_Gly");
		unimods.put("1148", "Met-_His");
		unimods.put("1149", "Met-_Asn");
		unimods.put("1150", "Met-_Pro");
		unimods.put("1151", "Met-_Gln");
		unimods.put("1152", "Met-_Ser");
		unimods.put("1153", "Met-_Trp");
		unimods.put("1154", "Met-_Tyr");
		unimods.put("1155", "Asn-_Ala");
		unimods.put("1156", "Asn-_Cys");
		unimods.put("1157", "Asn-_Glu");
		unimods.put("1158", "Asn-_Phe");
		unimods.put("1159", "Asn-_Gly");
		unimods.put("1160", "Asn-_Met");
		unimods.put("1161", "Asn-_Pro");
		unimods.put("1162", "Asn-_Gln");
		unimods.put("1163", "Asn-_Arg");
		unimods.put("1164", "Asn-_Val");
		unimods.put("1165", "Asn-_Trp");
		unimods.put("1166", "Pro-_Cys");
		unimods.put("1167", "Pro-_Asp");
		unimods.put("1168", "Pro-_Glu");
		unimods.put("1169", "Pro-_Phe");
		unimods.put("1170", "Pro-_Gly");
		unimods.put("1171", "Pro-_Lys");
		unimods.put("1172", "Pro-_Met");
		unimods.put("1173", "Pro-_Asn");
		unimods.put("1174", "Pro-_Val");
		unimods.put("1175", "Pro-_Trp");
		unimods.put("1176", "Pro-_Tyr");
		unimods.put("1177", "Gln-_Ala");
		unimods.put("1178", "Gln-_Cys");
		unimods.put("1179", "Gln-_Asp");
		unimods.put("1180", "Gln-_Phe");
		unimods.put("1181", "Gln-_Gly");
		unimods.put("1182", "Gln-_Met");
		unimods.put("1183", "Gln-_Asn");
		unimods.put("1184", "Gln-_Ser");
		unimods.put("1185", "Gln-_Thr");
		unimods.put("1186", "Gln-_Val");
		unimods.put("1187", "Gln-_Trp");
		unimods.put("1188", "Gln-_Tyr");
		unimods.put("1189", "Arg-_Ala");
		unimods.put("1190", "Arg-_Asp");
		unimods.put("1191", "Arg-_Glu");
		unimods.put("1192", "Arg-_Asn");
		unimods.put("1193", "Arg-_Val");
		unimods.put("1194", "Arg-_Tyr");
		unimods.put("1195", "Arg-_Phe");
		unimods.put("1196", "Ser-_Asp");
		unimods.put("1197", "Ser-_Glu");
		unimods.put("1198", "Ser-_His");
		unimods.put("1199", "Ser-_Lys");
		unimods.put("1200", "Ser-_Met");
		unimods.put("1201", "Ser-_Gln");
		unimods.put("1202", "Ser-_Val");
		unimods.put("1203", "Thr-_Cys");
		unimods.put("1204", "Thr-_Asp");
		unimods.put("1205", "Thr-_Glu");
		unimods.put("1206", "Thr-_Phe");
		unimods.put("1207", "Thr-_Gly");
		unimods.put("1208", "Thr-_His");
		unimods.put("1209", "Thr-_Gln");
		unimods.put("1210", "Thr-_Val");
		unimods.put("1211", "Thr-_Trp");
		unimods.put("1212", "Thr-_Tyr");
		unimods.put("1213", "Val-_Cys");
		unimods.put("1214", "Val-_His");
		unimods.put("1215", "Val-_Lys");
		unimods.put("1216", "Val-_Asn");
		unimods.put("1217", "Val-_Pro");
		unimods.put("1218", "Val-_Gln");
		unimods.put("1219", "Val-_Arg");
		unimods.put("1220", "Val-_Ser");
		unimods.put("1221", "Val-_Thr");
		unimods.put("1222", "Val-_Trp");
		unimods.put("1223", "Val-_Tyr");
		unimods.put("1224", "Trp-_Ala");
		unimods.put("1225", "Trp-_Asp");
		unimods.put("1226", "Trp-_Glu");
		unimods.put("1227", "Trp-_Phe");
		unimods.put("1228", "Trp-_His");
		unimods.put("1229", "Trp-_Lys");
		unimods.put("1230", "Trp-_Met");
		unimods.put("1231", "Trp-_Asn");
		unimods.put("1232", "Trp-_Pro");
		unimods.put("1233", "Trp-_Gln");
		unimods.put("1234", "Trp-_Thr");
		unimods.put("1235", "Trp-_Val");
		unimods.put("1236", "Trp-_Tyr");
		unimods.put("1237", "Tyr-_Ala");
		unimods.put("1238", "Tyr-_Glu");
		unimods.put("1239", "Tyr-_Gly");
		unimods.put("1240", "Tyr-_Lys");
		unimods.put("1241", "Tyr-_Met");
		unimods.put("1242", "Tyr-_Pro");
		unimods.put("1243", "Tyr-_Gln");
		unimods.put("1244", "Tyr-_Arg");
		unimods.put("1245", "Tyr-_Thr");
		unimods.put("1246", "Tyr-_Val");
		unimods.put("1247", "Tyr-_Trp");
		unimods.put("1248", "Tyr-_Xle");
		unimods.put("1249", "AHA-SS");
		unimods.put("1250", "AHA-SS_CAM");
		unimods.put("1251", "Biotin_Thermo-33033");
		unimods.put("1252", "Biotin_Thermo-33033-H");
		unimods.put("1253", "2-monomethylsuccinyl");
		unimods.put("1254", "Saligenin");
		unimods.put("1255", "Cresylphosphate");
		unimods.put("1256", "CresylSaligeninPhosphate");
		unimods.put("1257", "Ub-Br2");
		unimods.put("1258", "Ub-VME");
		unimods.put("1260", "Ub-amide");
		unimods.put("1261", "Ub-fluorescein");
		unimods.put("1262", "2-dimethylsuccinyl");
		unimods.put("1263", "Gly");
		unimods.put("1264", "pupylation");
		unimods.put("1266", "Label_13C(4)");
		unimods.put("1271", "HCysteinyl");
		unimods.put("1267", "Label_13C(4)+Oxidation");
		unimods.put("1276", "UgiJoullie");
		unimods.put("1270", "HCysThiolactone");
		unimods.put("1282", "UgiJoullieProGly");
		unimods.put("1277", "Dipyridyl");
		unimods.put("1278", "Furan");
		unimods.put("1279", "Difuran");
		unimods.put("1281", "BMP-piperidinol");
		unimods.put("1283", "UgiJoullieProGlyProGly");
		unimods.put("1287", "Arg-loss");
		unimods.put("1288", "Arg");
		unimods.put("1286", "IMEHex(2)NeuAc(1)");
		unimods.put("1289", "Butyryl");
		unimods.put("1290", "Dicarbamidomethyl");
		unimods.put("1291", "Dimethyl_2H(6)");
		unimods.put("1292", "GGQ");
		unimods.put("1293", "QTGG");
		unimods.put("1297", "Label_13C(3)15N(1)");
		unimods.put("1296", "Label_13C(3)");
		unimods.put("1298", "Label_13C(4)15N(1)");
		unimods.put("1299", "Label_2H(10)");
		unimods.put("1300", "Label_2H(4)13C(1)");
		unimods.put("1301", "Lys");
		unimods.put("1302", "mTRAQ_13C(6)15N(2)");
		unimods.put("1303", "NeuAc");
		unimods.put("1304", "NeuGc");
		unimods.put("1305", "Propyl");
		unimods.put("1306", "Propyl_2H(6)");
		unimods.put("1310", "Propiophenone");
		unimods.put("1345", "PS_Hapten");
		unimods.put("1348", "Cy3-maleimide");
		unimods.put("1312", "Delta_H(6)C(3)O(1)");
		unimods.put("1313", "Delta_H(8)C(6)O(1)");
		unimods.put("1314", "biotinAcrolein298");
		unimods.put("1315", "MM-diphenylpentanone");
		unimods.put("1317", "EHD-diphenylpentanone");
		unimods.put("1349", "benzylguanidine");
		unimods.put("1350", "CarboxymethylDMAP");
		unimods.put("1320", "Biotin_Thermo-21901+2H2O");
		unimods.put("1321", "DiLeu4plex115");
		unimods.put("1322", "DiLeu4plex");
		unimods.put("1323", "DiLeu4plex117");
		unimods.put("1324", "DiLeu4plex118");
		unimods.put("1330", "bisANS-sulfonates");
		unimods.put("1331", "DNCB_hapten");
		unimods.put("1326", "NEMsulfur");
		unimods.put("1327", "SulfurDioxide");
		unimods.put("1328", "NEMsulfurWater");
		unimods.put("1389", "HN3_mustard");
		unimods.put("1387", "3-phosphoglyceryl");
		unimods.put("1388", "HN2_mustard");
		unimods.put("1358", "NEM_2H(5)+H2O");
		unimods.put("1363", "Crotonyl");
		unimods.put("1364", "O-Et-N-diMePhospho");
		unimods.put("1365", "N-dimethylphosphate");
		unimods.put("1356", "phosphoRibosyl");
		unimods.put("1355", "azole");
		unimods.put("1340", "Biotin_Thermo-21911");
		unimods.put("1341", "iodoTMT");
		unimods.put("1342", "iodoTMT6plex");
		unimods.put("1343", "Gluconoylation");
		unimods.put("1344", "Phosphogluconoylation");
		unimods.put("1368", "Methyl_2H(3)+Acetyl_2H(3)");
		unimods.put("1367", "dHex(1)Hex(1)");
		unimods.put("1380", "methylsulfonylethyl");
		unimods.put("1370", "Label_2H(3)+Oxidation");
		unimods.put("1371", "Trimethyl_2H(9)");
		unimods.put("1372", "Acetyl_13C(2)");
		unimods.put("1375", "dHex(1)Hex(2)");
		unimods.put("1376", "dHex(1)Hex(3)");
		unimods.put("1377", "dHex(1)Hex(4)");
		unimods.put("1378", "dHex(1)Hex(5)");
		unimods.put("1379", "dHex(1)Hex(6)");
		unimods.put("1381", "ethylsulfonylethyl");
		unimods.put("1382", "phenylsulfonylethyl");
		unimods.put("1383", "PyridoxalPhosphateH2");
		unimods.put("1384", "Homocysteic_acid");
		unimods.put("1385", "Hydroxamic_acid");
		unimods.put("1390", "Oxidation+NEM");
		unimods.put("1391", "NHS-fluorescein");
		unimods.put("1392", "DiART6plex");
		unimods.put("1393", "DiART6plex115");
		unimods.put("1394", "DiART6plex116_119");
		unimods.put("1395", "DiART6plex117");
		unimods.put("1396", "DiART6plex118");
		unimods.put("1397", "Iodoacetanilide");
		unimods.put("1398", "Iodoacetanilide_13C(6)");
		unimods.put("1399", "Dap-DSP");
		unimods.put("1400", "MurNAc");
		unimods.put("1405", "EEEDVIEVYQEQTGG");
		unimods.put("1402", "Label_2H(7)15N(4)");
		unimods.put("1403", "Label_2H(6)15N(1)");
		unimods.put("1406", "EDEDTIDVFQQQTGG");
		unimods.put("1408", "Hex(5)HexNAc(4)NeuAc(2)");
		unimods.put("1409", "Hex(5)HexNAc(4)NeuAc(1)");
		unimods.put("1410", "dHex(1)Hex(5)HexNAc(4)NeuAc(1)");
		unimods.put("1411", "dHex(1)Hex(5)HexNAc(4)NeuAc(2)");
		unimods.put("1414", "Trimethyl_13C(3)2H(9)");
		unimods.put("99991", "RNPXlink1");
		unimods.put("99992", "RNPXlink2");
		unimods.put("99993", "RNPXlink3");
		unimods.put("99994", "RNPXlink4");
		unimods.put("99995", "RNPXlink5");

	}

	public static String unimod(String s) {
		String[] arr = s.replaceAll("[()]", "\t").split("\t");
		if (arr.length == 1) {
			return s;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arr.length; ++i) {
			if (arr[i].toLowerCase().contains("unimod")) {
				sb.append("(");
				String k = arr[i].replaceAll("[^0-9]", "");
				sb.append(unimods.get(k));
				sb.append(")");
			} else {
				sb.append(arr[i]);
			}
		}
		return sb.toString();
	}

	//
	static public TreeMap<Float, float[]> mzmap = new TreeMap<>();
	static public float[] mzst, mzed;

	static public TreeMap<String, ArrayList<Float>> libs = new TreeMap<>();

	static public TreeMap<String, float[]> rtmap = new TreeMap<>();
	static public TreeMap<String, float[]> irtmap = new TreeMap<>();

	// public static ArrayList<ArrayList<StringBuffer>[]> out = new
	// ArrayList<>();

	public static void main(String[] args) throws IOException, SAXException, DataFormatException {

		if (args.length != 3) {
			if (args.length == 1) {
				// merge
				File rt = new File(args[0]);
				TreeSet<String> set = new TreeSet<>();
				for (String fn : rt.list()) {
					if (fn.endsWith(".inf.out") || fn.endsWith(".txt.out")) {
						set.add(fn.substring(0, fn.length() - 8));
					}
				}
				dump(args[0], set);
				return;

			}
			if (args.length == 4) {

				float dt = Float.parseFloat(args[3]);

				TreeSet<String> set = new TreeSet<>();
				Scanner al = new Scanner(new File(args[1]));
				// align.tsv
				al.nextLine();

				TreeMap<String, ArrayList<float[]>> firt2rt = new TreeMap<>();

				while (al.hasNextLine()) {
					String[] line = al.nextLine().split("\t");

					if (!firt2rt.containsKey(line[3])) {
						firt2rt.put(line[3], new ArrayList<>());
					}

					float t = Float.parseFloat(line[4]);
					float irt = Float.parseFloat(line[17]);

					firt2rt.get(line[3]).add(new float[] { irt, t });

					if (!line[12].equals("0")) {
						// deocy
						continue;
					}
					// protein unimodpeptide charge
					String p = line[11] + "\t" + line[7] + "\t" + line[8];
					String fnp = line[3] + "\t" + p;
					// set.add(p);
					set.add(line[3]);
					if (!irtmap.containsKey(p)) {
						irtmap.put(p, new float[] { 0, 0, 0 });
					}
					if (!rtmap.containsKey(fnp)) {
						rtmap.put(fnp, new float[] { t - dt, t + dt });
					}
					float[] arr = irtmap.get(p);
					++arr[1];
					arr[2] += irt;
				}
				for (Entry<String, float[]> e : irtmap.entrySet()) {
					float[] ev = e.getValue();
					ev[0] = ev[2] / ev[1];
				}
				for (Entry<String, ArrayList<float[]>> e : firt2rt.entrySet()) {
					ArrayList<float[]> ev = e.getValue();
					ev.sort((a, b) -> {
						return Float.compare(a[0], b[0]);
					});
				}

				al.close();

				Scanner ls = new Scanner(new File(args[2]));// lib.tsv
				ls.nextLine();
				while (ls.hasNextLine()) {
					String[] line = ls.nextLine().split("\t");
					if (!line[7].equals("0")) {
						// deocy
						continue;
					}
					// protein unimodpeptide charge
					String p = line[9] + "\t" + line[11] + "\t" + line[12];
					float mz = Float.parseFloat(line[1]);
					if (mz < 100 || mz > 2000) {
						continue;
					}
					if (!irtmap.containsKey(p)) {
						continue;
					}
					ArrayList<Float> arr = null;
					if (!libs.containsKey(p)) {
						arr = new ArrayList<>();
						arr.add(Float.parseFloat(line[0]));
						libs.put(p, arr);
					}
					arr = libs.get(p);
					arr.add(mz);
				}
				for (Entry<String, ArrayList<Float>> e : libs.entrySet()) {
					ArrayList<Float> arr = e.getValue();
					float mz = arr.get(0);
					arr.remove(0);
					arr.sort(Float::compareTo);
					arr.add(0, mz);
				}
				ls.close();

				for (Entry<String, float[]> e : irtmap.entrySet()) {
					for (String xml : set) {
						String fnp = xml + "\t" + e.getKey();
						if (rtmap.containsKey(fnp)) {
							continue;
						}
						float irt = e.getValue()[0];
						float mindist = 1e5f;
						float rt = 0;
						for (float[] v : firt2rt.get(xml)) {
							if (Math.abs(v[0] - irt) < mindist) {
								mindist = Math.abs(v[0] - irt);
								rt = v[1];
							}
						}
						rtmap.put(fnp, new float[] { rt - dt, rt + dt });
					}
				}

				for (String xml : set) {
					// al = new Scanner(new File(args[1]));
					// al.nextLine();
					//
					// TreeMap<String, float[]> map = new TreeMap<>(rtmap);
					// while (al.hasNextLine()) {
					// String[] line = al.nextLine().split("\t");
					//
					// if (!line[3].equals(xml)) {
					// continue;
					// }
					// if (!line[12].equals("0")) {
					// // deocy
					// continue;
					// }
					// // protein unimodpeptide charge
					// String p = line[11] + "\t" + line[7] + "\t" + line[8];
					// // set.add(p);
					// float t = Float.parseFloat(line[4]);
					// float irt=Float.parseFloat(line[17]);
					// map.put(p, new float[] { t - dt, t + dt ,t,irt});
					// }
					// al.close();
					String fn = new File(xml).getName() + ".xic";

					PrintWriter pw = new PrintWriter(args[0] + File.separator + fn);
	
					String xfn=new File(xml).getName();
					
					pw.println(new File(xml).getParentFile().getAbsolutePath()+File.separator+xfn);
					pw.println(xfn);
					for (Entry<String, ArrayList<Float>> e : libs.entrySet()) {
						float[] time = rtmap.get(xml + "\t" + e.getKey());
						pw.println(toFilename(e.getKey()));
						pw.print(time[0] + "\t" + time[1]);
						for (float v : e.getValue()) {
							pw.print("\t" + v);
						}
						pw.println();
					}

					pw.close();
				}

				return;
			}

			System.out.println("for init outputPath0 aligned.tsv1 lib.tsv2 dt3");
			System.out.println("for dumpxic outputPath0 init.xic1 win.os.tsv2");
			System.out.println("for merge outputPath0");
			return;
		}

		final float dmz = 1 / 50.0f;

		for (float m = 99 - dmz, i = 0; m <= 2001; ++i) {
			float k = m;
			float v = m + dmz;
			k = Math.round(k * 1000) * 0.001f;
			v = Math.round(v * 1000) * 0.001f;
			mzmap.put(k, new float[] { v, i });
			m = (4950 + i) / 50.0f;
		}
		mzst = new float[mzmap.size()];
		mzed = new float[mzmap.size()];
		for (Entry<Float, float[]> e : mzmap.entrySet()) {
			float[] v = e.getValue();
			int i = (int) Math.round(v[1]);
			mzst[i] = e.getKey();
			mzed[i] = v[0];
		}

		ArrayList<Draw> arr = new ArrayList<>();

		Scanner s = new Scanner(new File(args[1]));

		String xml = s.nextLine();
		String fn = s.nextLine();
		while (s.hasNextLine()) {
			String line = s.nextLine();
			String data = s.nextLine();
			Draw d = new Draw();
			d.name = line;
			Scanner sd = new Scanner(data);
			d.rt = new float[2];
			d.rt[0] = sd.nextFloat();
			d.rt[1] = sd.nextFloat();
			d.mz = new ArrayList<>();
			while (sd.hasNextFloat()) {
				d.mz.add(sd.nextFloat());
			}
			sd.close();
			arr.add(d);
		}

		s.close();

		arr.trimToSize();

		XMLReader parser = XMLReaderFactory.createXMLReader();

		PrepareDraw_mzXML hdl = new PrepareDraw_mzXML(args[2], mzmap.size());

		hdl.draws = arr;

		parser.setContentHandler(hdl);

		InputStream is = new FileInputStream(xml);
		if (xml.endsWith("gz")) {
			is = new GZIPInputStream(is, 16777216);
		}

		parser.parse(new InputSource(new BufferedInputStream(is, 16777216)));

		PrintWriter info = new PrintWriter(args[0] + "/" + fn + ".inf.out");
		PrintWriter txt = new PrintWriter(args[0] + "/" + fn + ".txt.out");

		for (Draw d : arr) {

			ArrayList<StringBuffer>[] q = d.save();
			d.clear();

			for (StringBuffer t : q[0]) {
				info.println(t);
			}
			for (StringBuffer t : q[1]) {
				txt.println(t);
			}
		}
		info.close();
		txt.close();

	}

	private static String toFilename(String name) {
		String fn = name;
		String[] fns = fn.split("\t");

		String[] fnt = fns[0].split("[/]");
		String t3 = null;

		if (fnt.length > 4) {
			t3 = fnt[0] + "/" + fnt[1] + "/" + fnt[2] + "/" + fnt[3];
		} else {
			t3 = fns[0];
		}

		fn = (t3 + "-" + PrepareDraw.unimod(fns[1]) + "-" + fns[2]).replaceAll("[\\\\/:*?\"<>|]", "_");
		if (fn.length() > 240) {
			fn = (fnt[0] + "/" + fnt[1] + '-' + PrepareDraw.unimod(fns[1]) + "-" + fns[2]).replaceAll("[\\\\/:*?\"<>|]",
					"_");
		}
		if (fn.length() > 240) {
			System.err.println(fn);
			fn = fn.substring(0, 240);
		}

		return fn;
	}

	@SuppressWarnings("unchecked")
	private static void dump(String rt, TreeSet<String> set) throws FileNotFoundException {
		int n = set.size();
		Scanner[] stxt = new Scanner[n];
		Scanner[] sinf = new Scanner[n];
		ArrayList<String>[] sbtxt = new ArrayList[n];
		ArrayList<String>[] sbinf = new ArrayList[n];
		ArrayList<StringBuffer> otxt = new ArrayList<>();
		ArrayList<StringBuffer> oinf = new ArrayList<>();
		n = 0;
		for (String s : set) {
			stxt[n] = new Scanner(new File(rt + "/" + s + ".txt.out"));
			sinf[n] = new Scanner(new File(rt + "/" + s + ".inf.out"));
			sbtxt[n] = new ArrayList<>();
			sbinf[n] = new ArrayList<>();
			++n;
		}
		boolean finish = false;
		while (!finish) {
			finish = true;
			String fn = null;
			for (int i = 0; i < n; ++i) {
				sbtxt[i].clear();
				sbinf[i].clear();
			}
			for (int i = 0; i < n; ++i) {

				fn = readin(sinf[i], sbinf[i]);
				fn = readin(stxt[i], sbtxt[i]);
				finish = fn == null;
			}
			if (finish) {
				break;
			}
			otxt.clear();
			oinf.clear();
			for (int i = 0; i < n; ++i) {
				ArrayList<String> itxt = sbtxt[i];
				ArrayList<String> iinf = sbinf[i];
				if (i == 0) {
					for (String s : itxt) {
						otxt.add(new StringBuffer(s));
					}
					for (String s : iinf) {
						oinf.add(new StringBuffer(s));
					}
					continue;
				}
				oinf.get(0).append(iinf.get(0));
				oinf.get(2).append(iinf.get(2));
				for (int j = 0; j < otxt.size(); ++j) {
					otxt.get(j).append(itxt.get(j));
				}
			}
			PrintWriter txtpw = new PrintWriter(rt + "/" + fn + ".txt");
			PrintWriter infpw = new PrintWriter(rt + "/" + fn + ".info.txt");

			for (StringBuffer s : otxt) {
				txtpw.println(s);
			}

			for (StringBuffer s : oinf) {
				infpw.println(s);
			}

			txtpw.close();
			infpw.close();
		}

		for (int i = 0; i < n; ++i) {
			stxt[i].close();
			sinf[i].close();
		}
	}

	private static String readin(Scanner sc, ArrayList<String> sb) {
		String fn = null;
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.startsWith("#BEG")) {
				fn = line.split("\t")[1];
				continue;
			}
			if (line.startsWith("#END")) {
				break;
			}
			sb.add(line);
		}
		return fn;

	}

}

class PrepareDraw_mzXML extends DefaultHandler {

	public TreeMap<Float, Float> win = new TreeMap<>();
	// public TreeMap<Float, ArrayList<float[]>> datas = new TreeMap<>();
	public int curr;
	public ArrayList<Draw> draws;
	public LinkedBlockingQueue<String>[] queues;
	public ArrayList<Draw>[] windraw;
	// public float ms2ppm = 50;
	Thread workers[];

	int n;
	static Decoder b64;

	@SuppressWarnings("unchecked")
	public PrepareDraw_mzXML(String wind, int n) throws FileNotFoundException {

		this.n = n;
		b64 = Base64.getDecoder();

		Scanner s = new Scanner(new File(wind));
		while (s.hasNextLine()) {
			String line = s.nextLine().replaceAll("[^0-9.]", " ");
			Scanner ls = new Scanner(line);
			ArrayList<Float> buf = new ArrayList<>();
			while (ls.hasNextFloat()) {
				buf.add(ls.nextFloat());
			}
			ls.close();
			if (buf.size() < 2) {
				continue;
			}
			win.put(buf.get(0), buf.get(1));

		}
		s.close();
		queues = new LinkedBlockingQueue[win.size()];
		windraw = new ArrayList[win.size()];
		workers = new Thread[win.size()];
		for (int i = 0; i < queues.length; ++i) {
			queues[i] = new LinkedBlockingQueue<>(320);
			windraw[i] = new ArrayList<>();
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {

		if (sb != null) {
			for (int i = start; i < start + length; ++i) {
				if (ch[i] <= ' ') {
					continue;
				}
				sb.append(ch[i]);
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {

		try {
			for (int i = 0; i < queues.length; ++i) {
				queues[i].put("-1");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < workers.length; ++i) {
			try {
				workers[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < windraw.length; ++i) {
			draws.addAll(windraw[i]);
		}
		windraw = null;
	}

	@Override
	public void startDocument() throws SAXException {
		
		int wn = win.size() - 1;
		for (float e : win.descendingKeySet()) {

			for (int i = 0; i < draws.size(); ++i) {
				if (draws.get(i) == null) {
					continue;
				}
				float mz = draws.get(i).mz.get(0);
				if (e < mz) {
					windraw[wn].add(draws.get(i));
					draws.set(i, null);
				}
			}
			--wn;
		}
		draws.clear();
		
		for (int i = 0; i < workers.length; ++i) {
			workers[i] = new Thread(new Worker( queues[i], windraw[i]));
			workers[i].start();
		}

	}

	static class Worker implements Runnable {

		private LinkedBlockingQueue<String> q;
		private ArrayList<Draw> draws;

		public Worker(LinkedBlockingQueue<String> q, ArrayList<Draw> draws) {
			this.q = q;
			this.draws = draws;
		}

		@Override
		public void run() {
			try {
				int cd = 0;
				float rt = 0;
				int n = 0;
				boolean zlib = true;
				int sz = 4;
				String b64s = null;
				while (true) {

					while (true) {

						String t = q.take();
						switch (cd) {
						case 0:
							rt = Float.parseFloat(t);
							if (rt < 0) {
								return;
							}
							cd = 1;
							break;
						case 1:
							n = Integer.parseInt(t);
							cd = 2;
							break;
						case 2:
							zlib = Integer.parseInt(t) == 1;
							cd = 3;
							break;
						case 3:
							sz = Integer.parseInt(t);
							cd = 4;
							break;
						case 4:
							b64s = t;
							cd = 5;
							break;
						}
						if (cd == 5) {
							cd = 0;
							break;
						}
					}

					byte[] buf = b64.decode(b64s);

					float[] data = new float[n];
					float mz = 0;
					if (!zlib) {
						for (int i = 0; i < buf.length / sz; ++i) {
							long v = 0;
							for (int j = i * sz; j < i * sz + sz; ++j) {
								v <<= 8;
								v |= (buf[j] & 0xFF);
							}
							float t = 0;
							if (sz == 4) {
								t = Float.intBitsToFloat((int) v);
							} else {
								t = (float) Double.longBitsToDouble(v);
							}
							if (i % 2 == 0) {
								mz = t;
							} else {
								if (mz < 100 || mz > 2000) {
								} else {
									Entry<Float, float[]> q = PrepareDraw.mzmap.floorEntry(mz);
									if (q != null) {
										int p = (int) Math.round(q.getValue()[1]);
										data[p - 1] += t;
										data[p] += t;
										data[p + 1] += t;
									}
								}
							}
						}
					} else {

						Inflater decompresser = new Inflater();
						decompresser.setInput(buf, 0, buf.length);
						byte[] bf = new byte[sz * 2];
						while (decompresser.inflate(bf) == bf.length) {
							for (int i = 0; i < 2; ++i) {
								long v = 0;
								for (int j = i * sz; j < i * sz + sz; ++j) {
									v <<= 8;
									v |= (bf[j] & 0xFF);
								}
								float t = 0;
								if (sz == 4) {
									t = Float.intBitsToFloat((int) v);
								} else {
									t = (float) Double.longBitsToDouble(v);
								}

								if (i % 2 == 0) {
									mz = t;
								} else {
									if (mz < 100 || mz > 2000) {
									} else {
										Entry<Float, float[]> q = PrepareDraw.mzmap.floorEntry(mz);
										if (q != null) {
											int p = (int) Math.round(q.getValue()[1]);
											data[p - 1] += t;
											data[p] += t;
											data[p + 1] += t;
										}
									}

								}
							}
						}
					}
					
					final float frt=rt;

					draws.parallelStream().forEach(d->{
						work(d,frt,data);
					});
				}
			} catch (InterruptedException | DataFormatException e) {
				e.printStackTrace();
			}
		}

		void work(Draw d, float rt,float[] data) {

			if (rt < d.rt[0]) {
				return;
			}
			if (rt > d.rt[1]) {
				return;
			}

			float[] arr = new float[d.mz.size()];
			arr[0] = rt;
			for (int i = 1; i < d.mz.size(); ++i) {
				arr[i] = 0;
				float t = d.mz.get(i);
				Entry<Float, float[]> ev = PrepareDraw.mzmap.floorEntry(t);
				int p = (int) Math.round(ev.getValue()[1]);

				if (p+1>=data.length||p-1<0){
				    continue;
				}
				
				arr[i] += data[p - 1];
				arr[i] += data[p];
				arr[i] += data[p + 1];
				arr[i] /= 3.0;
			}
			d.data.add(arr);
		}


	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (localName.equals("scan")) {
			if (level == 1) {
				return;
			}
		}
		if (localName.equals("precursorMz")) {
			curr = win.size() - 1;
			float pmz = Float.parseFloat(sb.toString().trim());
			for (float e : win.descendingKeySet()) {
				if (e < pmz) {
					break;
				}
				--curr;
			}
			sb = null;
		}
		if (localName.equals("peaks")) {

			if (level == 1) {
				sb = null;
				return;
			}
			try {
				queues[curr].put(rt + "");
				queues[curr].put(n + "");
				queues[curr].put(zlib ? "1" : "0");
				queues[curr].put(sz + "");
				queues[curr].put(sb.toString().trim());

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			sb = null;

		}
	}

	public StringBuffer sb = new StringBuffer();
	public boolean zlib;
	public float rt;
	public int level;
	public int sz;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (localName.equals("scan")) {
			level = Integer.parseInt(attributes.getValue("msLevel"));
			rt = Float.parseFloat(attributes.getValue("retentionTime").replaceAll("[^0-9.]", ""));
		}
		if (localName.equals("precursorMz")) {
			sb = new StringBuffer();
		}
		if (localName.equals("peaks")) {
			sz = Integer.parseInt(attributes.getValue("precision")) / 8;
			if ("zlib".equals(attributes.getValue("compressionType"))) {
				zlib = true;
			}
			sb = new StringBuffer();
		}
	}

}
