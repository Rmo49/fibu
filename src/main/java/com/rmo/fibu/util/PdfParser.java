package com.rmo.fibu.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.rmo.fibu.model.BuchungCsv;
import com.rmo.fibu.model.CsvBank;


/**
 * Parsed das PDF Dokument
 * @author ruedi
 *
 */
public class PdfParser {

	private CsvBank mBank;
	// Datum von bis 
	private Date mDateVon = null;
	private Date mDateBis = null;

	// enthält das gesamte Dokument
	private PdfDokument pdfDoku;

	// die Spalte des Datums
	private final int mDateCol = 1;
	// das Format der Raiffeisen
	private final SimpleDateFormat mDateFormat = new SimpleDateFormat("dd.MM.yy");
	// die Spalte des Buchungtextes


	/**
	 * Initialisierung des Parsers, liest das PDF-Dokument.
	 * @param file
	 */
	public PdfParser(File file,  Date von, Date bis) {
		mDateVon = von;
		mDateBis = bis;
		PdfWordStripper stripper = pdfStripWords(file);
		// enthält das pdf-Dokument
		pdfDoku = new PdfDokument(stripper.pdfWords);
	}

	/**
	 * Startet das parsing, gibt alle Buchungen zurück
	 * @return
	 */
	public List<BuchungCsv> startParsing(CsvBank bank) {
		this.mBank = bank;

		return readAllBuchungen();
	}


	/**
	 * PDF mit PDFBox File öffnen und Stripper mit allen Worte zurückgeben
	 */
	public static PdfWordStripper pdfStripWords(File file) {
		PdfWordStripper stripper = null;
		if (!file.exists()) {
			return null;
		}
		try {
			PDDocument document = PDDocument.load(file);
			stripper = new PdfWordStripper();
			stripper.setSortByPosition(true);
			stripper.setStartPage(0);
			stripper.setEndPage(document.getNumberOfPages());
			Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
			// hier werden die Worte in den Stripper geschrieben
			stripper.writeText(document, dummy);
		} catch (Exception ex) {
			Trace.println(1, "error: " + ex.getMessage());
		}
		return stripper;
	}


	/**
	 * Alle Buchungen lesen.
	 * @param wordBefore
	 * @return
	 */
	private List<BuchungCsv> readAllBuchungen() {
		if (mBank.getBankName().compareToIgnoreCase("Cumulus") == 0) {
			return readBuchungenCumulus();
		}
		pdfDoku.gotoStart(mBank.getWordBefore());
		pdfDoku.gotoNextLine();
		List<BuchungCsv> buchungen = new ArrayList<>();
		// die eingelesene Zeile
		List<String> pdfZeile = pdfDoku.nextLine();
		while (pdfZeile.size() > 0)  {
			BuchungCsv buchung = makeBuchung(pdfZeile);
			if (buchung != null) {
				buchungen.add(buchung);
			}
			pdfZeile = pdfDoku.nextLine();
		}
		return buchungen;
	}

	/**
	 * Die Buchungen von Cumulus lesen
	 */
	private List<BuchungCsv> readBuchungenCumulus() {
		pdfDoku.gotoStart(mBank.getWordBefore());
		pdfDoku.gotoNextLine();
		List<BuchungCsv> buchungen = new ArrayList<>();
		// die eingelesene Zeile
		List<String> pdfZeile = pdfDoku.nextLine();
		while (pdfZeile.size() > 0)  {
			BuchungCsv buchung = makeBuchungCumulus(pdfZeile);
			if (buchung != null) {
				buchungen.add(buchung);
			}
			pdfZeile = pdfDoku.nextLine();
		}
		// pdfDoku.close(); Wie closen? (hatte Fehlermeldung)
		return buchungen;
		
	}


	/**
	 * Von einer Zeile die Buchungen auslesen.
	 * Spaltennummer beginnt mit 0, im Setup mit 1
	 * @return BuchungCsv eine Buchung
	 */
	private BuchungCsv makeBuchung(List <String> pdfZeile) {
		if (pdfZeile.size() >= mBank.getAnzahlSpalten()) {
			BuchungCsv buchung = new BuchungCsv();
			String datum = pdfZeile.get(mBank.getSpalteDatum()-1);
			if (isDatum(datum)) {
				buchung.setDatum(datum);
				buchung.setText(pdfZeile.get( mBank.getSpalteText()-1) );
				// TODO wenn soll und haben unterschiedlich
				buchung.setBetrag(pdfZeile.get( mBank.getSpalteSoll()-1) );
				buchung.setHaben(mBank.getKontoNrDefault());
			}
			else {
				// wenn kein Datum, dann keine Buchung
				return null;
			}
			return buchung;
		}
		return null;
	}

	
	/**
	 * Von einer Zeile die Buchungen auslesen.
	 * , speziell für Cumulus
	 * Spaltennummer beginnt mit 0, im Setup mit 1
	 * @return BuchungCsv eine Buchug
	 */
	private BuchungCsv makeBuchungCumulus(List <String> pdfZeile) {
		// TODO allgemein für pdf definieren, 
		if (pdfZeile.size() >= mBank.getAnzahlSpalten()) {
			BuchungCsv buchung = new BuchungCsv();
			String datum = pdfZeile.get(mBank.getSpalteDatum()-1);
			if (isDatum(datum)) {
				buchung.setDatum(datum);
				buchung.setText(pdfZeile.get( mBank.getSpalteText()-1) );
				// TODO wenn soll und haben unterschiedlich
				int spalteSoll = mBank.getSpalteSoll()-1;
				String betrag = pdfZeile.get(spalteSoll);
				if (isText(betrag)) {
					spalteSoll++; spalteSoll++;
				}
				buchung.setBetrag(pdfZeile.get(spalteSoll));
				buchung.setHaben(mBank.getKontoNrDefault());
			}
			else {
				// wenn kein Datum, dann keine Buchung
				return null;
			}
			return buchung;
		}
		return null;
	}

	/**
	 * Ist der String ein datum?
	 * Hier könnte geprüft werden, ob Datum im gesuchten Range liegt
	 * wenn ausserhalb, dann wir false zurück gegeben.
	 * @param datum
	 * @return true wenn das ein Datum ist, oder wenn im Range von ... bis liegt
	 */
	private boolean isDatum(String datum) {
		if (datum.length() < 8) {
			datum = datum + "24";
//			return false;
		}
	    mDateFormat.setLenient(false);
	    Date lDate;
	    try {
	      lDate = mDateFormat.parse(datum.trim());
	    } catch (ParseException pe) {
	      return false;
	    }
	    if (lDate.before(mDateVon) || lDate.after(mDateBis)) {
	    	return false;
	    }
	    return true;
	}

	/**
	 * prüfen, ob der String einen Text enthält
	 * @param betrag
	 */
	private boolean isText(String betrag) {
		if (Pattern.matches("[a-zA-Z]+", betrag)) {
			return true;
		}
		return false;
	}

	/**
	 * Die Spalten-Nummer des Datums
	 */
	protected int getDateCol() {
		return mDateCol;
	}

	/**
	 * Das verwendete Datumsformat.
	 */
	protected DateFormat getDateFormat() {
		return mDateFormat;
	}


}
