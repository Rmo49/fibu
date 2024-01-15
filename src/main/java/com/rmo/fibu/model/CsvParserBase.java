package com.rmo.fibu.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	static public String	companyNameRaiff = "Raiffeisen";
	static private String	buchungFalsch = ">>> Fehlerhafte Buchung in CSV file <<<";

	protected CsvBank		mCompany = null;
	// Datum von bis 
	private Date mDateVon = null;
	private Date mDateBis = null;

	// file von dem gelesen werden soll
	
	protected CSVReader			reader = null;
//	protected BufferedReader	br = null;
	protected String 			line = "";
	protected String[] 			lineValues = null;
	protected String 			csvSplitBy = ";";
	protected StringBuffer		textBuchung = null;
	protected StringBuffer 		pdfText = null;
	// Naechste position ab der gelesen werden soll
	protected String 			textBetrag = null;
	// die encoding standad-Strings
	protected static final String ANSI_CODE = "windows-1252";
	protected static final String UTF_CODE = "UTF8";

	/**
	 * Prüft, ob Parser vorhanden
	 * @param companyName
	 * @return Fehlermeldung-String
	 */
	public static String parserVorhanden(String companyName) {
		boolean hatParser = false;
		if (companyName.equalsIgnoreCase(CsvParserBase.companyNamePost)) {
			hatParser = true;
			}
		if (companyName.equalsIgnoreCase(CsvParserBase.companyNameCS)) {
			hatParser = true;
		}
		if (companyName.equalsIgnoreCase(CsvParserBase.companyNameRaiff)) {
			hatParser = true;
		}
		StringBuffer sb = new StringBuffer(100);
		if (!hatParser) {
			sb.append("Kein Parser für: ");
			sb.append(companyName);
			sb.append("\n Implementationen vorhanden für: ");
			sb.append(CsvParserBase.companyNamePost);
			sb.append(", ");
			sb.append(CsvParserBase.companyNameCS);
			sb.append(", ");
			sb.append(CsvParserBase.companyNameRaiff);
		}
		return sb.toString();
	}

	/**
	 * Konstruktor, initialisiert gemeinsame Daten.
	 * @param file
	 */
	public CsvParserBase (File file, Date von, Date bis) {
		mDateVon = von;
		mDateBis = bis;
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
	 * Startet das parsing, gibt Buchungen zurück
	 * @return
	 */
	public List<BuchungCsv> startParsing(CsvBank bank) {
		this.mCompany = bank;
		List<BuchungCsv> buchungList = new ArrayList<>();

		BuchungCsv buchungCvs = nextBuchung();
		while (buchungCvs != null) {
			if (buchungCvs.getDatum().length() > 0) {
				buchungList.add(buchungCvs);
			}
			buchungCvs = nextBuchung();
		}

		return buchungList;
	}

	/**
	 * Die nächste Buchung einlesen, liest so lange, bis eine Zeile mit einem Datum gefunden.
	 *
	 * @return BuchungPdf, if BuchungPdf.datum = "" dann keine gültige Buchung, aber Zeile gefunden
	 * @return null, wenn keine Zeile mehr vorhanden
	 */
	public BuchungCsv nextBuchung() {
		BuchungCsv lBuchungCsv = new BuchungCsv();
		boolean datumOk = false;
		boolean hasNext = false;
		try {
			while (hasNext = hasNext()) {
				if (readDatum(lBuchungCsv)) {
					datumOk = true;
					break;
				}
				else {
					datumOk = false;
				}
			}
			if (datumOk) {
				if (lineValues.length < 4) {
					lBuchungCsv.setText(buchungFalsch);
					lBuchungCsv.setBetrag("0");
					lBuchungCsv.setHaben(mCompany.getKontoNrDefault());
				}
				else {
					readBuchungsText(lBuchungCsv);
					readBetrag(lBuchungCsv);
				}
			}
			else {
				// wenn kein Datum, dann keine Buchung
				lBuchungCsv.setDatum("");
			}
		}
		catch (Exception ex) {
			Trace.println(0, "CsvParser, Fehler: " + ex.getMessage());
			hasNext = false;
		}
		if (!hasNext) {
			lBuchungCsv = null;
		}
		return lBuchungCsv;
	}

	/**
	 * für Iterator, true wenn noch eine weitere Buchung vorhanden ist.
	 * Liest die nächste Zeile in den lineValues[] Array.
	 * @return
	 */
	private boolean hasNext() {
		try {
			if ((lineValues = reader.readNext()) != null) {
				return true;
			}
			return false;
		}
		catch (Exception ex) {
			return false;
		}
	}


	/**
	 * Vom text lesen, bis ein datum gefunden.
	 * Hier könnte der gesuchte Range geprüft werden 
	 */
	private boolean readDatum(BuchungCsv buchung) {
		Date datum;
		try {
			datum = getDatum(lineValues[getDateCol()]);
		} catch (ParseException pe) {
			// weitermachen, bis Datum gefunden
			return false;
		}
		// Range prüfen, wenn ausserhalb false zurückgeben
		if (datum.before(mDateVon) || datum.after(mDateBis)) {
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
	 * Die führenden + und - und das Tausender Trennzeichen entfernen.
	 * @param betrag
	 * @return
	 */
	protected String removeTrennzeichen(String betrag) {
		if (betrag.startsWith("+") || betrag.startsWith("-")) {
			betrag = betrag.substring(1, betrag.length());
		}
		int posTausend = betrag.indexOf("'");
		if (posTausend > 0) {
			betrag = betrag.substring(0,posTausend) + betrag.substring(posTausend+1, betrag.length());
		}
		return betrag;
	}

//------ Was implementiert werden muss -----------------------------------------

	/**
	 * Die Spalten-Nummer des Datums
	 */
	protected abstract int getDateCol();

	/**
	 * Das Datumsformat der jeweiligen Implementation
	 */
	protected abstract DateFormat getDateFormat();

	/**
	 * Buchungstest lesen, diesen evt. anpassen.
	 * @param buchung
	 */
	protected abstract void readBuchungsText(BuchungCsv buchung);

	/**
	 * Betrag, weiss ober Gut oder Lastschrift setzt auch ob Soll oder Haben
	 * @param buchung
	 */
	protected abstract void readBetrag(BuchungCsv buchung);


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
