package com.rmo.fibu.util;

import javax.swing.JTable;

import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.view.BuchungModel;

/** Stellt die Verbindung zu den Daten her und steuert die Darstellung.
 */
public class JournalPrinterModel implements BasePrinterModel
{
	/** Das Model zur Tabelle */
	private JTable mBuchungTable;

	public JournalPrinterModel() {
		BuchungData mBuchungData =
			(BuchungData) DataBeanContext.getContext().getDataBean(BuchungData.class);
		mBuchungTable = new JTable(new BuchungModel(mBuchungData));
	}

	/** Die Tabelle, die gedruckt werden soll */
	@Override
	public JTable getTableToPrint() {
		return mBuchungTable;
	}

	/** Die Anzahl Kopfzeilen */
	@Override
	public int getHeaderCount() {
		return 1;
	}

	// TODO wahrscheilich löschen
	/** Die Kopfzeile, wir linksbündig angezeigt.
	 *  Die Seitenzahl wird automatisch rechts generiert */
	public String getHeader(int number) {
		return "Journal " + Config.sFibuTitel;
	}


	@Override
	public String getTitle() {
		return "Journal";
	}

	/** Die Anzahl Spalten */
	@Override
	public int getColCount() {
		return 6;
	}


	@Override
	public boolean isColToAdd(int columnIndex) {
		return false;
	}

	/** Die relative Spaltenbreiten, diese werden der Seitenbreite angepasst. */
	@Override
	public int getColSize(int columnIndex) {
		switch (columnIndex) {
			case 0: return Config.printerJournalCol1;
			case 1: return Config.printerJournalCol2;
			case 2: return Config.printerJournalCol3;
			case 3: return Config.printerJournalCol4;
			case 4: return Config.printerJournalCol5;
			case 5: return Config.printerJournalCol6;
			default: return 20;
		}
	}

	/** Die Spalten-Nummern, die eine Summen enthalten sollen. */
	@Override
	public boolean getColSumme(int columnIndex) {
		if (columnIndex == 5) {
			return true;
		} else {
			return false;
		}
	}

	/** Die Spalten, die rechtsbündig gedruckt werden.
	 *  Zahlen werden automatisch rechtsbündig gedruckt,
	 *  hier angeben, wenn Ueberschrift auch rechtsbündig sein soll */
	@Override
	public boolean getColRight(int columnIndex) {
		if ((columnIndex == 1) || (columnIndex >= 3)) {
			return true;
		}
		return false;
	}

	/** Die überschrift einer Spalte der Liste. */
	@Override
	public String getColName(int columnIndex) {
		return mBuchungTable.getModel().getColumnName(columnIndex);
	}

}///endOfJournalPrinterModel
