package com.rmo.fibu.util;

import java.awt.Graphics2D;

/**
 * Druckt das Journal
 */
public class JournalPrinter extends BasePrinter {

	/**
	 * Konstruktor mit TableModel für die Datenabfrage */
	public JournalPrinter(BasePrinterModel theModel) {
		super.printerModel = theModel;
	}


	/**
	 * Eine Seite drucken. Die Methode muss selber wissen was wann noch fortgesetzt werden soll.
	 * @param g
	 * @param pageIndex
	 * @param printing
	 */
	@Override
	protected void printPage(Graphics2D g, int pageIndex, boolean printing) {
		Trace.println(4, "JournalPrinter.printPage: " + pageIndex);

		// Kopfzeile drucken
		yPos = Config.printerNormalFont.getSize2D();
		printHeader(g, printing);
		// den title
		printTitle(g, printing);
		// Tabelle drucken
		printTableHeader(g, printing);
		// Tabelle drucken
		printTable(g, printing);
	}

	/**
	 * Die Tabelle drucken, muss selber merken, wann fertig ist. Berechnet die
	 * schleppende Summen und führt
	 */
	private void printTable(Graphics2D g, boolean printing) {
		yPos += spaceForOneRow();
		if (printing) {
			g.setFont(Config.printerNormalFont);
			
			while (mPrintedRows <= getTableModel().getRowCount()) {
				if (yPos > (pageHeight - spaceSummeFooter())) {
					// wenn kein Platz mehr
					return;
				}
				if (printing) {
					// --- eine Zeile drucken
					printTableRow(g, mPrintedRows);
				}
				mPrintedRows++;
				yPos += spaceForOneRow();
			}
			mPrintedAll = true;
		}
		// den letzten increment korrigieren, Zeile wurde nicht gedruckt
		yPos -= spaceForOneRow();
	}

}
