package com.rmo.fibu.model;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CsvParserCs extends CsvParserBase {

	// die Spalte des Datums
	private final int mDateCol = 0;
	// das Datumfromat
	private static SimpleDateFormat mDateFormat = new SimpleDateFormat("dd.MM.yy");
	// die Spalte des Buchungtextes
	private final int mTextCol = 1;
	// Buchnungstext löschen
	private String[] textToDelete = {"Vergütung", "Maestro", "Dauerauftrag", "Einzug"};
	// die Spalte des Betrages
	private final int mBetragCol = 2;

	public CsvParserCs(File file) {
		super(file);
	}

	@Override
	public char getSplitChar() {
		return ',';
	}

	/**
	 * Die Spalten-Nummer des Datums
	 */
	@Override
	protected int getDateCol() {
		return mDateCol;
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
	 * Das verwendete Datumsformat.
	 */
	@Override
	protected DateFormat getDateFormat() {
		return mDateFormat;
	}

	/**
	 * Erster Teil bis zum ersten Komma entfernen, wenn bestimmte Werte enthält.
	 * Sonst Leezeichen entfernen
	 */
	@Override
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
//		text = setGrossKlein(text.trim());
		return text;
	}

	/**
	 * Untersucht ob Text enthält der als Konstante definiert wurde.
	 * @param text
	 * @return
	 */
	private boolean hasTextToDelete(String text){
		for (String element : textToDelete) {
			if (text.startsWith(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Betrag, weiss ober Gut oder Lastschrift setzt auch ob Soll oder Haben
	 * @param buchung
	 */
	@Override
	protected void readBetrag(BuchungCsv buchung) {
		// könnte auch "-" enthalten, dann leer
		if (lineValues[mBetragCol] != null && lineValues[mBetragCol].length() > 1) {
			// ist eine Belastung
			buchung.setBetrag(lineValues[mBetragCol]);
			buchung.setHaben(mCompany.getKontoNrDefault());
		}
		if (lineValues[mBetragCol+1] != null && lineValues[mBetragCol+1].length() > 1) {
			// ist eine Gutschrift
			buchung.setBetrag(lineValues[mBetragCol+1]);
			buchung.setSoll(mCompany.getKontoNrDefault());
		}
	}

	/**
	 * Die ID der Company
	 */
//	@Override
//	protected int getCompanyId() {
//		CsvCompanyData companyData = (CsvCompanyData) DataBeanContext.getContext().getDataObject(CsvCompanyData.class);
//		try {
//			return companyData.readData(CsvParserBase.companyNameCS).getCompanyID();
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
//			return companyData.readData(CsvParserBase.companyNameCS).getKontoNrDefault();
//		}
//		catch (FibuException ex){
//			// do nothing
//		}
//		return "";
//	}

	/**
	 * Den encoding string, muss von CS oder Postfinance gesetzt werden.
	 * Siehe auch encoding standard strings
	 * Wenn null zurückgegeben, dann ist Windows-Standard.
	 * @return
	 */
	@Override
	protected String getEncoding() {
		return UTF_CODE;
	}


}
