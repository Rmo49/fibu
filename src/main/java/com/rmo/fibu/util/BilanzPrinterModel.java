package com.rmo.fibu.util;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JTable;

/**
 * Stellt die Verbindung zu den Daten her und steuert die Darstellung.
 */
public class BilanzPrinterModel implements BasePrinterModel {
	/** Das Model zur Tabelle */
	private JTable mTableToPrint;
	private String mTitel;
	/** version 2 mit mehreren Einträgen */
	private ArrayList<BilanzPrinterWerte> bilanzListe = new ArrayList<BilanzPrinterWerte>();

	public BilanzPrinterModel(JTable tabletoPrint, String kopfzeile) {
		mTableToPrint = tabletoPrint;
		mTitel = kopfzeile;
	}
	
	public BilanzPrinterModel() {
	}
	
	public void addBilanz(BilanzPrinterWerte werte) {
		bilanzListe.add(werte);
	}
	
	/**
	 * Git eine Bilanz zurück, wenn in der Reihenfolge >= der übergebenden.
	 * Wenn 0 dann 1, wenn 1 auch 1 oder 2, 
	 * @return
	 */
	public Iterator<BilanzPrinterWerte> getInterator() {
		return bilanzListe.iterator();
	}

	/** Die Tabelle, die gedruckt werden soll */
	@Override
	public JTable getTableToPrint() {
		return mTableToPrint;
	}

	/** Die Anzahl Kopfzeilen */
	@Override
	public int getHeaderCount() {
		return 1;
	}

	// TODO wahrscheinlich löschen (23.1.25)
	/**
	 * Die Kopfzeile, wir linksbündig angezeigt. Die Seitenzahl wird automatisch
	 * rechts generiert
	 */
//	public String getHeader(int nr) {
//		if (nr == 0) {
//			return Config.sFibuTitel;
//		} else {
//			return mTitel;
//		}
//	}


	@Override
	public String getTitle() {
		return mTitel;
	}

	/** Die Anzahl Spalten */
	@Override
	public int getColCount() {
		return 4;
	}

	/**
	 * Die relative Spaltenbreiten, diese werden der Seitenbreite angepasst.
	 */
	@Override
	public int getColSize(int columnIndex) {
		switch (columnIndex) {
			case 0: return Config.printerBilanzCol1;
			case 1: return Config.printerBilanzCol2;
			case 2: return Config.printerBilanzCol3;
			case 3: return Config.printerBilanzCol4;
			default: return 20;
		}
	}

	/**
	 * Die Spalten-Nummern, die eine Summen enthalten sollen.
	 */
	@Override
	public boolean getColSumme(int columnIndex) {
		return isColToAdd(columnIndex);
	}

	/**
	 * Die Spalten, die rechtsbündig gedruckt werden. Zahlen werden automatisch
	 * rechtsbündig gedruckt, hier angeben, wenn Ueberschrift auch rechtsbündig sein
	 * soll
	 */
	@Override
	public boolean getColRight(int columnIndex) {
		// ab col 2 rechtsbündig
		if (columnIndex <= 1) {
			return false;
		} else {
			return true;
		}
	}


	@Override
	public boolean isColToAdd(int columnIndex) {
		if (columnIndex <= 1) {
			return false;
		}
		return true;
	}

	/** Die überschrift einer Spalte der Liste. */
	@Override
	public String getColName(int columnIndex) {
		return mTableToPrint.getModel().getColumnName(columnIndex);
	}
	

} // end of BilanzenPrinterModel
