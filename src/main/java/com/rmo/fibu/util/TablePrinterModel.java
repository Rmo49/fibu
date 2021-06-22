package com.rmo.fibu.util;

import javax.swing.JTable;

/** Das Interface zum TablePrinter.
 *  Klassen, die den TablePrinter verwenden, müssen dieses Interface
 * implementieren.
 */
public interface TablePrinterModel {

	/** Die Tabelle, die gedruckt werden soll */
	public JTable getTableToPrint();

	/** Die Anzahl Kopfzeilen */
	public int getHeaderCount();

	/** Die Kopfzeile, wir linksbündig angezeigt.
	 *  Die Seitenzahl wird bei der ersten Zeile automatisch rechts generiert */
	public String getHeader(int number);

	/** Die Anzahl Spalten */
	public int getColCount();

	/** Die relative Spaltenbreiten, diese werden der Seitenbreite angepasst. */
	public int getColSize(int columnIndex);

	/** Die Spalten-Nummern, die eine Summen enthalten sollen. */
	public boolean getColSumme(int columnIndex);

	/** Die Spalten, die rechtsbündig gedruckt werden.
	 *  Zahlen werden automatisch rechtsbündig gedruckt,
	 *  hier angeben, wenn Ueberschrift auch rechtsbündig sein soll */
	public boolean getColRight(int columnIndex);

	/** Die überschrift einer Spalte der Liste. */
	public String getColName(int columnIndex);
}
