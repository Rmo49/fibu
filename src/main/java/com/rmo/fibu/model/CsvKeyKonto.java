package com.rmo.fibu.model;

/**
 * Datenstruktur der Keywords für das einlesen von CSV-files.
 * @author Ruedi
 *
 */
public class CsvKeyKonto {

	private int Id;
	private int companyId;
	private String suchWort;
	private String kontoNr;
	private String sh;
	private String textNeu;

	public CsvKeyKonto() {
	}

	public CsvKeyKonto(int companyId, String suchWort, String kontoNr, String sh, String textNeu) {
		this.companyId = companyId;
		this.suchWort = suchWort;
		this.kontoNr = kontoNr;
		this.sh = sh;
		this.textNeu = textNeu;
	}

	// ---- setter und getter

	public int getId() {
		return Id;
	}

	public void setId(int Id) {
		this.Id = Id;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public String getSuchWort() {
		return suchWort;
	}

	public void setSuchWort(String suchWort) {
		this.suchWort = suchWort;
	}

	public String getKontoNr() {
		return kontoNr;
	}

	public void setKontoNr(String kontoNr) {
		this.kontoNr = kontoNr;
	}

	public String getSh() {
		return sh;
	}

	public void setSh(String sh) {
		this.sh = sh;
	}

	public String getTextNeu() {
		return textNeu;
	}

	public void setTextNeu(String textNeu) {
		this.textNeu = textNeu;
	}
}
