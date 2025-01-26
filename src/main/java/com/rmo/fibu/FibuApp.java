package com.rmo.fibu;

import java.awt.Color;
import java.util.Date;

import javax.swing.UIManager;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;
import com.rmo.fibu.view.FibuView;

/**
 * Startprogramm für die Fibu. Mehrere Look and Feel könnten gesetzt werden.
 * Ruft FibuView auf, hier werden alle Ressourcen initialisiert. Autor: RMO
 */
public class FibuApp {

	private static final String sVersion = "FibuLocal V7.10 (26.01.25)";
	private static FibuView mFibu;

	/********************************
	 * Main, start der Fibu: Config einlesen, Fibu kreieren
	 */
	public static void main(String[] args) {
		try {
			Trace.println(0, "--- Gestartet: " + new Date());
			Trace.println(0, "java.version: " + System.getProperty("java.version"));
			Trace.println(0, "java.runtime.name: " + System.getProperty("java.runtime.name"));
			// Die Configuration einlesen
			Trace.println(0, "Lese Config");
			Config.checkArgs(args);
//			setupLogging()
			try {
				// Config-File einlesen
				Config.readPropertyFile();
			}
			catch (FibuException ex) {
				// nix tun, file nicht gefunden
			}
			// alle Werte setzen, wenn noch nicht gemacht
			Config.setAllProperties();

//			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
//			UIManager.put("nimbusBase", new Color(140,140,140));
//			UIManager.put("nimbusBlueGrey", new Color(47,92,180));
//			UIManager.put("control", new Color(176,179,50));
			UIManager.getLookAndFeelDefaults().put("Table.alternateRowColor", new Color(220, 220, 220));
//			UIManager.getLookAndFeelDefaults().put("Table[Enabled+Selected].textForeground", Color.YELLOW);
			// Fibu starten
			mFibu = new FibuView(sVersion);
			mFibu.setVisible(true);
		} catch (Exception ex) {
			Trace.println(0, "Fehler: " + ex.getMessage());
			mFibu.showError(ex);
		}
	}

	/*
	// Login von java, ist ein bisschen zu kompliziert, bleibe beim Trace
	private static void setupLogging() {
		// logging mit Java
		File logging = new File("Logging.properties");
		if (logging.exists()) {
			System.setProperty("java.util.logging.config.file", "Logging.properties");
		}
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream("Logging.properties"));
			String myPropertyValue = LogManager.getLogManager().getProperty("java.util.logging.FileHandler.pattern");
			System.out.println(myPropertyValue);
		} catch (Exception ex) {
			mFibu.showError(ex);
		}
	}
	*/

}
