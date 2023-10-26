package com.rmo.fibu.view;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

/** KontoListView: Zeigt die Liste der vorhandenen Konti,
 * wird bei Buchungen verwendet.
 * Die Konti werden in einer JTable angezeigt.
 * Das aktuelle Konto wird in der mitte angezeigt.
 * Grösse und Position wird vom parent-Fenster gesteuert.
 */

 public class KontoListDialog {
//	private static final long serialVersionUID = -1430502678322062915L;
	/** Die Verbindung zur aufrufenden Klasse */
	private BuchungEingabe	mEingabe = null;
	/** KontoDialog damit die KontoListe free floating ist */
	private JDialog 		mKontoDialog;
	/** KontoListe für die Anzeige aller Konti */
	private JScrollPane		mKontoScrollPane;
	private KontoData   	mKontoData = null;      // die Verbindung zur DB
	/** Das Model zur Tabelle */
	private KontoModel		mKontoTableModel;
	/** Die Liste aller KontoNummern, für Prüfungen */
	private KontoNrVector	mKontoNr = null;

    private JTable 			mKontoTable = new JTable();

	/**
	 * KontoListScrollPane constructor.
	 * startet initialiserung des Frames
	 */
	public KontoListDialog(BuchungEingabe eingabe) {
		super();
		mEingabe = eingabe;
		// title resizable closable maximizable iconifiable
		mKontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);
		// beim Data als Observer anmelden
		//mKtoData.addKtoObserver(this);
//		init();
	}

	/**
	 * Initialisieren aller Ressourcen.
	 */
	public void init() {
		initTableKonto();
		initKontoList();
		initListener();

//		this.setSize(250, 400);
//		this.setLocation(100, 20);
	}

	/** Initialisierung des Anzeige-Bereiches: Kontoliste in ScrollPane
	 * in einem DesktopPane.
	 */
	private void initKontoList() {
		Trace.println(4,"KontoListDialog.initKontoList()");

		mKontoDialog = new JDialog();
		mKontoScrollPane = new JScrollPane(mKontoTable);
		mKontoDialog.add(mKontoScrollPane);

		mKontoDialog.setTitle("Konto Liste");
		setKontoDialogSize();
	}


	/** Initialisierung der Tabelle für Konto mit dem Model
	 */
	private void initTableKonto() {
		Trace.println(3,"KontoListDialog.initTable()");
		// ----- die Tabelle mit dem Model
		mKontoTableModel = new KontoModel();
		mKontoTable.setModel(mKontoTableModel);
		mKontoTable.setFont(Config.fontText);
		mKontoTable.setRowHeight(Config.windowTextSize + 4);
		mKontoTableModel.addTableModelListener(mKontoTable);
		// die Breite der Cols setzen
		TableColumn column = null;
		for (int i = 0; i <= 1; i++) {
			column = mKontoTable.getColumnModel().getColumn(i);
			switch (i) {
				case 0: column.setPreferredWidth(50);   break;
				case 1: column.setPreferredWidth(200);   break;
				default:
					column.setMaxWidth(100);
					column.setPreferredWidth(80);
			}
		}
		mKontoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mKontoNr = new KontoNrVector();

		// wenn eine Zeile selektiert wurde
		mKontoTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int index = mKontoTable.getSelectedRow();
				Object kontoNr = mKontoTableModel.getValueAt(index, 0);
				mEingabe.kontoSelected((Integer)kontoNr);
				Trace.println(6, "KontoListScrollPane.ListSelectionListener, Index: " + index);
			}
		});
	}


	public JDialog getDialog() {
		return mKontoDialog;
	}

	/** Grösse und Position der Kontoliste berechnen */
	private void setKontoDialogSize() {
		mKontoDialog.setSize(270, 440);
		mKontoScrollPane.setSize(250, 400);
		mKontoScrollPane.setLocation(0, 0);
	}

	/**
	 * Listener, wenn Zahl eingegeben wrid, sprint zur entsprechenden Kontonummer
	 */
	private void initListener() {
		mKontoTable.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent event) {
				Trace.println(5,"KontoListDialog.keyTyped");
				char xx = event.getKeyChar();
				Trace.println(5,"Char: " + xx);
				String kontoNr = String.valueOf(xx);
				try {
					int zeile = checkNr(kontoNr);
					mKontoTable.changeSelection(zeile, 0, false, false);
				}
				catch (KontoNotFoundException ex) {
					// wenn keine Zahl, nix tun
				}					
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// funktioniert nicht, da Fokus auf diesem Dialog
//				Trace.println(5,"KontoListDialog.keyReleased");
//				int kCode=e.getKeyCode();
//				Trace.println(5,"KeyCode: " + kCode);
//				// wenn Tab-Taste gedrückt
//				if (kCode == 9) {
//					mEingabe.selectNextField();
//				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
//				Trace.println(5,"KontoListDialog.keyPressed");	
			}
		});
	}
	
	

	/** Setzt den Cursor auf das entsprechende Konto.
	 * @param ktoNr kann auch nur ein Teil der Nr enthalten.
	 * @return wenn die Nummer eindeutig vervollständigt werden kann,
	 *  wird diese zurückgegeben, sonst der korrekte Teil
	 */
	public String selectRow (String ktoNr) throws KontoNotFoundException {
		Trace.println(4,"KontoListDialog.selectRow(ktoNr:" + ktoNr +")");
		// prüfen, ob eine gültige KontoNr
		int rowNr = checkNr(ktoNr);
		// Scrollbar berechnen und bewegen
		BoundedRangeModel scrollRange = mKontoScrollPane.getVerticalScrollBar().getModel();
		float faktor = (float)rowNr / (float)mKontoNr.size();
		int max = scrollRange.getMaximum();
		int value = Float.valueOf(max * faktor).intValue();
		//value -= scrollRange.getExtent();
		scrollRange.setValue(value);
		// bewegt die scrollbar, wenn ganz oben = 0
		mKontoScrollPane.getVerticalScrollBar().setValue(value);
		mKontoTable.setRowSelectionInterval(rowNr, rowNr);

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
		return mKontoTable;
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
