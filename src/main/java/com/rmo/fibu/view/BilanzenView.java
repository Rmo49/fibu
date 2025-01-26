package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoCalculator;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.util.BilanzPrinter;
import com.rmo.fibu.util.BilanzPrinterModel;
import com.rmo.fibu.util.BilanzPrinterWerte;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;
import com.rmo.fibu.view.util.BetragRenderer;
import com.rmo.fibu.view.util.IntegerRenderer;

/**
 * Auswertung: Eröffnungsbilanz, Bilanz, ER. Ein Frame mit CardLayout für jeden
 * Typ. Wurde mit JBuilder-Designer erstellt!
 */
public class BilanzenView extends JFrame { // implements Printable
	private static final long serialVersionUID = 387966003598525362L;
	public final static int ONE_SECOND = 1000;
	/** Die DB zu dieser View */
	private KontoData mKontoData = null;
	/** Die Models zu dieser View */
	private AuswertungModel modelBilanz;
	private AuswertungModel modelER;
	private AuswertungModel modelStartBilanz;
	JTabbedPane tabPane = new JTabbedPane();


	// private DecimalFormat mFormat = new DecimalFormat("###,###.00");
	private int mSizeOfNumbers = 10;

	BorderLayout borderLayout1 = new BorderLayout();
	JButton btnPrint = new JButton();
	JButton btnClose = new JButton();
	JButton btnUpdate = new JButton();

	JTable tableStartBilanz = new JTable();
	JScrollPane scrollStartBilanz = new JScrollPane();
	JScrollPane scrollBilanz = new JScrollPane();
	JTable tableBilanz = new JTable();
	JScrollPane scrollER = new JScrollPane();
	JTable tableER = new JTable();

	/** Default Konstruktor */
	public BilanzenView() {
		super("Bilanzen");
		try {
			initView();
			initData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialisierung der verschiedenen Views
	 */
	private void initView() {
		getContentPane().add(initButton(), BorderLayout.NORTH);
		getContentPane().add(initTables(), BorderLayout.CENTER);
		setSize(Config.winBilanzenDim);
		setLocation(Config.winBilanzenLoc);
	}


	/** Methode von JBuilder generiert */
	private Container initButton() {
		this.getContentPane().setLayout(borderLayout1);
		JToolBar jToolBar1 = new JToolBar();

		btnPrint.setText("Drucken");
		btnPrint.setFont(Config.fontTextBold);
		btnPrint.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnPrint_action(e);
			}
		});
		btnClose.setText("Schliessen");
		btnClose.setFont(Config.fontTextBold);
		btnClose.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnClose_action(e);
			}
		});
		btnUpdate.setActionCommand("Berechnen");
		btnUpdate.setFont(Config.fontTextBold);
		btnUpdate.setText("Berechnen");
		btnUpdate.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnUpdate_action(e);
			}
		});
		jToolBar1.add(btnUpdate, null);
		jToolBar1.add(btnPrint, null);
		jToolBar1.add(btnClose, null);
		return jToolBar1;
	}

	private Container initTables() {
		tableStartBilanz.setShowHorizontalLines(false);
		scrollBilanz.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tableBilanz.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		tableBilanz.setCellSelectionEnabled(true);
		tableBilanz.setColumnSelectionAllowed(false);
		tableBilanz.setRowSelectionAllowed(false);
		tableBilanz.setShowHorizontalLines(false);
		tableER.setShowHorizontalLines(false);
		tabPane.add(scrollStartBilanz, "StartBilanz");

		tabPane.add(scrollBilanz, "Bilanz");
		tabPane.add(scrollER, "Erfolgsrechnung");
		tabPane.setFont(Config.fontTextBold);

		scrollER.getViewport().add(tableER, null);
		scrollBilanz.getViewport().add(tableBilanz, null);
		scrollStartBilanz.getViewport().add(tableStartBilanz, null);
		return tabPane;
	}

	/** Initialisierung der Daten (Tabellen) */
	/**
	 *
	 */
	private void initData() {
		// die Verbindung zur DB
		mKontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);
		// ----- Bilanz
		modelBilanz = new AuswertungModel(Config.sBilanzStart, Config.sBilanzEnd, false);
		modelBilanz.setUpData();
		tableBilanz.setModel(modelBilanz);
		tableBilanz.setFont(Config.fontText);
		tableBilanz.setRowHeight(Config.windowTextSize + 4);
		tableBilanz.getTableHeader().setFont(Config.fontText);
		modelBilanz.addTableModelListener(tableBilanz);
		setUpColumnSize(tableBilanz);
		// Den default-Renderer für Spalten mit Double-Werten
		tableBilanz.setDefaultRenderer(Double.class, new BetragRenderer());
		tableBilanz.setDefaultRenderer(Integer.class, new IntegerRenderer());
		// ----- ER Erfolgsrechung
		modelER = new AuswertungModel(Config.sERStart, Config.sEREnd, false);
		modelER.setUpData();
		tableER.setModel(modelER);
		tableER.setFont(Config.fontText);
		tableER.setRowHeight(Config.windowTextSize + 4);
		tableER.getTableHeader().setFont(Config.fontText);
		modelER.addTableModelListener(tableER);
		setUpColumnSize(tableER);
		// Den default-Renderer für Spalten mit Double-Werten
		tableER.setDefaultRenderer(Double.class, new BetragRenderer());
		tableER.setDefaultRenderer(Integer.class, new IntegerRenderer());
		// ----- StartBilanz
		modelStartBilanz = new AuswertungModel(Config.sBilanzStart, Config.sBilanzEnd, true);
		modelStartBilanz.setUpData();
		tableStartBilanz.setModel(modelStartBilanz);
		tableStartBilanz.setFont(Config.fontText);
		tableStartBilanz.setRowHeight(Config.windowTextSize + 4);
		tableStartBilanz.getTableHeader().setFont(Config.fontText);
		modelStartBilanz.addTableModelListener(tableStartBilanz);
		setUpColumnSize(tableStartBilanz);
		// Den default-Renderer für Spalten mit Double-Werten
		tableStartBilanz.setDefaultRenderer(Double.class, new BetragRenderer());
		tableStartBilanz.setDefaultRenderer(Integer.class, new IntegerRenderer());
	}

	/**
	 * Die Spaltenbreite der Tabellen setzen Ist für alle gleich
	 */
	private void setUpColumnSize(JTable table) {
		TableColumn column = null;
		for (int i = 0; i < table.getColumnCount(); i++) {
			column = table.getColumnModel().getColumn(i);
			switch (i) {
			case 1:
				column.setPreferredWidth(300);
				break;
			case 2:
			case 3:
				column.setPreferredWidth(100);
				break;
			default:
				column.setPreferredWidth(50);
			}
		}
	}

	/**
	 * Wird vom PrintHandler aufgerufen. Die Methode muss jede einzelne Seite
	 * unabhängig berechnen, bzw. ausgeben können.
	 */
