package com.rmo.fibu.model;

import java.util.HashMap;
import java.util.Iterator;

import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.util.Trace;

/**
 * Berechnet den Saldo aller Konti. ausgehend vom Startsaldo, dann wird jede
 * Buchung durchlaufen. Der Fortschritt des Durchlaufs kann mit einem
 * ProgressObserver verfolgt werden.
 */
public class KontoCalculator {

	private KontoData mKontoData;
	private HashMap<String, Konto> mKonti; // temporärer Speicher

	public KontoCalculator() {
		mKontoData = (KontoData) DataBeanContext.getContext().getDataBean(
				KontoData.class);
		mKonti = new HashMap<String, Konto>(mKontoData.getRowCount());
	}

	/**
	 * berechnet den aktuellen Saldo. ausgehend vom Startsaldo, dann jede
	 * Buchung
	 */
	public void calculateSaldo() throws KontoNotFoundException {
		Trace.println(2, "KontoCalculator.calculateSaldo()");
		fillHashMap();
		verbucheSaldo();
		saveHashMap();
	}

	/** Liste auffüllen, StartSaldo setzen */
	private void fillHashMap() {
		Trace.println(3, "KontoCalculator.fillHashMap()");
		// übertrage Startbeträge in StartSaldo
		Iterator<Konto> kontoIterator = mKontoData.getIterator();
		// Vektor aufbauen
		while (kontoIterator.hasNext()) {
			Konto lKonto = kontoIterator.next();
			Trace.println(8, "Konto: " + lKonto.getKontoNr());
			// notifyObservers(lKonto.getKontoNrAsString());
			lKonto.setSaldo(lKonto.getStartSaldo());
			mKonti.put(lKonto.getKontoNrAsString(), lKonto);
		}
	}

	/** Durchlaufe alle Buchungen und berechne Saldo */
	private void verbucheSaldo() throws KontoNotFoundException {
		Trace.println(3, "KontoCalculator.verbucheSaldo()");
		BuchungData lBuchungData = (BuchungData) DataBeanContext.getContext()
				.getDataBean(BuchungData.class);
		Iterator<Buchung> buchungIter = lBuchungData.getIterator();
		while (buchungIter.hasNext()) {
			Buchung lBuchung = buchungIter.next();
			Trace.println(8, "Beleg: " + lBuchung.getBeleg());
			// notifyObservers(lBuchung.getBeleg());
			verbuche(lBuchung.getBetrag(), lBuchung.getSollAsString(), true);
			verbuche(lBuchung.getBetrag(), lBuchung.getHabenAsString(), false);
		}
	}

	/**
	 * verbucht die Buchung im Kontorahmen (HashMap)
	 * 
	 * @param double pBetrag der Betrag der Buchung
	 * @para int pKontoNr das Konto auf das verbucht werden soll
	 * @para boolean pSoll der Betrag steht in der Soll- / Haben-Spalte diese
	 *       Methode muss 2 mal aufgerufen werden
	 */
	private void verbuche(double pBetrag, String pKontoNr, boolean pSoll)
			throws KontoNotFoundException {
		// Konto lesen, check ob Soll- oder Haben-Konto
		Konto lKonto = mKonti.get(pKontoNr);
		if (lKonto == null) {
			throw new KontoNotFoundException("Konto nicht gesetzt");
		}
		double lSaldo = lKonto.getSaldo();
		// verbuche: wenn es ein Soll-Konto ist
		if (lKonto.isSollKonto()) {
			// wenn in Buchung in der Soll-Spalte steht: dazuzählen
			if (pSoll)
				lSaldo += pBetrag;
			else
				lSaldo -= pBetrag;
		} else {
			if (pSoll)
				lSaldo -= pBetrag;
			else
				lSaldo += pBetrag;
		}
		lKonto.setSaldo(lSaldo);
	}

	private void saveHashMap() throws KontoNotFoundException {
		Trace.println(3, "KontoCalculator.saveHashMap()");
		Iterator<String> iter = mKonti.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Konto lKonto = mKonti.get(key);
			mKontoData.add(lKonto);
		}
		Trace.println(3, "KontoCalculator.saveHashMap() <=== end");
	}

}