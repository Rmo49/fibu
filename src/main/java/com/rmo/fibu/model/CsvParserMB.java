package com.rmo.fibu.model;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Parser für Migrosbank
 */
public class CsvParserMB extends CsvParserBase {

	// der Splitchar
	private final char mSplitChar = ';';
	// die Spalte des Datums
	private final int mDateCol = 0;
	// das Datumfromat
	private static SimpleDateFormat mDateFormat = new SimpleDateFormat("dd.MM.yy");
	// die Spalte des Buchungtextes
	private final int mTextCol = 1;
	// Buchnungstext löschen
	private String[] textToDelete = {"Karte:"};
	// die Spalte des Betrages
	private final int mBetragCol = 4;
	// wenn Komma dann durch Punkt ersetzen
	private final char komma = ',';
	private final char punkt = '.';

	public CsvParserMB(File file, Date von, Date bis) {
		super(file, von, bis);
	}

	@Override
	public char getSplitChar() {
		return mSplitChar;
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
	 * Den Teil nach dem gesuchten textToDelete entfernen
	 * Sonst Leezeichen entfernen
	 */
	@Override
	protected String checkText(String text) {
		for (String textDel : textToDelete) {
			int pos = text.indexOf(textDel);
			if (pos > 0) {
				// den Bereich nach pos entfernen
				text = text.substring(0, pos);
			}
		}
		text = text.trim();

//		text = setGrossKlein(text.trim());
		return text;
	}


	/**
	 * Betrag, weiss ober Gut oder Lastschrift setzt auch ob Soll oder Haben
	 * @param buchung
	 */
	@Override
	protected void readBetrag(BuchungCsv buchung) {
		if (lineValues.length < mBetragCol+1) {
			buchung.setBetrag("check col");
			return;
		}
		if (lineValues[mBetragCol] != null && lineValues[mBetragCol].length() > 1) {
			String betrag = lineValues[mBetragCol];
			// wenn -xx dann Belastung
			if (betrag.contains("-")) {
				// ist eine Belastung
				betrag = betrag.substring(1, betrag.length());
				buchung.setHaben(mCompany.getKontoNrDefault());
			}
			else {
				// ist eine Gutschrift
				buchung.setSoll(mCompany.getKontoNrDefault());
			}
			// wenn , durch punkt ersetzen
			betrag = betrag.replace(komma, punkt);
			buchung.setBetrag(betrag);
		}
	}


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
