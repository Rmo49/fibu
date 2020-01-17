package com.rmo.fibu.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.rmo.fibu.util.Trace;



/**
 * CSV-Parser, liest den Text von csv file.
 * Nach Initialisierung mit File, liest die erste Buchung mit nextBuchung.
 * @author Ruedi
 */
public abstract class CsvParserBase {

	// Name der bisher implementierten Parser.
	static public String	companyNamePost = "Post";
	static public String	companyNameCS = "CS";
	static private String	buchungFalsch = ">>> Fehlerhafte Buchung in CSV file <<<";

	// file von dem gelesen werden soll
	protected CSVReader			reader = null;
//	protected BufferedReader	br = null;
	protected String 			line = "";
	protected String[] 			values = null;
	protected String 			csvSplitBy = ";";
	protected StringBuffer		textBuchung = null;
	protected StringBuffer 		pdfText = null;
	// Naechste position ab der gelesen werden soll
	protected String 			textBetrag = null;
	// die encoding standad-Strings
	protected static final String ANSI_CODE = "windows-1252";
	protected static final String UTF_CODE = "UTF8";

	public CsvParserBase (File file) {
		try {
			final CSVParser parser = new CSVParserBuilder()
					.withSeparator(getSplitChar())
					.withIgnoreQuotations(false)
					.build();
			
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), getEncoding()));
			
			reader = new CSVReaderBuilder(in)
					.withSkipLines(0)
					.withCSVParser(parser)
					.build();
//			reader = new CSVReader(new FileReader(file), getSplitChar());
		}
		catch (Exception ex) {
			// sollte nie passieren, da selektiert von Liste
		}
	}
	
	/**
	 * für Iterator, true wenn noch eine weitere Buchung vorhanden ist.
	 * Liest die nächste Zeile in den values[] Array.
	 * @return
	 */
	private boolean hasNext() {
		try {
			if ((values = reader.readNext()) != null) {
				return true;
			}
			return false;
		}
		catch (IOException ex) {
			return false;
		}
	}


	/** Die nächste Buchung einlesen, liest so lange, bis eine Zeile mit einem Datum gefunden.
	 * 
	 * @return BuchungPdf, if BuchungPdf.datum = "" dann keine gültige Buchung, aber Zeile gefunden
	 * @return null, wenn keine Zeile mehr vorhanden
	 */
	public BuchungCsv nextBuchung() {
		BuchungCsv buchungPdf = new BuchungCsv();
		boolean datumOk = false;
		boolean hasNext = false;
		try {
			while (hasNext = hasNext()) {
				if (readDatum(buchungPdf)) {
					datumOk = true;
					break;
				}
				else {
					datumOk = false;
				}
			}
			if (datumOk) {
				if (values.length < 4) {
					buchungPdf.setText(buchungFalsch);
					buchungPdf.setBetrag("0");
					buchungPdf.setHaben(getKontoNrDefault());		
				}
				else {
					readBuchungsText(buchungPdf);
					readBetrag(buchungPdf);
				}
			}
			else {
				// wenn kein Datum, dann keine Buchung
				buchungPdf.setDatum("");
			}
		}
		catch (Exception ex) {
			Trace.println(0, "CsvParser, Fehler: " + ex.getMessage());
		}
		if (!hasNext) {
			buchungPdf = null;
		}
		return buchungPdf;
	}

	/**
	 * Vom text lesen, bis ein datum gefunden.
	 */
	private boolean readDatum(BuchungCsv buchung) {
		Date datum;
		try {
			datum = getDatum(values[0]);
		} catch (ParseException pe) {
			// weitermachen, bis Datum gefunden
			return false;
		}
		buchung.setDatum(datum);
		return true;
	}

	
	/**
	 * lese das Datum.
	 * Wenn kein Datum wird eine Exception geworfen, sonst das Date zurückgegeben
	 */
	private Date getDatum(String datum) throws ParseException {
		if (datum.length() < 8) {
			throw new ParseException("Text zu kurz", 1);
		}
	    //dateFormat.setLenient(false);
	    return getDateFormat().parse(datum.trim());
	}
	
	/**
	 * Der Buchungstext, mit gross und kleinschreibung
	 * @param buchung
	 * @return
	 */
	private void readBuchungsText (BuchungCsv buchung) {
		if (values[1] != null && values[1].length() > 0) {
			buchung.setText(checkText(values[1]));
		}
	}

	/** Aendert den String in Gross und Kleinbuchstaben
	 * @param text
	 * @return String mit Gross und Kleinbuchstaben
	 */
	protected String setGrossKlein(String text) {
		StringBuffer txt = new StringBuffer(text);
		boolean startWord = true;
		int i = 0;
		while (i < txt.length()) {
			if (startWord) {
				if (i+2 >= txt.length()) {
					// letzte 2 char, nichts machen
					break;
				}
				else if (i+2 < txt.length() && txt.charAt(i+2) == ' ') {
					i++;
				}
				startWord = false;
			}
			else if (txt.charAt(i) == ' ') {
				startWord = true;
			}
			else if (txt.charAt(i) == ';') {
				txt.deleteCharAt(i);
			}
			else {
				txt.setCharAt(i, Character.toLowerCase(txt.charAt(i)));
			}
			i++;
		}
		return txt.toString();
	}
	
	/**
	 * Das Datumsformat der jeweiligen Implementation
	 */
	protected abstract DateFormat getDateFormat();
	
	/**
	 * Betrag, weiss ober Gut oder Lastschrift setzt auch ob Soll oder Haben
	 * @param buchung
	 */
	protected abstract void readBetrag(BuchungCsv buchung);

	/**
	 * Die ID der Company
	 */
	protected abstract int getCompanyId();

	/**
	 * Die standard KontoNr
	 */
	protected abstract String getKontoNrDefault();

	/**
	 * Buchungstext muss von Impl. speziell behandelt werden.
	 * @param text
	 * @return
	 */
	protected abstract String checkText(String text);
	
	/**
	 * Den encoding string, muss von CS oder Postfinance gesetzt werden.
	 * Siehe auch encoding standard strings
	 * Wenn null zurückgegeben, dann ist Windows-Standard.
	 * @return
	 */
	protected abstract String getEncoding();
	
	/**
	 * The split character of csv file.
	 * @return
	 */
	public abstract char getSplitChar();
	
}
