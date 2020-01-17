package com.rmo.fibu.model;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.rmo.fibu.exception.FibuException;

public class CsvParserCs extends CsvParserBase {

	private static SimpleDateFormat mDateFormat = new SimpleDateFormat("dd.MM.yy");
	private String[] textToDelete = {"Vergütung", "Maestro", "Dauerauftrag", "Einzug"};
			
	public CsvParserCs(File file) {
		super(file);
	}
	
	@Override
	public char getSplitChar() {
		return ',';
	}

	/**
	 * Erster Teil bis zum ersten Komma entfernen, wenn bestimmte Werte enthält.
	 * Sonst Leezeichen entfernen
	 */
	protected String checkText(String text) {
		int komma = text.indexOf(",", 0);
		String firstText = null;
		if (komma > 0) {
			firstText = text.substring(0, komma);
		}
		else {
			firstText = text;
		}
		if (hasTextToDelete(firstText)) {
			text = text.substring(komma + 1, text.length());
		}
		else {
			firstText = firstText.trim();
			if (komma > 0) {
				text = firstText + " " + text.substring(komma + 1, text.length());
			}
		}
		text = setGrossKlein(text.trim());
		return text;
	}
	
	/**
	 * Untersucht ob Text enthält der als Konstante definiert wurde.
	 * @param text
	 * @return
	 */
	private boolean hasTextToDelete(String text){
		for (int i=0; i < textToDelete.length; i++) {
			if (text.startsWith(textToDelete[i])) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Das verwendete Datumsformat.
	 */
	protected DateFormat getDateFormat() {
		return mDateFormat;
	}

	/**
	 * Betrag, weiss ober Gut oder Lastschrift setzt auch ob Soll oder Haben
	 * @param buchung
	 */
	protected void readBetrag(BuchungCsv buchung) {
		// könnte auch "-" enthalten, dann leer
		if (values[2] != null && values[2].length() > 1) {
			// ist eine Belastung
			buchung.setBetrag(values[2]);
			buchung.setHaben(getKontoNrDefault());
		}
		if (values[3] != null && values[3].length() > 1) {
			// ist eine Gutschrift
			buchung.setBetrag(values[3]);
			buchung.setSoll(getKontoNrDefault());
		}
	}

	/**
	 * Die ID der Company
	 */
	protected int getCompanyId() {
		CsvCompanyData companyData = (CsvCompanyData) DataBeanContext.getContext().getDataObject(CsvCompanyData.class);
		try {
			return companyData.readData(CsvParserBase.companyNameCS).getCompanyID();
		}
		catch (FibuException ex){
			// do nothing
		}
		return 0;
	}

	/**
	 * Die standard KontoNr
	 */
	protected String getKontoNrDefault() {
		CsvCompanyData companyData = (CsvCompanyData) DataBeanContext.getContext().getDataObject(CsvCompanyData.class);
		try {
			return companyData.readData(CsvParserBase.companyNameCS).getKontoNrDefault();
		}
		catch (FibuException ex){
			// do nothing
		}
		return "";
	}

	/**
	 * Den encoding string, muss von CS oder Postfinance gesetzt werden.
	 * Siehe auch encoding standard strings
	 * Wenn null zurückgegeben, dann ist Windows-Standard.
	 * @return
	 */
	protected String getEncoding() {
		return UTF_CODE;
	}
	

}
