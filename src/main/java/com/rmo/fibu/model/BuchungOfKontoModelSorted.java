package com.rmo.fibu.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.util.Trace;

/**
 * Die Buchungen eines Kontos.
 * Klasse hält einen Vektor mit allen Buchungen, die zu einem Konto gehören.
 */
public class BuchungOfKontoModelSorted extends BuchungOfKontoModel {

	private static final long serialVersionUID = -141586862566627222L;
	/** die Lise mit allen Buchungen eines Kontos */
	private List<BuchungRow> mBuchungen = new ArrayList<>();
	/** gibt an, ob das Konto ein SollKonto ist */
	private boolean mIsSoll = true;

public BuchungOfKontoModelSorted() {
}

@Override
public int getRowCount() {
	return mBuchungen.size();
}

/**
 * Die Liste aller Buchungen des Kontos
 * @return
 */
@Override
protected List<BuchungRow> getBuchungen() {
	return mBuchungen;
}

/** Gibt den Wert an der Koordinate row / col zurück.
 */
@Override
public Object getValueAt(int row, int col) {
	Trace.println(7, "KontoView.BuchungModel.getValueAt(" +row +',' + col +')');
	BuchungRow lRow = mBuchungen.get(row);
	switch (col) {
		case 0: return lRow.datum;
		case 1: return lRow.beleg;
		case 2: return lRow.text;
		case 3: return Integer.valueOf(lRow.gegenKonto);
		case 4: return Double.valueOf(lRow.sollBetrag);
		case 5: return Double.valueOf(lRow.habenBetrag);
		case 6: return Double.valueOf(lRow.saldo);
	}
	return "";
}

/** Setup the new display (Vector)
 *  @param int konto die kontonummer, die angezeigt werden soll
 *  @param Date from ab diesem Datum Buchungen anzeigen
 */
@Override
public void setup(int kontoNr, Date from) {
	Trace.println(3, "KontoView.BuchungModel.setup(" +kontoNr +"  " +from.toString() +")");

	if (! mBuchungen.isEmpty()) {
		fireTableDataChanged();
		mBuchungen.clear();
	}
	readAllBuchungen(kontoNr, from);
	mBuchungen.sort(new BuchungComparator());
	List<BuchungRow> buchungenNew = addZwischensumme();
	mBuchungen.clear();
	mBuchungen.addAll(buchungenNew);
	this.fireTableRowsInserted(0, mBuchungen.size()-1);
}

/**
 * Liest alle Buchungen in die Liste mBuchungen.
 */
private void readAllBuchungen(int kontoNr, Date from) {
	try {
		mStartSaldo = mKontoData.read(kontoNr).getStartSaldo();
		mShowStartLine = true;
		mSaldo = 0;
		mSummeSoll = 0;
		mSummeHaben = 0;
		mIsSoll = mKontoData.read(kontoNr).isSollKonto();
		mShowStartLine = false;
		//--- alle Buchungen addieren
		Buchung lBuchung = null;
		Iterator<Buchung> buchungIter = mBuchungData.getIterator();
		while (buchungIter.hasNext()) {
			lBuchung = buchungIter.next();
			// muss Buchung angezeigt werden?
			if (lBuchung.getSoll() == kontoNr || lBuchung.getHaben() == kontoNr) {
				if (lBuchung.getDatum().compareTo(from) >= 0) {
					if (mShowStartLine) {
						mBuchungen.add(rowStartSaldo(from));
						mShowStartLine = false;
					}
				    addSaldo(kontoNr, mIsSoll, lBuchung);
				    mBuchungen.add(rowValue(kontoNr, lBuchung));
				}
				else {
				    addSaldo(kontoNr, mIsSoll, lBuchung);
				}
			}
		}
	}
	catch (FibuException e) {}
}

/**
 * Alle Zwischensummen zu den bestehenden buchungen einfügen
 */
private List<BuchungRow> addZwischensumme() {
	List<BuchungRow> buchungenNew = new ArrayList<>();
	Iterator<BuchungRow> iter = mBuchungen.iterator();
	BuchungRow buchung = null;
	String tag1 = "";
	String[] tags = null;
	boolean ersteZeile = true;
	while (iter.hasNext()) {
		buchung = iter.next();
		buchung.saldo = 0.0;
		tags = buchung.text.split(",");
		// weitere Zeile gefunden
		if (tags[0].equalsIgnoreCase(tag1)) {
			buchung.saldo = -1;
			buchungenNew.add(buchung);
			addSortedSaldo(buchung);
		}
		// neue sequenz
		else {
			if (ersteZeile) {
				ersteZeile = false;
			}
			else {
				addTotalRow(tag1, buchungenNew);
			}
			tag1 = tags[0];
			buchung.saldo = -1;
			buchungenNew.add(buchung);
			mSaldo = 0;
			addSortedSaldo(buchung);
		}
	}
	if (! ersteZeile) {
		addTotalRow(tag1, buchungenNew);
	}
	return buchungenNew;
}


private void addTotalRow(String tag, List<BuchungRow> buchungen) {
	BuchungRow lRow = new BuchungRow();
	lRow.text = tag + ", Total";
	lRow.saldo = mSaldo;
	buchungen.add(lRow);
	lRow = new BuchungRow();
	buchungen.add(lRow);
}

/**
 * Den schleppenden Saldo berechnen
 * @param buchung
 */
private void addSortedSaldo(BuchungRow buchung) {
	if (mIsSoll) {
		if (buchung.sollBetrag > 0) {
			mSaldo += buchung.sollBetrag;
		}
		else {
			mSaldo -= buchung.habenBetrag;
		}
	}
	else {
		if (buchung.sollBetrag > 0) {
			mSaldo -= buchung.sollBetrag;
		}
		else {
			mSaldo += buchung.habenBetrag;
		}
	}
}

//----- Sortieren -------------------------

private class BuchungComparator implements Comparator <BuchungRow> {

	@Override
	public int compare(BuchungRow o1, BuchungRow o2) {
		String[] tag = o1.text.split(",");
		if (o2.text.startsWith(tag[0])) {
			return 0;
		}
		return o2.text.compareTo(tag[0]);
	}

}

}