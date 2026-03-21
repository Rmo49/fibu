package com.rmo.fibu.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.rmo.fibu.model.BuchungCsv;


/**
 * Parsed das PDF Dokument.
 * Das file und datum werden übergeben
 * Mit startParsing wird der Vorgang gestartet
 * @author ruedi
 *
 */
public abstract class PdfParser extends ParserBase {

	protected ParserBank mBank;
	// Datum von bis
	protected Date mDateVon = null;
	protected Date mDateBis = null;

	// enthält das gesamte Dokument
	protected PdfDokument pdfDoku;

	// die Spalte des Datums
	protected final int mDateCol = 1;
	// die Spalte des Buchungtextes


	/**
	 * Initialisierung des Parsers, liest das PDF-Dokument.
	 * @param file
	 */
	protected PdfParser(File file,  Date von, Date bis) {
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
	public List<BuchungCsv> startParsing(ParserBank bank) {
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
			PDDocument document = Loader.loadPDF(file);
			stripper = new PdfWordStripper();
			stripper.setSortByPosition(true);
			stripper.setStartPage(1);
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
	protected abstract List<BuchungCsv> readAllBuchungen();


	/**
	 * Ist der String ein datum?
	 * Hier könnte geprüft werden, ob Datum im gesuchten Range liegt
	 * wenn ausserhalb, dann wir false zurück gegeben.
	 * @param datum
	 * @return true wenn das ein Datum ist, oder wenn im Range von ... bis liegt
	 */
	protected boolean isDatum(String datum) {
		if (datum.length() < 8) {
			datum = datum + "24";
//			return false;
		}
	    getDateFormat().setLenient(false);
	    Date lDate;
	    try {
	      lDate = getDateFormat().parse(datum.trim());
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
	protected boolean isText(String betrag) {
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
	protected abstract DateFormat getDateFormat();


}
