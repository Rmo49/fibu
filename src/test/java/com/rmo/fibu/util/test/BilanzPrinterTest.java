package com.rmo.fibu.util.test;

import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.rmo.fibu.util.BasePrinterModel;
import com.rmo.fibu.util.BilanzPrinter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test für BasePrinter.
 */

public class BilanzPrinterTest extends TestCase implements BasePrinterModel {

	private JTable table;

	public BilanzPrinterTest(String name) {
		super(name);
		// Initialisierung einer Tabelle
		TableModel dataModel = new AbstractTableModel() {
			private static final long serialVersionUID = 5945736873892866473L;
			@Override
			public int getColumnCount() { return 5; }
			@Override
			public int getRowCount() { return 6;}
			@Override
			public Object getValueAt(int row, int col) {
				switch (col) {
					case 0: return Integer.valueOf(row);
					case 1: return Double.valueOf(row);
					case 2: return Double.valueOf(row + (col*.1));
					case 3: return new String ("Row: " + row);
					case 4: return new String ("Konstante");
                    case 5: return new Date();
					default: return new String ("default");
				}
			}
		};
		table = new JTable(dataModel);
	}


	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public boolean isColToAdd(int columnIndex) {
		return false;
	}



	/***************************
	 * Diese Tests starten
	*/
	public static void main(String[] args) {
		  junit.textui.TestRunner.run(suite());
	}

	/** Setup Test-Objects
	 */
	@Override
	public void setUp() {
		try {
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	/** Hier werden alle TestSchritte zusammengestellt.
	 Generisch oder Einzeln, wobei bei Generisch die Reihenfolge nicht bestimmt werden kann.
	*/
	public static Test suite() {
		// Generisch: alle Tests von KontoModel
		TestSuite suite = new TestSuite(BilanzPrinterTest.class);
		return suite;
	}

	/** Die Tabelle drucken mit der BasePrinter Klasse.
	 */
	public void testPrint() throws Exception {
		BilanzPrinter lPrinter= new BilanzPrinter(null);
		lPrinter.doPrint();
	}

	//----- Implementierung des TablePrinterModels -------------------------
	/** Die Tabelle, die gedruckt werden soll */
	@Override
	public JTable getTableToPrint() {
		return table;
	}

	/** Die Kopfzeile, wir linksböndig angezeigt, Seitenzahl rechts */
	@Override
	public int getHeaderCount() {
		return 1;
	}

	// TODO wahrscheinlich löschen
	/** Die Kopfzeile, wir linksböndig angezeigt, Seitenzahl rechts */
//	public String getHeader(int nr) {
//		return "Kopfzeile Test";
//	}

	/** Die max. Anzahl Spalten */
	@Override
	public int getColCount() {
		return 6;
	}

	/** Ein Array mit der gleichen Länge wie Anzahl Spalten wird erwartet.
	 *  Die Spaltenbreiten werden relativ angegeben, diese werden der
	 *  Seitenbreite angepasst.  */
	@Override
	public int getColSize(int columnIndex) {
		switch (columnIndex) {
			case 0: return 10;
			case 3: return 40;
			default: return 20;
		}
	}

	/** Die Spalten, die rechtsböndig gedruckt werden.
	 *  Zahlen werden automatisch rechtsböndig gedruckt. */
	@Override
	public boolean getColRight(int columnIndex) {
		if (columnIndex == 3) {
			return true;
		}
		return false;
	}

	/** Die Spalten, die eine Summen enthalten sollen */
	@Override
	public boolean getColSumme(int columnIndex) {
		if (columnIndex == 1 || columnIndex == 2) {
			return true;
		}
		return false;
	}

	/** Die überschrift einer Spalte der Liste */
	@Override
	public String getColName(int columnIndex) {
		return "";
	}
}