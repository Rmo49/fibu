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
		Trace.println(4, "KontoNrVecor.init()");
        KontoData lKontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);
        Konto lKonto = null;
        int max = lKontoData.getRowCount();
        for (int i = 0; i < max; i++) {
            try {
                lKonto = lKontoData.readAt(i);
                add(lKonto.getKontoNrAsString());
            }
            catch (KontoNotFoundException e) {
                Trace.println(1, "KontoNrVecor.init(): " + e.getMessage());
            }
        }
    }
    
    /** Auf die gewünsche Nummer setzen */
    public int getIndex(String kontoNummer) {
       KontoData lKontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);
        Konto lKonto = null;
        int max = lKontoData.getRowCount();
        int ktoNr = Integer.parseInt(kontoNummer);
        int index = 0;
        
        while (index < max) {
            try {
                lKonto = lKontoData.readAt(index);
                if (lKonto.getKontoNr() == ktoNr) {
                	break;
                }
            }
            catch (KontoNotFoundException e) {
                Trace.println(1, "KontoNrVecor.init(): " + e.getMessage());
            }
            index++;
        }
    	return index;
    }
    
    /** Die Kontonummer einer Zeile als String */
    public String getAsString(int rowNr) {
    	return this.get(rowNr);
    }
    
	/** Die Kontonummer einer Zeile als int */
    public int getAsInt(int rowNr) {
    	return Integer.valueOf(this.get(rowNr)).intValue();
    }
}