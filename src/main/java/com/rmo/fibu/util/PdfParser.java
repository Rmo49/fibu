package com.rmo.fibu.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.rmo.fibu.model.BuchungCsv;
import com.rmo.fibu.model.CsvCompany;


/**
 * Parsed das PDF Dokument
 * @author ruedi
 *
 */
public class PdfParser {

	private CsvCompany company;
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
	public PdfParser(File file) {
			PdfWordStripper stripper = pdfStripWords(file);
			// enthält das pdf-Dokument
			pdfDoku = new PdfDokument(stripper.pdfWords);
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
			System.out.println(ex.getMessage());
		}
		return stripper;
	}


	/**
	 * Startet das parsing, gibt Buchungen zurück
	 * @return
	 */
	public List<BuchungCsv> startParsing(CsvCompany company) {
		this.company = company;

		return readAllBuchungen();
	}

	/**
	 * Alle Buchungen lesen.
	 * @param wordBefore
	 * @return
	 */
	private List<BuchungCsv> readAllBuchungen() {
		pdfDoku.gotoStart(company.getWordBefore());
		pdfDoku.gotoNextLine();
		List<BuchungCsv> buchungen = new ArrayList<>();
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
	 * Von einer Zeile die Buchungen auslesen
	 * @return
	 */
	private BuchungCsv makeBuchung(List <String> pdfZeile) {
		if (pdfZeile.size() >= company.getAnzahlSpalten()) {
			BuchungCsv buchung = new BuchungCsv();
			String datum = pdfZeile.get(company.getSpalteDatum()-1);
			if (isDatum(datum)) {
				buchung.setDatum(datum);
				buchung.setText(pdfZeile.get( company.getSpalteText()-1) );
				// TODO wenn soll und haben unterschiedlich
				buchung.setBetrag(pdfZeile.get( company.getSpalteSoll()-1) );
				buchung.setHaben(company.getKontoNrDefault());
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
	 * @param datum
	 * @return true wenn das ein Datum ist.
	 */
	private boolean isDatum(String datum) {
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
