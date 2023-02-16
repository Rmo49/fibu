package com.rmo.fibu.model;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.rmo.fibu.exception.FibuException;

public class CsvParserRaiff extends CsvParserBase {
	
	// die Spalte des Datums
	private final int mDateCol = 1;
	// das Format der Raiffeisen
	private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	// die Spalte des Buchungtextes
	private final int mTextCol = 2;
	// die Spalte des Betrages
	private final int mBetragCol = 3;
	
	public CsvParserRaiff(File file) {
		super(file);
	}
	
	@Override
	public char getSplitChar() {
		return ';';
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
	 * Buchungstext anpassen
	 */
	@Override
	protected String checkText(String text) {
		// nach erstem Komma ausblenden
		int posX = text.indexOf(",", 0);
		if (posX > 0) {
			text = text.substring(0, posX);
		}
//		text = setGrossKlein(text.trim());
		return text;
	}

	/**
	 * Betrag, weiss ober Gut oder Lastschrift setzt auch ob Soll oder Haben
	 * @param buchung
	 */
	@Override
	protected void readBetrag(BuchungCsv buchung) {
		if (lineValues[mBetragCol] != null && lineValues[mBetragCol].length() > 0) {
			if (lineValues[mBetragCol].startsWith("-")) {
				buchung.setHaben(mCompany.getKontoNrDefault());
			}
			else {
				buchung.setSoll(mCompany.getKontoNrDefault());
			}
			buchung.setBetrag(removeTrennzeichen(lineValues[mBetragCol]));		
		}
	}
	
	/**
	 * Die ID der Company
	 */
//	@Override
//	protected int getCompanyId() {
//		CsvCompanyData companyData = (CsvCompanyData) DataBeanContext.getContext().getDataObject(CsvCompanyData.class);
//		try {
//			return companyData.readData(CsvParserBase.companyNameRaiff).getCompanyID();
//		}
//		catch (FibuException ex){
//			// do nothing
//		}
//		return 0;
//	}

	/**
	 * Die standard KontoNr
	 */
//	@Override
//	protected String getKontoNrDefault() {
//		CsvCompanyData companyData = (CsvCompanyData) DataBeanContext.getContext().getDataObject(CsvCompanyData.class);
//		try {
//			return companyData.readData(CsvParserBase.companyNameRaiff).getKontoNrDefault();
//		}
//		catch (FibuException ex){
//			// do nothing
//		}
//		return "";
//	}

	/**
	 * Den encoding string, muss von detail Implementation gesetzt werden.
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
