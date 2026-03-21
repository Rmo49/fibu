package com.rmo.fibu.util;

public abstract class ParserBase {

	static final public int docTypeCsv = 1;
	static final public int docTypePdf = 2;
	static final public String[] docTypes = { "CSV", "PDF" };

	// Name der bisher implementierten Parser.
	static public String companyNamePost = "Post";
	static public String companyNameRaiff = "Raiffeisen";
	static public String companyNameMB = "MB";
	static public String companyNameCumulus = "Cumulus";
	static public String companyNameCler = "Cler";
	static public String[] companyNameList = { companyNamePost, companyNameRaiff, companyNameMB, companyNameCumulus,
			companyNameCler };

	/**
	 * Die Basisklasse für alle Parser.
	 * Prüft, ob Parser vorhanden
	 *
	 * @param companyName
	 * @return Fehlermeldung-String
	 */
	public static String parserVorhanden(int docType, String companyName) {
		boolean hatParser = false;
		if (docType == docTypeCsv) {
			if (companyName.equalsIgnoreCase(ParserBase.companyNamePost)) {
				hatParser = true;
			}
			if (companyName.equalsIgnoreCase(ParserBase.companyNameRaiff)) {
				hatParser = true;
			}
			if (companyName.equalsIgnoreCase(ParserBase.companyNameMB)) {
				hatParser = true;
			}
			StringBuffer sb = new StringBuffer(100);
			if (!hatParser) {
				sb.append("Kein CVS-Parser für: ");
				sb.append(companyName);
				sb.append("\n Implementationen vorhanden für: ");
				sb.append(ParserBase.companyNamePost);
				sb.append(", ");
				sb.append(ParserBase.companyNameRaiff);
				sb.append(", ");
				sb.append(ParserBase.companyNameMB);
			}
			return sb.toString();
		}
		else {
			if (companyName.equalsIgnoreCase(ParserBase.companyNameCumulus)) {
				hatParser = true;
			}
			if (companyName.equalsIgnoreCase(ParserBase.companyNameCler)) {
				hatParser = true;
			}
			StringBuffer sb = new StringBuffer(100);
			if (!hatParser) {
				sb.append("Kein Pdf-Parser für: ");
				sb.append(companyName);
				sb.append("\n Implementationen vorhanden für: ");
				sb.append(ParserBase.companyNameCler);
				sb.append(", ");
				sb.append(ParserBase.companyNameCumulus);
			}
			return sb.toString();
		}
	}

	/**
	 * Betrag bereinigen.
	 * Die führenden + und - und das Tausender Trennzeichen entfernen.
	 * @param betrag
	 * @return
	 */
	protected String betragClean(String betrag) {
		if (betrag.startsWith("+") || betrag.startsWith("-")) {
			betrag = betrag.substring(1, betrag.length());
		}
		betrag = betrag.replace("'", "");
		return betrag;
	}


}
