package com.rmo.fibu.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.rmo.fibu.model.BuchungCsv;

public class PdfParserCler extends PdfParser {

	// das Format des Datums
	private final SimpleDateFormat mDateFormat = new SimpleDateFormat("dd.MM.yy");
	private double mLastSaldo = 0.0;

	public PdfParserCler(File file, Date von, Date bis) {
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
		while (pdfZeile.size() > 0) {
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
	 * Von einer Zeile die Buchungen auslesen. Spaltennummer beginnt mit 0, im Setup
	 * mit 1
	 *
	 * @return BuchungCsv eine Buchung
	 */
	private BuchungCsv makeBuchung(List<String> pdfZeile) {
		if (pdfZeile.size() >= mBank.getAnzahlSpalten() - 1) {
			// wenn alle Spalten eingelesen
			BuchungCsv buchung = new BuchungCsv();
			String datum = pdfZeile.get(mBank.getSpalteDatum() - 1);
			String newSaldo = "0";
			if (isDatum(datum)) {
				buchung.setDatum(datum);
				buchung.setText(pdfZeile.get(mBank.getSpalteText() - 1));
				// Spezial, da einige Buchungen nur 4 Spalten
				if (pdfZeile.size() <= 4) {
					buchung.setBetrag(pdfZeile.get(mBank.getSpalteSoll() - 2));
					if (mBank.getSpalteSaldo() > 0) {
						newSaldo = pdfZeile.get(mBank.getSpalteSaldo() - 2);
					}
				} else {
					buchung.setBetrag(pdfZeile.get(mBank.getSpalteSoll() - 1));
					if (mBank.getSpalteSaldo() > 0) {
						newSaldo = pdfZeile.get(mBank.getSpalteSaldo() - 1);
					}
				}
				// Tausender entfernen
				buchung.setBetrag(betragClean(buchung.getBetrag()));

				// bestimmen ob Soll oder Haben
				if (mBank.getSpalteSaldo() > 0) {
					if (zunahme(newSaldo) > 0) {
						buchung.setSoll(mBank.getKontoNrDefault());
					} else {
						buchung.setHaben(mBank.getKontoNrDefault());
					}
				} else {
					buchung.setHaben(mBank.getKontoNrDefault());
				}
			} else {
				// wenn kein Datum, dann keine Buchung
				return null;
			}
			return buchung;
		}
		return null;
	}

	/**
	 * Vergleicht mit den alten Saldo und bestimmt wieviel Zunahme
	 *
	 * @param newSaldo
	 * @return
	 */
	private double zunahme(String newSaldo) {
		double neueSaldo = 0.0;
		try {
			neueSaldo = Double.parseDouble(betragClean(newSaldo));

		} catch (NumberFormatException ex) {
			// TODO wenn Fehler wie melden?
			mLastSaldo = 0.0;
			return mLastSaldo;
		}
		double diff = neueSaldo - mLastSaldo;
		mLastSaldo = neueSaldo;
		return diff;
	}


	/**
	 * Das verwendete Datumsformat.
	 */
	@Override
	protected DateFormat getDateFormat() {
		return mDateFormat;
	}

}
