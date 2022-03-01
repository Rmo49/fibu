package com.rmo.fibu;

import java.awt.Color;

import javax.swing.UIManager;

import com.rmo.fibu.util.Config;
import com.rmo.fibu.view.FibuView;

/**
 * Startprogramm für die Fibu.
 * Mehrere Look and Feel könnten gesetzt werden.
 * Ruft FibuView auf, hier werden alle Ressourcen initialisiert.
 * Autor: RMO
 */
public class FibuApp {

	private static final String sVersion = "FibuLocal V3.8 (02.03.22)";
	private static FibuView mFibu;
   
	/********************************
	 * Main, start der Fibu: Config einlesen, Fibu kreieren
	 */
	public static void main(String[] args) {
		try {
			// Die Configuration einlesen
			Config.checkArgs(args);
			Config.readProperties();
//			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
//			UIManager.put("nimbusBase", new Color(140,140,140));
//			UIManager.put("nimbusBlueGrey", new Color(47,92,180));
//			UIManager.put("control", new Color(176,179,50));
			UIManager.getLookAndFeelDefaults().put("Table.alternateRowColor", new Color(220,220,220));
//			UIManager.getLookAndFeelDefaults().put("Table[Enabled+Selected].textForeground", Color.YELLOW);
			// Fibu starten
			mFibu = new FibuView(sVersion);
			mFibu.setVisible(true);
		} catch (Exception ex) {
			mFibu.showError(ex);
		}
	}

}
