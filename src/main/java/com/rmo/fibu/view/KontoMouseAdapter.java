package com.rmo.fibu.view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;

import com.rmo.fibu.util.Trace;

/** Eingabefelder für Konto-Nummern.
 * Der MouseAdapter für die Behandlung der Mouse-Events.
 */
public class KontoMouseAdapter extends MouseAdapter {
	// Das Eingabefeld, auf das sich der Mouse-Event bezieht.
	JTextField mKontoField;
	
	/**
	 * Konstruktor mit dem entsprechenden Feld
	 */
	public KontoMouseAdapter(JTextField kontoField) {
		super();
		mKontoField = kontoField;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Trace.println(4, "KontoMouseAdapter.mouseClicked()");
		e.consume();
		mKontoField.setCaretPosition(mKontoField.getText().length());
	}
}
