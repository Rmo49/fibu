package com.rmo.fibu.model;

/**
 * Die Company von der CSV-files eingelesen werden
 * @author Ruedi
 *
 */
public class CsvCompany {

	static final public int docTypeCsv = 1;
	static final public int docTypePdf = 2;
	
	private int companyID;
	private String companyName;
	private String kontoNrDefault;
	private String dirPath;
	private int typeOfDoc = 1;	// 1=default

	public CsvCompany() {
	}

	public CsvCompany(String companyName, String kontoNrDefault, String dirPath) {
		this.companyName = companyName;
		this.kontoNrDefault = kontoNrDefault;
		this.dirPath = dirPath;
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
	 * @return the typeOfDoc
	 */
	public int getTypeOfDoc() {
		return typeOfDoc;
	}

	/**
	 * @param typeOfDoc the typeOfDoc to set
	 */
	public void setTypeOfDoc(int typeOfDoc) {
		this.typeOfDoc = typeOfDoc;
	}


	

}
