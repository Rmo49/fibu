package com.rmo.fibu.model;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.rmo.fibu.exception.FibuException;

public class CsvParserPost extends CsvParserBase {

	// das Format der Post
	private static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public CsvParserPost(File file) {
		super(file);
	}
	
	@Override
	public char getSplitChar() {
		return ';';
	}
	
	
	/**
	 * 
	 */
	protected String checkText(String text) {
		// Kartennummer ausblenen
		int posX = text.indexOf("XXXX", 0);
		if (posX > 0) {
			text = text.substring(posX + 9, text.length());
		}
		text = setGrossKlein(text.trim());
		return text;
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
		if (values[2] != null && values[2].length() > 0) {
			buchung.setBetrag(removeTrennzeichen(values[2]));
			buchung.setSoll(getKontoNrDefault());
		}
		if (values[3] != null && values[3].length() > 0) {			
			// ist eine Lastschrift
			buchung.setBetrag(removeTrennzeichen(values[3]));
			buchung.setHaben(getKontoNrDefault());
		}
	}

	/**
	 * Die führenden + und - und das Tausender Trennzeichen entfernen.
	 * @param betrag
	 * @return
	 */
	private String removeTrennzeichen(String betrag) {
		if (betrag.startsWith("+") || betrag.startsWith("-")) {
			betrag = betrag.substring(1, betrag.length());
		}
		int posTausend = betrag.indexOf("'");
		if (posTausend > 0) {
			betrag = betrag.substring(0,posTausend) + betrag.substring(posTausend+1, betrag.length());
		}
		return betrag;
	}
	
	/**
	 * Die ID der Company
	 */
	protected int getCompanyId() {
		CsvCompanyData companyData = (CsvCompanyData) DataBeanContext.getContext().getDataObject(CsvCompanyData.class);
		try {
			return companyData.readData(CsvParserBase.companyNamePost).getCompanyID();
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
			return companyData.readData(CsvParserBase.companyNamePost).getKontoNrDefault();
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
		return ANSI_CODE;
	}


	/**
	 * Ist der String ein datum?
	 * @param datum
	 * @return true wenn das ein Datum ist.
	 */
	protected boolean isDatum(String datum) {
		if (datum.length() < 8) {
			return false;
		}
	    mDateFormat.setLenient(false);
	    try {
	      mDateFormat.parse(datum.trim());
	    } catch (ParseException pe) {
	      return false;
	    }
	    return true;
	}

}
