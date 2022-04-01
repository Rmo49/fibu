package com.rmo.fibu.model;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.rmo.fibu.exception.FibuException;

public class CsvParserPost extends CsvParserBase {

	// die Spalte des Datums
	private final int mDateCol = 0;	// das Format der Post
	// das Datumformat
	private static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	// die Spalte des Buchungtextes
	private final int mTextCol = 1;
	// die Spalte des Betrages
	private final int mBetragCol = 2;

	
	
	public CsvParserPost(File file) {
		super(file);
	}
	
	@Override
	public char getSplitChar() {
		return ';';
	}
	
	/**
	 * Buchungstest lesen, diesen evt. anpassen.
	 * @param buchung
	 */
	@Override
	protected void readBuchungsText(BuchungCsv buchung) {
		if (lineValues[mTextCol] != null && lineValues[mTextCol].length() > 0) {
			buchung.setText(checkText(lineValues[mTextCol]));
		}
	}

	
	/**
	 * Buchungstext optimieren
	 */
	@Override
	protected String checkText(String text) {
		// Kartennummer ausblenen
		int posX = text.indexOf("XXXX", 0);
		if (posX > 0) {
			text = text.substring(posX + 9, text.length());
		}
//		text = setGrossKlein(text.trim());
		return text;
	}

	/**
	 * Die Spalten-Nummer des Datums
	 */
	@Override
	protected int getDateCol() {
		return mDateCol;
	}
	
	/**
	 * Das verwendete Datumsformat.
	 */
	@Override
	protected DateFormat getDateFormat() {
		return mDateFormat;
	}

	/**
	 * Betrag, weiss ober Gut oder Lastschrift setzt auch ob Soll oder Haben
	 * @param buchung
	 */
	@Override
	protected void readBetrag(BuchungCsv buchung) {
		if (lineValues[mBetragCol] != null && lineValues[mBetragCol].length() > 0) {
			buchung.setBetrag(removeTrennzeichen(lineValues[mBetragCol]));
			buchung.setSoll(getKontoNrDefault());
		}
		if (lineValues[mBetragCol+1] != null && lineValues[mBetragCol+1].length() > 0) {			
			// ist eine Lastschrift
			buchung.setBetrag(removeTrennzeichen(lineValues[mBetragCol+1]));
			buchung.setHaben(getKontoNrDefault());
		}
	}
	
	/**
	 * Die ID der Company
	 */
	@Override
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
	@Override
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
	@Override
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
