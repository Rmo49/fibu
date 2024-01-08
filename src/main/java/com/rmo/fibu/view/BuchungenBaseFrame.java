package com.rmo.fibu.view;

import javax.swing.JInternalFrame;

/**
 * Die Basisklasse für alle Buchungen die in einer Liste angezeibt werden.
 */
public abstract class BuchungenBaseFrame extends JInternalFrame {

	private static final long serialVersionUID = -1772491187783153597L;
	
	public BuchungenBaseFrame (String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
		super (title, resizable, closable, maximizable, iconifiable);
	}

	/**
	 * Damit die Aenderungen angezeigt werden 
	 */
	public abstract void repaintBuchungen();
	
	/**
	 * Meldung, dass Zeilen eingefüft wurden
	 */
	public abstract void rowsInserted(int firstRow, int lastRow);

	
}
