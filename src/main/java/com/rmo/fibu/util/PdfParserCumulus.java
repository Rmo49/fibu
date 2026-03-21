package com.rmo.fibu.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.rmo.fibu.model.BuchungCsv;

public class PdfParserCumulus extends PdfParser {

	// das Format des Datums
	protected final SimpleDateFormat mDateFormat = new SimpleDateFormat("dd.MM.yy");


	public PdfParserCumulus(File file, Date von, Date bis) {
		super(file, von, bis);
	}


	/**
	 * Die Buchungen von Cumulus lesen
	 */
	@Override
	protected List<BuchungCsv> readAllBuchungen() {
		pdfDoku.gotoSuchWort(mBank.getWordBefore());
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
		// pdfDoku.close(); Wie closen? (hatte Fehlermeldung)
		return buchungen;

	}

	/**
	 * Von einer Zeile die Buchungen auslesen.
	 * , speziell für Cumulus
	 * Spaltennummer beginnt mit 0, im Setup mit 1
	 * @return BuchungCsv eine Buchug
	 */
	private BuchungCsv makeBuchung(List <String> pdfZeile) {
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
	 * Das verwendete Datumsformat.
	 */
	@Override
	protected DateFormat getDateFormat() {
		return mDateFormat;
	}

}
