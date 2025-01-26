package com.rmo.fibu.util;

import javax.swing.JTable;

/**
 * Datentyp für die BilanzListe,
 */
public class BilanzPrinterWerte {
		/** Die nrToPrint in der Reihenfolge (0..3) */
		public int nrToPrint;
		/** Nummer der Bilanz, 1: StartBilanz, 2: Bilanz, 3 ER */
		public int bilanzNr;
		public boolean isPrinted = false;
		public String title;
		public JTable tabelle;
}
