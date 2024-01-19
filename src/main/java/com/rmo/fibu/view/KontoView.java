package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.DatumFormat;
import com.rmo.fibu.util.ExcelExportKonto;
import com.rmo.fibu.util.TablePrinter;
import com.rmo.fibu.util.TablePrinterModel;
import com.rmo.fibu.util.Trace;

/**
 * Die View aller Buchungen eines Kontos. Links ist die Liste mit allen Konti. Oben die
 * Bedienungselemente. Mitte die Liste der gefundenen Buchungen.
 */
public class KontoView extends JFrame
	implements TablePrinterModel, TableModelListener  {
	private static final long serialVersionUID = -1758211182591434071L;

	/** Die Breite der KontoListe */
	private static final int KTONR_WIDTH = 4;
	private static final int KTOTEXT_WIDTH = 16;

	private final Dimension datumAbSize = new Dimension(8 * Config.windowTextSize, Config.windowTextSize + 12);


	/** Die Models zu dieser View Verbindung zur DB */
	// private BuchungData mBuchungData = null;
	private KontoData 		mKontoData = null;
	/** Das Model zur Konto-Tabelle */
	private KontoModel 		mKontoModel;
	private ListSelectionModel mKontoCellSelection;
	private boolean 		mKontoSelected = false;

	/** Tabelle für die Anzeige der Konti */
	private JTable 		mKontoTable;
	/** Tabelle für die Anzeige der Buchungen, enthält alle Buchungen */
	private BuchungenKontoFrame	mBuchungenFrame;

	// ---- die TextFelder für die Eingabe des Datums
	private JTextField mTfDatumAb; // ab Datum

	// ----- die Buttons
	private JButton mButtonShow;
	private JButton mButtonSort;
	private JButton mButtonBuchung;
	private JButton mButtonPrint;
	private JButton mButtonExcel;
	private JButton mButtonClose;

	/** Die Kontonummer, deren Buchungen angezeigt werden */
	private int mKontoNrShow = -1;

	/**
	 * KontoView constructor.
	 *
	 * @param title
	 *            String
	 */
	public KontoView() {
		super("Kontoblatt V2.0");
		init();
	}

	// ----- Initialisierung ------------------------------------------------
	/**
	 * Start der Initialisierung, muss von jedem Konstruktor aufgerufen werden.
	 */
	private void init() {
		Trace.println(1, "KontoView.init()");
		// mBuchungData = (BuchungData)
		// DataBeanContext.getContext().getDataBean(BuchungData.class);
		mKontoData = (KontoData) DataBeanContext.getContext().getDataBean(
				KontoData.class);
		initView();
		enableButtons();
	}

	/**
	 * Initialisierung der verschiedenen Views
	 */
	private void initView() {
		Trace.println(2, "KontoView.initView()");
		getContentPane().add(initButtons(), BorderLayout.PAGE_START);
		getContentPane().add(initListArea(), BorderLayout.CENTER);
		setSize(Config.winKontoblattDim);
		setLocation(Config.winKontoblattLoc);
	}

	/**
	 * Initialisierung der Bedienung Nord (oben) Datum, Buttons: Anzeigen,
	 * drucken, schliessen
	 */
	private Container initButtons() {
		Trace.println(3, "KontoView.initButtonTop()");
		JPanel lPanel = new JPanel();
		// --- Ab Datum
		JLabel labelDatum = new JLabel("ab Datum:");
		labelDatum.setFont(Config.fontTextBold);
		lPanel.add(labelDatum);
		mTfDatumAb = new JTextField(Config.sDatumVon.toString());
		mTfDatumAb.setFont(Config.fontTextBold);
		mTfDatumAb.setPreferredSize(datumAbSize);
		lPanel.add(mTfDatumAb);
		// --- Show-Button
		mButtonShow = new JButton("Anzeigen");
		mButtonShow.setFont(Config.fontTextBold);
		lPanel.add(mButtonShow);
		mButtonShow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mBuchungenFrame.actionShowBuchnungen(getSelectedKontoNr(), getSelectedDate() );
			}
		});
		// --- Sort-Button
		mButtonSort = new JButton("Sortieren");
		mButtonSort.setFont(Config.fontTextBold);
		lPanel.add(mButtonSort);
		mButtonSort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mBuchungenFrame.actionBuchungenSortieren(mKontoNrShow, getSelectedDate());
			}
		});

		// --- Buchung ändern
		mButtonBuchung = new JButton("Buchung ändern");
		mButtonBuchung.setFont(Config.fontTextBold);
		lPanel.add(mButtonBuchung);
		mButtonBuchung.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mBuchungenFrame.actionBuchungAendern();
			}
		});

		// --- ExcelExport-button
		mButtonExcel = new JButton("Excel export");
		mButtonExcel.setFont(Config.fontTextBold);
		lPanel.add(mButtonExcel);
		mButtonExcel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionExcelExport();
			}
		});

		// --- Print-button
		mButtonPrint = new JButton("Drucken");
		mButtonPrint.setFont(Config.fontTextBold);
		lPanel.add(mButtonPrint);
		mButtonPrint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionPrint();
			}
		});
		// --- Close-button
		mButtonClose = new JButton("Schliessen");
		mButtonClose.setFont(Config.fontTextBold);
		lPanel.add(mButtonClose);
		mButtonClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionClose();
			}
		});
		return lPanel;
	}

	/** Initialisiert die Areas mit den Konto- und BuchungListe */
	private Container initListArea() {
		mBuchungenFrame = new BuchungenKontoFrame(this);
		JSplitPane lSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				initTableKonto(), mBuchungenFrame.initTableBuchung());
		lSplitPane.setOneTouchExpandable(true);
		lSplitPane.setDividerLocation( ((KTONR_WIDTH + KTOTEXT_WIDTH) * Config.windowTextSize) );
