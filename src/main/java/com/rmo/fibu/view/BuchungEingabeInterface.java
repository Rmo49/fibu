package com.rmo.fibu.view;

import javax.swing.JDesktopPane;

import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungData;

/**
 * Das Interface, das alle Klassen, die BuchungEingaben verwenden, implementieren müssen.
 * Über dieses Interface wird die Kommunikation mit der Basisklasse sichergestellt.
 * @author ruedi
 *
 */
public interface BuchungEingabeInterface {
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
	 * Die Liste der Buchungen soll ans Ende der Liste scrollen
	 */
	public void scrollToEnd();

	/**
	 * Die Anzeige der Buchungen in BuchungView und KontoView
	 * @return Basisklasse BuchungenBaseFrame
	 */
	public BuchungenBaseFrame getBuchungenFrame();

	/**
	 * Das Frame auf not visible setzen, falls es ein JFrame ist.
	 */
	public void hideEingabe();

	/**
	 * Die Zentrale JPanel, für die Anzeige der KontoListen,
	 */
	public JDesktopPane getPaneCenter();

	/**
	 * Welche Klasse steckt hinter der Buchung
	 */
	public boolean isBuchungView();
}
