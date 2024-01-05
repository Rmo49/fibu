package com.rmo.fibu.view;


import javax.swing.BoundedRangeModel;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.model.KontoNrVector;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

/** KontoListFrame, Wird momentan nicht verwendet
 * Zeigt die Liste der vorhandenen Konti, wird bei Buchungen verwendet.
 * Die Konti werden in einer JTable angezeigt.
 * Das aktuelle Konto wird in der mitte angezeigt.
 * Grösse und Position wird vom parent-Fenster gesteuert.
 */

 public class KontoListFrame extends JInternalFrame //implements KontoListener, Runnable
 {
	private static final long serialVersionUID = -1430502678322062915L;
	private KontoData   	mKontoData = null;      // die Verbindung zur DB
	/** Das Model zur Tabelle */
	private KontoModel		mKontoTableModel;
	/** Die Liste aller KontoNummern, für Prüfungen */
	private KontoNrVector	mKontoNr = null;

	private JScrollPane		jScrollTable = new JScrollPane();
    private JTable 			jKontoTable = new JTable();

	/**
	 * KontoListFrame constructor.
	 * startet initialiserung des Frames
	 */
	public KontoListFrame() {
		// title resizable closable maximizable iconifiable
		super("Konto Liste (Frame)", false, false, false, false);
		mKontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);
		// beim Data als Observer anmelden
		//mKtoData.addKtoObserver(this);
		init();
	}

	/**
	 * Initialisieren aller Ressourcen.
	 */
	private void init() {
		this.getContentPane().add(jScrollTable);
		jScrollTable.getViewport().add(jKontoTable, null);
		initTableKonto();

//		setTitle("KontoList");
//		this.setSize(250, 400);
//		this.setLocation(100, 20);
	}

	/** Initialisierung der Tabelle für Konto mit dem Model
	 */
	private void initTableKonto() {
		Trace.println(3,"KontoListFrame.initTable()");
		// ----- die Tabelle mit dem Model
		mKontoTableModel = new KontoModel();
		jKontoTable.setModel(mKontoTableModel);
		jKontoTable.setFont(Config.fontText);
		jKontoTable.setRowHeight(Config.windowTextSize + 4);
		mKontoTableModel.addTableModelListener(jKontoTable);
		// die Breite der Cols setzen
		TableColumn column = null;
		for (int i = 0; i <= 1; i++) {
			column = jKontoTable.getColumnModel().getColumn(i);
			switch (i) {
				case 0: column.setPreferredWidth(50);   break;
				case 1: column.setPreferredWidth(200);   break;
				default:
					column.setMaxWidth(100);
					column.setPreferredWidth(80);
			}
		}
		jKontoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mKontoNr = new KontoNrVector();
	}

	public void hideKontoList() {
		this.hide();
	}

	public void showKontoList() {
		this.show();
	}

	/** Setzt den Cursor auf das entsprechende Konto.
	 * @param ktoNr kann auch nur ein Teil der Nr enthalten.
	 * @return wenn die Nummer eindeutigvervollständigt werden kann,
	 *  wird diese zurückgegeben, sonst der korekte Teil
	 */
	public String selectRow (String ktoNr) throws KontoNotFoundException {
		Trace.println(4,"KontoListFrame.selectRow(ktoNr:" + ktoNr +")");
		// prüfen, ob eine gültige KontoNr
		int rowNr = checkNr(ktoNr);
		// Scrollbar berechnen und bewegen
		BoundedRangeModel scrollRange = jScrollTable.getVerticalScrollBar().getModel();
		float faktor = (float)rowNr / (float)mKontoNr.size();
		int max = scrollRange.getMaximum();
		int value = Float.valueOf(max * faktor).intValue();
		//value -= scrollRange.getExtent();
		scrollRange.setValue(value);
		// bewegt die scrollbar, wenn ganz oben = 0
		jScrollTable.getVerticalScrollBar().setValue(value);
		jKontoTable.setRowSelectionInterval(rowNr, rowNr);

		return expandKontoNr (ktoNr, rowNr);
	}

	/** Sucht nach der KontoNummer, oder Teile davon
	 * @param ktoNr die Nummer 1..4-stellig
	 * @return die ZeilenNr (Position) des Kontos
	 * @throws KontoNotFoundException wenn KontoNummer nicht gefunden
	 */
	private int checkNr (String ktoNr) throws KontoNotFoundException {

		int i =0;
		while (i<mKontoNr.size()) {
			String lStr = mKontoNr.get(i);
			if (lStr.startsWith(ktoNr)) {
				return i;
			}
			i++;
		}
		throw new KontoNotFoundException (ktoNr);
	}

	/** Erweitert die KontoNummer mit gültigen Werten
	 * @param kontNr Die eingegeben wurde
	 * @param rowNr erste gültige ZeilenNummer im KontoNrVector
	 * @return die mögliche gültige Nummer
	 */
	private String expandKontoNr (String kontoNr, int rowNr) {
		// Ist die nächste KontoNummer anders als die Eingabe?
		// wenn ja die aktuelle Kontonummer zurückgeben, sonst die Eingabe
		if (kontoNr.length() == 0) {
			return kontoNr;
		}
		// wenn die letzte KontoNummer
		if (rowNr >= mKontoNr.size()-1) {
			return mKontoNr.getAsString(mKontoNr.size()-1);
		}
		String lKontoNext = mKontoNr.getAsString(rowNr+1);
		if (lKontoNext.startsWith(kontoNr)) {
			return kontoNr;
		}
		return mKontoNr.getAsString(rowNr);
	}

	public JTable getTable() {
		return jKontoTable;
	}

	/** Die KontoNummer einer Zeile */
	public String getKontoNrAt (int row) {
		try {
			return mKontoData.readAt(row).getKontoNrAsString();
		}
		catch (KontoNotFoundException ex) {
			ex.printStackTrace(Trace.getPrintWriter());
		}
		return null;
	}

	/** Beenden der Anzeige */
	@Override
	public void dispose() {
	  super.dispose();
	  //@todo close
	  //mKontoData.close();
	}

	public void run() {
	  try {
		// show Form and go
		this.show();
	  }
	  catch (Exception e) {
		  Trace.println(1, "Konto-Exception");
	  }
	}

	//----- Model der Konto-Tabelle ----------------------------
	/** Die Klasse hält eine Verbindung zu den KontoDaten
	 */
	private class KontoModel extends AbstractTableModel {

		private static final long serialVersionUID = 8961852084923312241L;

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return mKontoData.getRowCount();
		}

		/** Die Bezeichnung der Spalten */
		@Override
		public String getColumnName(int col) {
			switch (col) {
				case 0: return "Nummer";
				case 1: return "Konto";
			}
			return "";
		}

		/** Steuert das aussehen einer Spalte */
		@Override
		public Class<?> getColumnClass(int col) {
			return getValueAt(0, col).getClass();
		}

		/** Gibt den Wert an der Koordinate row / col zurück.
		 */
		@Override
		public Object getValueAt(int row, int col) {
			Trace.println(7, "KontoModel.getValueAt(" +row +',' + col +')');
			try {
				Konto lKonto = mKontoData.readAt(row);
				switch (col) {
					case 0: return Integer.valueOf(lKonto.getKontoNr());
					case 1: return lKonto.getText();
				}
			}
			catch (KontoNotFoundException ex) {
				ex.printStackTrace(Trace.getPrintWriter());
			}
			return "";
		}
   }

	//----- Implementierung der Observer-Methode ----------------------
	/** Wird aufgerufen wenn ein Konto geändert wurde,
	* d.h. wenn dort die Methode notifyObservers aufgerufen wurde
	*/
//	public void update(Observable pObservable, Object pArg ) {
//	  /* rmo: mit Events?
//	  if (pObservable instanceof KontoData.KtoObservable) {
//		// die KontoNr, sichtbar?
//		String lKtoNr = (String) pArg;
//		this.repaint();
//	  }
//	  */
//	}


	/**********************************************
	* für den einzel-Test der View.
	*/
	public static void main(String[] args) {
		try {
			DbConnection.open("FibuLeer");
			KontoplanView lKonto = new KontoplanView();
			lKonto.setVisible(true);
		}
		catch (FibuRuntimeException ex) {}
	}

}
