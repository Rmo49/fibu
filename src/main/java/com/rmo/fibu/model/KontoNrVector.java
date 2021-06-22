package com.rmo.fibu.model;

import java.util.Vector;

import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.util.Trace;

/**
 * Stellt die aktuellen Konotnummern in einem String-Vektor zur Verfügung
 * @author RMO
 * @version 1.0
 * TODO Listener von KontoData wenn add oder removed members
 */
public class KontoNrVector extends Vector<String> {

	private static final long serialVersionUID = -7027963298249850446L;

	public KontoNrVector() {
        init();
    }

    /** Initialisiert die Daten, liest alle ein */
    private void init() {
		Trace.println(3, "KontoNrVecor.init()");
        KontoData lKontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);
        Konto lKonto = null;
        int max = lKontoData.getRowCount();
        for (int i = 0; i < max; i++) {
            try {
                lKonto = lKontoData.readAt(i);
                add((String) lKonto.getKontoNrAsString());
            }
            catch (KontoNotFoundException e) {
                Trace.println(1, "KontoNrVecor.init(): " + e.getMessage());
            }
        }
    }
    
    /** Die Kontonummer einer Zeile als String */
    public String getAsString(int rowNr) {
    	return (String) this.get(rowNr);
    }
    
	/** Die Kontonummer einer Zeile als int */
    public int getAsInt(int rowNr) {
    	return Integer.valueOf((java.lang.String)this.get(rowNr)).intValue();
    }
}