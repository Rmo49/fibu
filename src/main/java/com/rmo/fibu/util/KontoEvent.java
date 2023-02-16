package com.rmo.fibu.util;

import java.util.EventObject;

/**
 * @author Ruedi
 *
 * Event-Klasse für die Notifikation der KontoListe
 */
public class KontoEvent extends EventObject {

	private static final long serialVersionUID = 3615826658264857078L;
	private String mKtoNr;

	/**
	 * @param source, das Objekt, das den Event geworfen hat
	 * @param ktoNr die (umvollständige) Nummer des Kontos
	 */
	public KontoEvent(Object source, String ktoNr) {
		super(source);
		mKtoNr = ktoNr;
	}

	public String getKtoNr() {
		return mKtoNr;
	}
}
