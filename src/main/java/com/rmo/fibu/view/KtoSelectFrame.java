package com.rmo.fibu.view;

import javax.swing.JInternalFrame;

import com.rmo.fibu.util.Trace;

/** KtoSelectFrame: Zeigt die Liste der vorhandenen Konti,
 * wird bei BuchungEingabe verwendet.
 * Die Konti werden in einer JTable angezeigt.
 * Das aktuelle Konto wird in der mitte angezeigt.
 * Grösse und Position wird vom parent-Fenster gesteuert.
 */

 public class KtoSelectFrame extends KtoSelectBase {
//	private static final long serialVersionUID = -1430502678322182915L;
	/** KontoDialog damit die KontoListe free floating ist */
	private JInternalFrame	mKtoFrame;

	/**
	 * KtoSelectDialog constructor.
	 * startet initialiserung des Frames.
	 * eingabe zeigt auf den Panel woher aufgerufen
	 */
	public KtoSelectFrame(BuchungEingabe eingabe) {
		super(eingabe);
	}


	/** Initialisierung des Anzeige-Bereiches: Kontoliste in ScrollPane
	 * in einem JInternalFrame.
	 */
	public JInternalFrame initKtoSelectFrame() {
		Trace.println(4,"KtoSelectFrame.initKtoSelectFrame()");
		mKtoFrame = new JInternalFrame();
		mKtoFrame.add(getKtoScrollPane());

		mKtoFrame.setTitle("Konto Liste (Frame)");
		setSize();
		return mKtoFrame;
	}


	/** Grösse und Position der Kontoliste berechnen */
	private void setSize() {
		mKtoFrame.setSize(270, 440);
//		mKontoScrollPane.setSize(250, 400);
//		mKontoScrollPane.setLocation(0, 0);
	}

	public JInternalFrame getFrame() {
		return mKtoFrame;
	}


}
