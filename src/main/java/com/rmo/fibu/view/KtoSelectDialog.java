package com.rmo.fibu.view;

import javax.swing.JDialog;

import com.rmo.fibu.util.Trace;

/** KtoSelectDialog: Zeigt die Liste der vorhandenen Konti,
 * wird bei BuchungEingabe verwendet.
 * Die Konti werden in einer JTable angezeigt.
 * Das aktuelle Konto wird in der mitte angezeigt.
 * Grösse und Position wird vom parent-Fenster gesteuert.
 */

 public class KtoSelectDialog extends KtoSelectBase {
//	private static final long serialVersionUID = -1430502678322062915L;
	/** KontoDialog damit die KontoListe free floating ist */
	private JDialog 		mKtoDialog;


	/**
	 * KtoSelectDialog constructor.
	 * startet initialiserung des Frames.
	 * eingabe zeigt auf den Panel woher aufgerufen
	 */
	public KtoSelectDialog(BuchungEingabe eingabe) {
		super (eingabe);
	}


	/** Initialisierung des Anzeige-Bereiches: Kontoliste in ScrollPane
	 * in einem DesktopPane.
	 */
	public JDialog initKtoSelectDialog() {
		Trace.println(4,"KtoSelectDialog.initKtoSelectDialog()");
		mKtoDialog = new JDialog();
		mKtoDialog.add(getKtoScrollPane());

		mKtoDialog.setTitle("Konto Liste (Dialog)");
		setSize();
		return mKtoDialog;
	}

	/** Grösse und Position der Kontoliste berechnen */
	private void setSize() {
		mKtoDialog.setSize(270, 440);
//		mKontoScrollPane.setSize(250, 400);
//		mKontoScrollPane.setLocation(0, 0);
	}

	public JDialog getDialog() {
		return mKtoDialog;
	}

}
