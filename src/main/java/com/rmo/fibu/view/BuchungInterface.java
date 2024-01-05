package com.rmo.fibu.view;

import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungData;

/**
 * Das Interface, das alle Klassen, die BuchungEingaben verwenden, implementieren müssen.
 * Über dieses Interface wird die Kommunikation mit der Basisklasse sichergestellt.
 * @author ruedi
 *
 */
public interface BuchungInterface {
	/**
	 * Die letzte Buchung in der Liste
	 * @return letzte Buchdung
	 */
	public Buchung getLastBuchung();
	
	/**
	 * Verbindung zu der Datenbank
	 */
	public BuchungData getBuchungData();
	
	/**
	 * Menu enable oder disable, je nach Zustand der Eingabe
	 * @param enteringBooking
	 * @param mChangeing
	 */
	public void setBuchungMenu(boolean enteringBooking, boolean mChangeing);
	
	/**
	 * Ans Ende der Liste scrollen
	 */
	public void scrollToEnd();
	
	/**
	 * Die Liste der angezeigten Buchungen
	 * @return
	 */
	public BuchungListFrame getBuchungListe();
}
