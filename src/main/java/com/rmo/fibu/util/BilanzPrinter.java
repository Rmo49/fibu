package com.rmo.fibu.util;

import java.awt.Graphics2D;
import java.util.Iterator;

import javax.swing.JTable;

/**
 * Druckt die Bilanzen.
 */
public class BilanzPrinter extends BasePrinter implements BasePrinterModel {

	// Das Model zum BilanzPrinter
	private BilanzPrinterModel bilanzPrinterModel;
	// die nächste Bilanz, grösser oder gleich von lastNr
	private int lastNrPrinted = 0;
	// die zu druckende Bilanz
	BilanzPrinterWerte werte = null;

	/**
	 * Konstruktor mit TableModel für die Datenabfrage
	 */
	public BilanzPrinter(BilanzPrinterModel theModel) {
		// diese Klasse ist der Provider für die Daten
		printerModel = this;
		bilanzPrinterModel = theModel;
		// die erste Bilanz einlesen, damit Daten vorhanden
		werte = naechsteBilanz();
	}

	/**
	 * Eine Seite drucken. Die Methode muss selber wissen was wann noch fortgesetzt
	 * werden soll.
	 * 
	 * @param g
	 * @param pageIndex
	 * @param printing
	 */
	@Override
	protected void printPage(Graphics2D g, int pageIndex, boolean printing) {
		Trace.println(4, "BilanzPrinter.printPage(" + pageIndex + ")");

		while (werte != null) {
			// eine Bilanz gefunden
			// Kopfzeile drucken nur, wenn die erste auf der Seite
			if (werte.nrToPrint == 1 || pageIndex > 0) {
				yPos = Config.printerNormalFont.getSize2D();
				printHeader(g, printing);
			}

			// den title
			printTitle(g, printing);
			// Tabellekopf drucken
			printTableHeader(g, printing);
			// Tabelle drucken
			werte.isPrinted = printTable(g, printing);

			if (werte.isPrinted) {
				werte = naechsteBilanz();
			} else {
				// Summe drucken am ende einer Seite
				printSummeFooter(g, printing);
				break;
			}
		}
		if (werte == null) {
			mPrintedAll = true;
		}
	}

	/**
	 * Gibt es eine Bilanz zu drucken? wenn Bilanz >= lastNrToPrint, dann drucken
	 * 
	 * @return
	 */
	private BilanzPrinterWerte naechsteBilanz() {
		BilanzPrinterWerte werte = null;
		for (Iterator<BilanzPrinterWerte> iterator = bilanzPrinterModel.getInterator(); iterator.hasNext();) {
			werte = iterator.next();
			if (werte.nrToPrint == lastNrPrinted+1 && !werte.isPrinted) {
				// noch nicht gedruckt, alles zurücksetzen
				lastNrPrinted++;
				mPrintedRows = 0;
				mTableModel = null;
				mColSumme = new double[werte.tabelle.getColumnCount()];
				return werte;
			}
		}
		// nichts mehr gefunden
		return null;
	}

	/**
	 * Die Tabelle drucken, muss selber merken, wann fertig ist. Berechnet die
	 * schleppende Summen
	 * @return true wenn alles gedruckt
	 */
	private boolean printTable(Graphics2D g, boolean printing) {
		yPos += spaceForOneRow();
		if (printing) {
			g.setFont(Config.printerNormalFont);
			// drucken, solange Platz vorhanden
			while (mPrintedRows <= getTableModel().getRowCount()) {
				if (yPos >= (pageHeight - spaceSummeFooter())) {
					// kein Platz
					yPos -= spaceForOneRow(); // nicht gedruckt
					return false;
				}
				if (printing) {
					// --- eine Zeile drucken
					printTableRow(g, mPrintedRows);
				}
				addSumme();
				mPrintedRows++;
				yPos += spaceForOneRow();
			}
		}
		// den letzten increment korrigieren, Zeile wurde nicht gedruckt
		yPos -= spaceForOneRow();
		return true;
	}

	@Override
	public JTable getTableToPrint() {
		return werte.tabelle;
	}

	/** Die Anzahl Kopfzeilen */
	@Override
	public int getHeaderCount() {
		return 1;
	}

	@Override
	public String getTitle() {
		return werte.title;
	}

	@Override
	public int getColCount() {
		return werte.tabelle.getColumnCount();
	}

	@Override
	public int getColSize(int columnIndex) {
		switch (columnIndex) {
		case 0: return Config.printerBilanzCol1;
		case 1: return Config.printerBilanzCol2;
		case 2: return Config.printerBilanzCol3;
		case 3: return Config.printerBilanzCol4;
		}
		return Config.printerBilanzCol1;
	}

	@Override
	public boolean getColSumme(int columnIndex) {
		return isColToAdd(columnIndex);
	}

	@Override
	public boolean isColToAdd(int columnIndex) {
		if (columnIndex >= 2) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean getColRight(int columnIndex) {
		if (columnIndex >= 2) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getColName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Konto";
		case 1:
			return "Text";
		case 2:
			return "Soll";
		case 3:
			return "Haben";
		}
		return "";
	}

	// ---- die Werte für den Base Printer

}
