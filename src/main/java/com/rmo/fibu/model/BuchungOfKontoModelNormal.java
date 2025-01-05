package com.rmo.fibu.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

/**
 * Die Buchungen eines Kontos.
 * Klasse hält einen Vektor mit allen Buchungen, die zu einem Konto gehören.
 */
public class BuchungOfKontoModelNormal extends BuchungOfKontoModel {

	private static final long serialVersionUID = -141586862566627777L;
	/** die Liste mit allen Buchungen eines Kontos */
	private List<BuchungRow> mBuchungen = new ArrayList<>();
	/** der StartSaldo */

public BuchungOfKontoModelNormal() {
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


/** Setup the new display (Vector)
 *  @param int konto die kontonummer, die angezeigt werden soll
 *  @param Date from ab diesem Datum Buchungen anzeigen
 */
@Override
public void setup(int kontoNr, Date from) {
	Trace.println(4, "KontoView.BuchungModel.setup(" +kontoNr +"  " +from.toString() +")");
	boolean isSoll = true;      // ist die Buchung ein Soll-Konto

	if (! mBuchungen.isEmpty()) {
		fireTableDataChanged();
		mBuchungen.clear();
	}
	try {
		mStartSaldo = mKontoData.read(kontoNr).getStartSaldo();
		mShowStartLine = true;
		mSaldo = 0;
		mSummeSoll = 0;
		mSummeHaben = 0;
		isSoll = mKontoData.read(kontoNr).isSollKonto();
		//--- Die erste Buchung ist der StartSaldo
		Buchung lBuchung = null;
		if (isSoll) {
			lBuchung = new Buchung(0, Config.sDatumVon, "", "Start Saldo",
				kontoNr, 0, mStartSaldo);
		}
		else {
			lBuchung = new Buchung(0, Config.sDatumVon, "", "Start Saldo",
				0, kontoNr, mStartSaldo);
		}
		addSaldo(kontoNr, isSoll, lBuchung);
		if (lBuchung.getDatum().compareTo(from) >= 0) {
			mBuchungen.add(rowValue(kontoNr, lBuchung));
			mShowStartLine = false;
		}
		//--- alle weiteren Buchungen addieren
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
				    addSaldo(kontoNr, isSoll, lBuchung);
				    mBuchungen.add(rowValue(kontoNr, lBuchung));
				}
				else {
				    addSaldo(kontoNr, isSoll, lBuchung);
				}
			}
		}
		mBuchungen.add(rowSummen());
	}
	catch (FibuException e) {}
	this.fireTableRowsInserted(0, mBuchungen.size()-1);
}


}