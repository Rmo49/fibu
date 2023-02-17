package com.rmo.fibu.model;

/**
 * Die Company von der CSV- oder PDF-files eingelesen werden.
 * @author Ruedi
 *
 */
public class CsvCompany {

	static final public int docTypeCsv = 1;
	static final public int docTypePdf = 2;
	static final public String[] docTypes = { "CSV", "PDF"};


	private int companyID;
	private String companyName;
	private String kontoNrDefault;
	private String dirPath;
	private int docType = 1;	// 1=default
	private String docString;	// für die Anzeige von ComboBox

	//--- für PDF
	private String wordBefore;
	private String spaltenArray; // zum Speichern der Spalten-Werte

	private int spalteDatum;
	private int spalteText;
	private int spalteSoll;
	private int spalteHaben;
	private int anzahlSpalten = 0; // die höchste Nummer der Spalten


	public CsvCompany() {
	}

	public CsvCompany(String companyName, String kontoNrDefault, String dirPath, int docType) {
		this.companyName = companyName;
		this.kontoNrDefault = kontoNrDefault;
		this.dirPath = dirPath;
		this.docType = docType;
	}

	// ---- setter und getter
	public int getCompanyID() {
		return companyID;
	}

	public void setCompanyID(int companyID) {
		this.companyID=companyID;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getKontoNrDefault() {
		return kontoNrDefault;
	}

	public void setKontoNrDefault(String kontoNrDefault) {
		this.kontoNrDefault = kontoNrDefault;
	}

	public String getDirPath() {
		return dirPath;
	}

	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}

	/**
	 * @return the docType
	 */
	public int getDocType() {
		return docType;
	}

	/**
	 * @param docType the docType to set
	 */
	public void setDocType(int typeOfDoc) {
		this.docType = typeOfDoc;
		this.docString = docTypes[typeOfDoc-1];
	}

	/**
	 * @param docType the docType to set
	 */
	public void setDocType(String typeOfDoc) {
		if (typeOfDoc.startsWith("CSV")) {
			this.docType = 1;
		}
		else this.docType = 2;
	}

	/**
	 * @return the docString
	 */
	public String getDocString() {
		return docString;
	}

	/**
	 * @param docString the docString to set
	 */
	public void setDocString(String docString) {
		if (docString.startsWith("CSV")) {
			this.docType = 1;
		}
		else this.docType = 2;
		this.docString = docString;
	}

	/**
	 * @return the wordBefore
	 */
	public String getWordBefore() {
		return wordBefore;
	}

	/**
	 * @param wordBefore the wordBefore to set
	 */
	public void setWordBefore(String wordBefore) {
		this.wordBefore = wordBefore;
	}

	/**
	 * @return the spalteDatum
	 */
	public int getSpalteDatum() {
		return spalteDatum;
	}

	/**
	 * @param spalteDatum the spalteDatum to set
	 */
	public void setSpalteDatum(int spalteDatum) {
		this.spalteDatum = spalteDatum;
	}

	/**
	 * @param spalteDatum the spalteDatum to set
	 */
	public void setSpalteDatum(String spalteDatum) {
		this.spalteDatum = parse(spalteDatum);
	}

	/**
	 * @return the spalteText
	 */
	public int getSpalteText() {
		return spalteText;
	}

	/**
	 * @param spalteText the spalteText to set
	 */
	public void setSpalteText(int spalteText) {
		this.spalteText = spalteText;
	}

	/**
	 * @param spalteText the spalteText to set
	 */
	public void setSpalteText(String spalteText) {
		this.spalteText = parse(spalteText);
	}

	/**
	 * @return the spalteSoll
	 */
	public int getSpalteSoll() {
		return spalteSoll;
	}

	/**
	 * @param spalteSoll the spalteSoll to set
	 */
	public void setSpalteSoll(int spalteSoll) {
		this.spalteSoll = spalteSoll;
	}

	/**
	 * @param spalteSoll the spalteSoll to set
	 */
	public void setSpalteSoll(String spalteSoll) {
		this.spalteSoll = parse(spalteSoll);
	}


	/**
	 * @return the spalteHaben
	 */
	public int getSpalteHaben() {
		return spalteHaben;
	}

	/**
	 * @param spalteHaben the spalteHaben to set
	 */
	public void setSpalteHaben(int spalteHaben) {
		this.spalteHaben = spalteHaben;
	}

	/**
	 * @param spalteHaben the spalteHaben to set
	 */
	public void setSpalteHaben(String spalteHaben) {
		this.spalteHaben = parse(spalteHaben);
	}


	/**
	 * @return the spaltenArray
	 */
	public String getSpaltenArray() {
		StringBuffer buffer = new StringBuffer(20);
		buffer.append(spalteDatum);
		buffer.append(",");
		buffer.append(spalteText);
		buffer.append(",");
		buffer.append(spalteSoll);
		buffer.append(",");
		buffer.append(spalteHaben);
		spaltenArray = buffer.toString();
		return spaltenArray;
	}

	/**
	 * @param spaltenArray the spaltenArray to set
	 * wird von der DB aufgerufen
	 */
	public void setSpaltenArray(String spaltenArray) {
		this.spaltenArray = spaltenArray;
		// die Werte auslesen
		if (spaltenArray == null) {
			return;
		}
		String[] words = spaltenArray.split(",");
		for (int i = 0; i < words.length; i++) {
			switch(i) {
			case 0:
				setSpalteDatum(parse(words[i]));
				break;
			case 1:
				setSpalteText(parse(words[i]));
				break;
			case 2:
				setSpalteSoll(parse(words[i]));
				break;
			case 3:
				setSpalteHaben(parse(words[i]));
				break;
			}
		}
	}

	/**
	 * Von String zu int, setzt die max. anzahl Spalten
	 * die beim einlesen einer PDF-Zeile vorhanden sein muss.
	 * @param value
	 * @return int, -1 wenn Fehler
	 */
	private int parse(String value) {
		try {
			int i = Integer.parseInt(value);
			if (i > anzahlSpalten) {
				anzahlSpalten = i;
			}
			return i;
		}
		catch (NumberFormatException ex) {
			return -1;
		}
	}

	/**
	 * @return the anzahlSpalten
	 */
	public int getAnzahlSpalten() {
		return anzahlSpalten;
	}



}
