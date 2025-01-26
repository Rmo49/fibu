package com.rmo.fibu.util;

import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.BuchungOfKontoModel;
import com.rmo.fibu.model.BuchungOfKontoModelNormal;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoData;

/**
 * Stellt die Verbindung zu den Daten her und steuert die Darstellung.
 */
public class KontoListPrinterModel implements KontoListPrinterInterface
{
	/** Soll der schleppende Saldo angegeben werden. */
	private boolean mitSaldo = false;
	/** die Models zur Tabelle */
	private BuchungOfKontoModel mBuchungTable;
	private KontoData 			mKontoData;
	/** Das aktuelle Konto in der Tabelle */
	private	int					mActualKontoNr = 0;

	public KontoListPrinterModel() {
		mBuchungTable = new BuchungOfKontoModelNormal();
		mKontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);
	}

	/** Der Iterator, der verwendet werden muss
	 * um über alle Konti zu Iterieren*/
//	public Iterator<Konto> getIterator() {
//		return mKontoData.getIterator();
//	}

	/** Die Anzahl Kopfzeilen */
	@Override
	public int getHeaderCount() {
		return 1;
	}

	/** Die Kopfzeile, wir linksbündig angezeigt.
	 * @param kontoNr die gewählte Kontonummer
	 *  @param number die Zeilennummer
	 * */
	@Override
	public String getKontoName(int kontoNr, int number) {
		Konto lKonto = null;
		try {
			lKonto = mKontoData.read(kontoNr);
		}
		catch (KontoNotFoundException ex) {
			Trace.println(0, "Error in KontoListPrintDialog:" + ex.getMessage());
		}
		return lKonto.getKontoNrAsString() + "  " + lKonto.getText();
	}

	/** Die Anzahl Spalten */
	@Override
	public int getColCount() {
		if (mitSaldo) {
			return 7;
		}
		return 6;
	}

	/** Die relative Spaltenbreiten, diese werden der Seitenbreite angepasst. */
	@Override
	public int getColSize(int columnIndex) {
		switch (columnIndex) {
			case 0: return Config.printerKtoCol1;
			case 1: return Config.printerKtoCol2;
			case 2: return Config.printerKtoCol3;
			case 3: return Config.printerKtoCol4;
			case 4: return Config.printerKtoCol5;
			case 5: return Config.printerKtoCol6;
			case 6: return Config.printerKtoCol7;
			default: return 20;
		}
	}

	/** Die Spalten-Nummern, die eine Summen enthalten sollen. */
	@Override
	public boolean isColToAdd(int columnIndex) {
		if (columnIndex == 4 || columnIndex == 5) {
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
		if ((columnIndex == 1) || (columnIndex >= 4)) {
			return true;
		}
		return false;
	}

	/** Die überschrift einer Spalte der Liste. */
	@Override
	public String getColName(int columnIndex) {
		return mBuchungTable.getColumnName(columnIndex);
	}

	/** Die Anzahl Zeilen eines Kontos
	 * @param kontoNr die gewählte Kontonummer
	*/
	@Override
	public int getRowCount(int kontoNr) {
		return getBuchungTable(kontoNr).getRowCount();
	}

	/** Der Wert einer Zelle.
	 * @param row Zeile
	 * @param col Spalte
	 * @return Wert der Zelle */
	@Override
	public Object getValueAt(int kontoNr, int row, int col) {
		return getBuchungTable(kontoNr).getValueAt(row, col);
	}


	/**
	 * @return the mitSaldo true, wenn Saldo-Spalte angezeigt werden soll.
	 */
	public boolean isMitSaldo() {
		return mitSaldo;
	}


	/**
	 * Soll die Saldospalte angezeigt werden?
	 * @param mitSaldo wenn true wird die Saldospalte angezeigt
	 */

	public void setMitSaldo(boolean mitSaldo) {
		this.mitSaldo = mitSaldo;
	}


	/** Setzt das BuchungsModel auf das geforderte Konto
	 * falls nicht identisch mit dem Vorgänger.
	 * @param kontoNr
	 * @return
	 */
	private BuchungOfKontoModel getBuchungTable(int kontoNr) {
		if (kontoNr != mActualKontoNr) {
			mBuchungTable.setup(kontoNr, Config.sDatumVon);
			mActualKontoNr = kontoNr;
		}
		return mBuchungTable;
	}

}