//				+ lSplitPane.getInsets().left);

		mBuchungenFrame.scrollToLastEntry();
		return lSplitPane;
	}

	/**
	 * Initialisierung der Tabelle für die Anzeigen aller Konto. Die Tabell wird
	 * mit dem Model verknüpft, das in der innerClass KontoModel implementiert
	 * ist.
	 */
	private Component initTableKonto() {
		Trace.println(3, "KontoView.initTable()");
		// ----- die Tabelle mit dem Model
		mKontoModel = new KontoModel();
		mKontoTable = new JTable(mKontoModel);
		mKontoTable.setFont(Config.fontText);
		mKontoTable.getTableHeader().setFont(Config.fontText);
		mKontoTable.setRowHeight(Config.windowTextSize + 4);
		mKontoModel.addTableModelListener(mKontoTable);
		// die Breite der Cols
		TableColumn column = null;
		for (int i = 0; i < mKontoModel.getColumnCount(); i++) {
			column = mKontoTable.getColumnModel().getColumn(i);
			switch (i) {
			case 1:
				column.setPreferredWidth(KTOTEXT_WIDTH * Config.windowTextSize);
				break;
			default:
				column.setPreferredWidth(KTONR_WIDTH * Config.windowTextSize);
			}
		}
		JScrollPane lScrollPane = new JScrollPane(mKontoTable);
		lScrollPane.setPreferredSize(new Dimension(KTOTEXT_WIDTH * Config.windowTextSize, 100));
		lScrollPane.setMinimumSize(new Dimension(KTONR_WIDTH * Config.windowTextSize, 100));
		
		addListeners();
		return lScrollPane;
	}

	
	/**
	 * Die Anzeige der Buttons, 
	 */
	public void enableButtons() {
		if (! mKontoSelected) {
			mButtonShow.setEnabled(false);
			mButtonSort.setEnabled(false);
			mButtonExcel.setEnabled(false);
			mButtonPrint.setEnabled(false);
		}
		else {
			mButtonShow.setEnabled(true);
			mButtonSort.setEnabled(true);
			mButtonExcel.setEnabled(true);
			mButtonPrint.setEnabled(true);
			
		}
		if (mBuchungenFrame.isBuchungSelected()) {
			mButtonBuchung.setEnabled(true);
		}
		else {
			mButtonBuchung.setEnabled(false);
		}		
	}

	private void addListeners() {
		mKontoTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() == 2) {
					mBuchungenFrame.actionShowBuchnungen(getSelectedKontoNr(), getSelectedDate() );
				}
			}
		});
		
		mKontoCellSelection = mKontoTable.getSelectionModel();
		mKontoCellSelection.addListSelectionListener(new ListSelectionListener() {
		      public void valueChanged(ListSelectionEvent e) {
		    	  int[] selectedRow = mKontoTable.getSelectedRows();
		    	  if (selectedRow.length > 0) {
		    		  mKontoSelected = true;
		    	  }
		    	  else {
		    		  mKontoSelected = false;
		    	  }
		    	  enableButtons();
		      }	    	  
		});		

	}


	// ----- Implementierung des TablePrinterModels -------------------

	/** Die Tabelle, die gedruckt werden soll */
	@Override
	public JTable getTableToPrint() {
		return mBuchungenFrame.getBuchungTable();
	}

	/** Die Anzahl Kopfzeilen */
	@Override
	public int getHeaderCount() {
		return 1;
	}

	/**
	 * Die Kopfzeile, wird linksbündig angezeigt, Seitenzahl rechts
	 */
	@Override
	public String getHeader(int nr) {
		try {
			Konto lKonto = mKontoData.read(mKontoNrShow);
			return mKontoNrShow + "  " + lKonto.getText();
		} catch (KontoNotFoundException ex) {
			return mKontoNrShow + " Name nicht gefunden";
		}
	}

	/** Die max. Anzahl Spalten */
	@Override
	public int getColCount() {
		return 7;
	}

	/**
	 * Ein Array mit der gleichen Länge wie Anzahl Spalten wird erwartet. Die
	 * Spaltenbreiten werden relativ angegeben, diese werden der Seitenbreite
	 * angepasst.
	 */
	@Override
	public int getColSize(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return 16;
		case 1:
			return 10;
		case 2:
			return 50;
		case 3:
			return 10;
		default:
			return 20;
		}
	}

	/** Die Spalten, die eine Summen enthalten sollen */
	@Override
	public boolean getColSumme(int columnIndex) {
		if (columnIndex == 4 || columnIndex == 5)
			return true;
		else
			return false;
	}

	/**
	 * Die Spalten, die rechtsbündig gedruckt werden. Zahlen werden automatisch
	 * rechtsbündig gedruckt, hier angeben, wenn Ueberschrift auch rechtsbündig
	 * sein soll
	 */
	@Override
	public boolean getColRight(int columnIndex) {
		if ((columnIndex == 1) || (columnIndex >= 4))
			return true;
		return false;
	}

	/** Die überschrift einer Spalte der Liste */
	@Override
	public String getColName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Datum";
		case 1:
			return "Beleg";
		case 2:
			return "Text";
		case 3:
			return "Gegenkonto";
		case 4:
			return "Soll";
		case 5:
			return "Haben";
		case 6:
			return "Saldo";
		default:
			return "";
		}
	}

	// ----- Behandlung der Events ------------------------------------


	/**
	 * Excel-Button wurde gedrückt.
	 */
	private void actionExcelExport() {
		Trace.println(3, "KontoView.actionExcelPerformed()");
		// Printer aufrufen, Daten siehe Interface: TablePrinterModel
		ExcelExportKonto lExcel = new ExcelExportKonto(this.getTableToPrint().getModel());
		try {
			lExcel.doExport(mKontoNrShow);
			String msg = "Exportiert nach: " + Config.sDefaultDir;
			JOptionPane.showMessageDialog(this, msg, "Excel Export",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage(),
					"Excel Export", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Print-Button wurde gedrückt.
	 */
	private void actionPrint() {
		Trace.println(3, "KontoView.actionPrintPerformed()");
		// Printer aufrufen, Daten siehe Interface: TablePrinterModel
		TablePrinter lPrinter = new TablePrinter(this);
		try {
			lPrinter.doPrint();
		} catch (PrinterException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(),
					"Drucken, Fehler", JOptionPane.ERROR_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(),
					"Drucken, Fehler", JOptionPane.ERROR_MESSAGE);

		}
	}

	
	/**
	 * Close-Button wurde gedrückt.
	 */
	private void actionClose() {
		this.setVisible(false);
	}

	
	/**
	 * Die selektierte Kontonummer setzen
	 * @return
	 */
	private int getSelectedKontoNr() {
		int row = mKontoTable.getSelectedRow();
		if (row >= 0) {
			mKontoNrShow = ((Integer) mKontoTable.getModel().getValueAt(row, 0))
					.intValue();
		}
		return mKontoNrShow;
	}

	/**
	 * Das selektierte Datum ab dem Buchungen angezeigt werden.
	 */
	private Date getSelectedDate() {
		Date datum = null;
		if (mTfDatumAb.getText().length() == 0) {
			mTfDatumAb.setText(Config.sDatumVon.toString());
		}
		DatumFormat df = DatumFormat.getDatumInstance();
		try {
			datum = df.parse(mTfDatumAb.getText());
			return datum;
		} catch (ParseException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(),
					"Datum fehlerhaft", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}



// ----- Model der Konto-Tabelle ----------------------------

	/** Schnittstelle zum Daten-Objekt KontoData */
	private class KontoModel extends AbstractTableModel {

		private static final long serialVersionUID = -3805602970105237582L;

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return mKontoData.getRowCount();
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Nummer";
			case 1:
				return "Konto";
			}
			return "";
		}

		/** Steuert das aussehen einer Spalte */
		@Override
		public Class<?> getColumnClass(int col) {
			return getValueAt(0, col).getClass();
		}

		/*
		 * if (col == 0) return Integer.valueOf(0).getClass(); else return new
		 * String().getClass(); }
		 */

		/**
		 * Gibt den Wert an der Koordinate row / col zurück.
		 */
		@Override
		public Object getValueAt(int row, int col) {
			Trace.println(7, "KontoModel.getValueAt(" + row + ',' + col + ')');
			try {
				Konto lKonto = mKontoData.readAt(row);
				switch (col) {
				case 0:
					return Integer.valueOf(lKonto.getKontoNr());
				case 1:
					return lKonto.getText();
				}
			} catch (KontoNotFoundException ex) {
				ex.printStackTrace(Trace.getPrintWriter());
			}
			return "";
		}
	}


	@Override
	public void tableChanged(TableModelEvent e) {
	}

	public void repaintBuchungen() {
		mBuchungenFrame.repaintBuchungen();
	}

	/**
	 * Die Anzeige der Buchungen
	 * @return
	 */
	public BuchungenBaseFrame getBuchungenFrame() {
		return mBuchungenFrame;
	}


	/****************************************
	 * für den Test der view
	 */
//	public static void main(String[] args) {
//		try {
//			DbConnection.open("FibuLeer");
//			KontoView lKonto = new KontoView();
//			lKonto.setVisible(true);
//		} catch (FibuRuntimeException ex) {
//		}
//	}

}// endOfClass
