package com.rmo.fibu.view;

import com.rmo.fibu.model.Buchung;

/**
 * Das Interface, das alle Klassen, die BuchungEingaben verwenden, implementieren müssen.
 * Über dieses Interface wird die Kommunikation mit der Basisklasse sichergestellt.
 * @author ruedi
 *
 */
public interface BuchungInterface {
	public Buchung getLastBuchung();
	public void enableButtons();
	public void setMessage(String text);
}
