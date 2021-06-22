package com.rmo.fibu.view;

import javax.swing.table.AbstractTableModel;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.util.Trace;

/**
 * Title:        Fibu
 * Description:  Buchhaltungs-Programm
 * @version 1.0
 * Das Model für die Darstellung aller Buchungen in einer JTable.
 * Legt die Spalten und deren Beschriftung fest
 */
public class BuchungModel extends AbstractTableModel {
	private static final long serialVersionUID = 4355743120074812303L;
	/** Die Verbindung zur Datenbank */
	private BuchungData     mBuchungData = null;
	
public BuchungModel(BuchungData buchungen) {
	mBuchungData = buchungen;
}

/** Nur die ersten 6 Cols anzeigen (keine ID) */
public int getColumnCount() {
	return 6;
}

/** Anzahl Zeilen in der Tabelle */
public int getRowCount() {
	return mBuchungData.getRowCount();
}

public String getColumnName(int col) {
	switch (col) {
		case 0: return "Datum";
		case 1: return "Beleg";
		case 2: return "Text";
		case 3: return "Soll";
		case 4: return "Haben";
		case 5: return "Betrag";
		case 6: return "ID";
	}
	return "";
}

/** Steuert das aussehen einer Spalte */
public Class<?> getColumnClass(int col) {
	return getValueAt(0, col).getClass();
}

/** Gibt den Wert einer Buchung an der Koordinate row / col zurück.
 * @param row die Zeile im Model
 * @param col die Spalte
 * 0 = Datum (Datum)
 * 1 = Beleg (String)
 * 2 = Text (String)
 * 3 = Soll (Integer)
 * 4 = Haben (Integer)
 * 5 = Betrag (Double)
 * 6 = ID (Long)
 * @return Den Wert, oder LeerString wenn nichts gefunden
 */
public Object getValueAt(int row, int col) {
	Trace.println(7, "BuchungModel.getValueAt(" +row +',' + col +')');
	try {
		Buchung lBuchung = mBuchungData.readAt(row);
		if (lBuchung == null) {
			return "";
		}
		switch (col) {
			case 0: return lBuchung.getDatum();
			case 1: return lBuchung.getBeleg();
			case 2: return lBuchung.getBuchungText();
			case 3: return Integer.valueOf(lBuchung.getSoll());
			case 4: return Integer.valueOf(lBuchung.getHaben());
			case 5: return Double.valueOf(lBuchung.getBetrag());
			case 6: return Long.valueOf(lBuchung.getID());
		}
	}
	catch (FibuException ex) {}
	return "";
}

}//endOfClass