//	@Override
//	public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
//		Trace.println(3, "BilanzenView.print()");
//		if (pageIndex >= 1) return Printable.NO_SUCH_PAGE;
//		Graphics2D g2 = (Graphics2D) g;
//		g2.translate(pf.getImageableX(), pf.getImageableY());
//		switch (tabPane.getSelectedIndex()) {
//		case 0:
//			printPage(g2, pf, "Start-Bilanz per ", tableStartBilanz);
//			break;
//		case 1:
//			printPage(g2, pf, "Bilanz per ", tableBilanz);
//			break;
//		case 2:
//			printPage(g2, pf, "Erfolgsrechnung per ", tableER);
//			break;
//		}
//		return Printable.PAGE_EXISTS;
//	}

	/**
	 * Druckt eine Auswertungsseite
	 *
	 * @todo 3 später mit mehreren Seiten (BasePrinter verwenden)
	 */
//	private void printPage(Graphics2D g2, PageFormat pf, String title, JTable table) {
//		g2.setFont(Config.printerTitelFont);
//		float y = Config.printerTitelFont.getSize2D();
//		g2.drawString(title, 0, y);
//		g2.drawLine(0, (int) y + 4, (int) pf.getImageableWidth(), (int) y + 4);
//		g2.setFont(Config.printerNormalFont);
//		// der Rest + 30 Pixels tiefer
//		g2.translate(0, 30);
//		table.paint(g2);
//	}

	// ----- Das Model für die Tabellen ---------------------------------------
	/**
	 * Model enthält alle Informationen für die Auswertung. Auch Zeilen mit
	 * Summen und Striche etc.
	 */
	private class AuswertungModel extends AbstractTableModel {
		private static final long serialVersionUID = 4937461172246423789L;
		/** der Vector mit den einträgen */
		private Vector<KontoRow> mKonti = new Vector<>();
		/** Range der Konto */
		private int kontoNrVon;
		private int kontoNrBis;
		private boolean isStartBilanz;
		/** die Summe der Spalten */
		private double mSummeSoll = 0;
		private double mSummeHaben = 0;

		/** Die angezeigten Zeilen */
		private class KontoRow {
			public int kontoNr;
			public String text;
			public double sollBetrag = -1;
			public double habenBetrag = -1;
		}

		/** Konstruktor, bestimmt den Range und den Typ der Bilanz */
		public AuswertungModel(int kontoNrVon, int kontoNrBis, boolean isStartBilanz) {
			this.kontoNrVon = kontoNrVon;
			this.kontoNrBis = kontoNrBis;
			this.isStartBilanz = isStartBilanz;
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public int getRowCount() {
			return mKonti.size() + 6;
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
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

		/** Steuert das aussehen einer Spalte */
		@Override
		public Class<?> getColumnClass(int col) {
			return getValueAt(0, col).getClass();
		}

		/**
		 * Gibt den Wert an der Koordinate row / col zurück.
		 */
		@Override
		public Object getValueAt(int row, int col) {
			Trace.println(7, "BilanzenView.BuchungModel.getValueAt(" + row + ',' + col + ')');
			if (row < mKonti.size()) {
				KontoRow lRow = mKonti.elementAt(row);
				switch (col) {
				case 0:
					return Integer.valueOf(lRow.kontoNr);
				case 1:
					return lRow.text;
				case 2:
					return Double.valueOf(lRow.sollBetrag);
				case 3:
					return Double.valueOf(lRow.habenBetrag);
				}
			} else {
				// Den Summenblock darstellen
				int summeRow = row - mKonti.size();
				switch (summeRow) {
				case 0:
					return getValueStrich(col, '_');
				case 1:
					return getValueSumme(col);
				case 2:
					return getValueSaldo(col);
				case 3:
					return getValueStrich(col, '_');
				case 4:
					return getValueTotal(col);
				case 5:
					return getValueStrich(col, '=');
				}
			}
			return "";
		}

		/** Zeichnet eine Strich in der Spalte von Soll und Haben */
		private Object getValueStrich(int col, char strich) {
			switch (col) {
			case 0:
				return Integer.valueOf(0);
			case 2:
			case 3:
				return makeString(strich, mSizeOfNumbers);
			default:
				return "";
			}
		}

		/** Macht einen String der Länge length mit dem char zeichen */
		private Object makeString(char zeichen, int length) {
			StringBuffer buffer = new StringBuffer(length);
			for (int i = 0; i < length; i++) {
				buffer.append(zeichen);
			}
			return buffer.toString();
		}

		/** Gibt die erste Summe zurück */
		private Object getValueSumme(int col) {
			switch (col) {
			case 0:
				return Integer.valueOf(0);
			case 1:
				return "Summe";
			case 2:
				return Double.valueOf(mSummeSoll);
			case 3:
				return Double.valueOf(mSummeHaben);
			default:
				return "";
			}
		}

		/** Gibt den Saldo zurück */
		private Object getValueSaldo(int col) {
			double sollSaldo = -1; // -1 wird nie angezeigt
			double habenSaldo = -1;
			String text = null;
			if (mSummeSoll > mSummeHaben) {
				habenSaldo = mSummeSoll - mSummeHaben;
			} else {
				sollSaldo = mSummeHaben - mSummeSoll;
			}
			if (kontoNrVon < Config.sERStart) {
				if (habenSaldo > 0) {
					text = "Gewinn";
				} else {
					text = "Verlust";
				}
			} else {
				if (habenSaldo > 0) {
					text = "Verlust";
				} else {
					text = "Gewinn";
				}
			}
			switch (col) {
			case 0:
				return Integer.valueOf(0);
			case 1:
				return text;
			case 2:
				return Double.valueOf(sollSaldo);
			case 3:
				return Double.valueOf(habenSaldo);
			default:
				return "";
			}
		}

		/** Gibt das Total zurück */
		private Object getValueTotal(int col) {
			double total = (mSummeSoll > mSummeHaben) ? mSummeSoll : mSummeHaben;
			switch (col) {
			case 0:
				return Integer.valueOf(0);
			case 1:
				return "Total";
			case 2:
				return Double.valueOf(total);
			case 3:
				return Double.valueOf(total);
			default:
				return "";
			}
		}

		/**
		 * Baut die internen Liste der Konti auf und liest die Werte von der DB
		 * ein. Die Grenzen der Konti wird über den Konstruktor gesetzt.
		 */
		public void setUpData() {
			Trace.println(3, "BilanzenView.AuswertungModel.setUpData()");
			if (!mKonti.isEmpty()) {
				this.fireTableDataChanged();
				mKonti.clear();
			}
			mSummeSoll = 0;
			mSummeHaben = 0;
			Iterator<Konto> kontoIter = mKontoData.getIterator();
			while (kontoIter.hasNext()) {
				Konto lKonto = kontoIter.next();
				if (lKonto.getKontoNr() >= kontoNrVon && lKonto.getKontoNr() <= kontoNrBis) {
					addRow(lKonto);
				}
			}
			fireTableRowsInserted(0, mKonti.size() - 1);
		}

		/**
		 * Eine Zeile der Liste dazufügen. Die Summe der Spalten werden
		 * berechnet.
		 */
		private void addRow(Konto konto) {
			KontoRow lRow = new KontoRow();
			lRow.kontoNr = konto.getKontoNr();
			lRow.text = konto.getText();
			double betrag = isStartBilanz ? konto.getStartSaldo() : konto.getSaldo();
			if (konto.isSollKonto()) {
				mSummeSoll += betrag;
				lRow.sollBetrag = betrag;
			} else {
				mSummeHaben += betrag;
				lRow.habenBetrag = betrag;
			}
			mKonti.addElement(lRow);
		}
	}

	// ----- End of AuswertungModel
	// -----------------------------------------------

	/** Alle Saldi neu berechnen */
	private void btnUpdate_action(ActionEvent e) {
		int lCursorType = 0;
		try {
			lCursorType = this.getCursor().getType();
			// Cursor verändern während Berechnung
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			KontoCalculator calculator = new KontoCalculator();

			calculator.calculateSaldo();
			setUpBilanzen();
			repaintAll();
		} catch (FibuException pEx) {
			JOptionPane.showMessageDialog(this, pEx.getMessage(), "Fehler Kontorahmen", JOptionPane.ERROR_MESSAGE);
		} finally {
			this.setCursor(Cursor.getPredefinedCursor(lCursorType));
		}
	}

	/** Alle Tabellen neu anzeigen */
	private void repaintAll() {
		// modelBilanz.fireTableDataChanged();
		modelBilanz.fireTableRowsUpdated(0, modelBilanz.getRowCount() - 1);
		modelER.fireTableDataChanged();
		modelStartBilanz.fireTableDataChanged();
	}

	/** Fenster schliessen */
	private void setUpBilanzen() {
		modelStartBilanz.setUpData();
		modelBilanz.setUpData();
		modelER.setUpData();
	}

	/** Fenster schliessen */
	private void btnClose_action(ActionEvent e) {
		dispose();
	}

	/**
	 * Drucken einer Seite. Wenn ausgeführt, wird die Methode print aufgerufen
	 */
	private void btnPrint_action(ActionEvent event) {
//		BasePrinterModel tablePrinterModel = null;
//		String aktuellesDatum = null;
		// hier wird ein JOptionpane aufgerufen
//		aktuellesDatum = readAktuellesDatum();
//		if (aktuellesDatum == null) {
//			// wenn abbrechen gedrückt
//			return;
//		}

		//----- Dialog aufrufen
		BilanzenPrintDialog bpDialog = new BilanzenPrintDialog(this, "Bilanzen drucken", true);
		bpDialog.setVisible(true);

		// hier gehts weiter nach dem Dialog
		if (bpDialog.isCancelSelected()) {
			return;
		}

		BilanzPrinterModel bilanzPrinterModel = new BilanzPrinterModel();
		boolean eineSelektiert = false;

		if (bpDialog.getPosStartBilanz() > 0) {
			BilanzPrinterWerte werte = new BilanzPrinterWerte();
			werte.nrToPrint = bpDialog.getPosStartBilanz();
			werte.bilanzNr = 1;
			werte.title = "Start Bilanz per " + Config.sDatumVon;
			werte.tabelle = tableStartBilanz;
			bilanzPrinterModel.addBilanz(werte);
			eineSelektiert = true;
		}

		if (bpDialog.getPosBilanz() > 0) {
			BilanzPrinterWerte werte = new BilanzPrinterWerte();
			werte.nrToPrint = bpDialog.getPosBilanz();
			werte.bilanzNr = 2;
			werte.title = "Bilanz per " + bpDialog.getDatum();
			werte.tabelle = tableBilanz;
			bilanzPrinterModel.addBilanz(werte);
			eineSelektiert = true;
		}

		if (bpDialog.getPosER() > 0) {
			BilanzPrinterWerte werte = new BilanzPrinterWerte();
			werte.nrToPrint = bpDialog.getPosER();
			werte.bilanzNr = 3;
			werte.title = "Erfolgsrechnung per " + bpDialog.getDatum();
			werte.tabelle = tableER;
			bilanzPrinterModel.addBilanz(werte);
			eineSelektiert = true;
		}

		// wenn alle auf 0 stehen
		if (!eineSelektiert) {
			return;
		}

		//---- drucken
		BilanzPrinter lPrinter = new BilanzPrinter(bilanzPrinterModel);
		try {
			lPrinter.doPrint();
		} catch (PrinterException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Fehler beim Drucken", JOptionPane.ERROR_MESSAGE);
		}
	}


	@Override
	public void setVisible(boolean visible) {
		if (!visible) {
			Config.winBilanzenDim = getSize();
			Config.winBilanzenLoc = getLocation();
		}
		super.setVisible(visible);

	}


	/********************************
	 * Start für Einzeltest
	 */
	public static void main(String[] args) {
		try {
			DbConnection.open("FibuLeer");
			BilanzenView auswertung = new BilanzenView();
			auswertung.setVisible(true);
		} catch (FibuRuntimeException ex) {
		}
	}

}// endOfClass
