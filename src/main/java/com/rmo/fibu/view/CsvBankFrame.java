package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.CsvBank;
import com.rmo.fibu.model.CsvBankData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.KontoNrVector;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

/**
 * Liest CSV Keywords ein, schreibt diese in Tabelle. "CSV-datei selektieren":
 * Eine Auswahl von dateien anzeigen, wenn ein File selektiert, wird das file an
 * CsvReaderTableFrame weitergegeben.
 */
public class CsvBankFrame extends JFrame {

	private static final long serialVersionUID = 1310342465892805867L;
	/** Die Breite der Columns */
	private static final int DEFAULT_WIDTH = 2;
	private static final int PATH_WIDTH = 20;

	// view elemente
	private BuchungView mParent = null;

	// view der tabelle
	private JTable mTableView = null;
	/** Das interne Model zur Tabelle */
	private CsvBankModel mTableModel;
	/** Die Daten in der DB */
	private CsvBankData mBankData = null;

	/**
	 * Wird gestartet von Buchungen für Einstellungen.
	 *
	 * @param pParent Referenz zu den Buchungen
	 */
	public CsvBankFrame(BuchungView pParent) {
		super("Setup für CSV und PDF einlesen");
		mParent = pParent;
		init();
	}

	/**
	 * Start der Initialisierung, muss von jedem Konstruktor aufgerufen werden.
	 */
	private void init() {
		Trace.println(1, "CsvBankFrame.init()");
		// Verbindung zur DB
		mBankData = (CsvBankData) DataBeanContext.getContext().getDataBean(CsvBankData.class);
//		datenEinlesen();
		initView();
		this.setVisible(true);
	}

	/**
	 * Initialisierung der verschiedenen Views
	 */
	private void initView() {
		Trace.println(5, "CsvReaderFrame.initView()");
		getContentPane().add(initTable(), BorderLayout.CENTER);
		getContentPane().add(initBottom(), BorderLayout.PAGE_END);
		setSize(Config.winCsvSetupDim);
		setLocation(Config.winCsvSetupLoc);
	}

	/**
	 * Tabelle für die Anzeige setzen
	 *
	 * @return
	 */
	private Container initTable() {
		mTableModel = new CsvBankModel();

		mTableView = new JTable(mTableModel);
		mTableView.getTableHeader().setFont(Config.fontText);
		mTableView.setRowHeight(Config.windowTextSize + 4);
		mTableView.setFont(Config.fontText);

		JScrollPane lScrollPane = new JScrollPane(mTableView);
		setColWidth();
		setColKontoNummern();
		setColDocType();
		return lScrollPane;
	}

	/**
	 * Die Breite der Cols setzen
	 */
	private void setColWidth() {
		TableColumn column = null;
		for (int i = 0; i < mTableView.getColumnCount(); i++) {
			column = mTableView.getColumnModel().getColumn(i);
			switch (i) {
			case 3:
				column.setPreferredWidth(PATH_WIDTH * Config.windowTextSize);
				break;
			default:
				column.setPreferredWidth(DEFAULT_WIDTH * Config.windowTextSize);
			}
		}
	}

	/**
	 * Setzt die Kontonummern Combobox
	 */
	private void setColKontoNummern() {
		JComboBox<String> comboBoxKtonr = new JComboBox<>();
		comboBoxKtonr.setModel(new DefaultComboBoxModel<>(new KontoNrVector()));
		comboBoxKtonr.setFont(Config.fontText);
		TableColumn ktoColumn = mTableView.getColumnModel().getColumn(2);
		ktoColumn.setCellEditor(new DefaultCellEditor(comboBoxKtonr));
	}

	/**
	 * Setzt die docType Combobox
	 */
	private void setColDocType() {
		JComboBox<String> comboBoxDocType = new JComboBox<>();
		comboBoxDocType.setModel(new DefaultComboBoxModel<>(CsvBank.docTypes));
		comboBoxDocType.setFont(Config.fontText);
		TableColumn docColumn = mTableView.getColumnModel().getColumn(4);
		docColumn.setCellEditor(new DefaultCellEditor(comboBoxDocType));
	}

	/**
	 * Buttons setzen
	 *
	 * @return
	 */
	private Container initBottom() {
		JPanel lPanel = new JPanel(new GridLayout(0, 1));

		JPanel flow1 = new JPanel(new FlowLayout());

		JButton btnAdd = new JButton("Neuer Eintrag");
		btnAdd.setFont(Config.fontTextBold);

		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addAction();
			}
		});
		flow1.add(btnAdd);

		JButton btnSetup = new JButton("PDF Steuerdaten eingeben");
		btnSetup.setFont(Config.fontTextBold);

		btnSetup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setupAction();
			}
		});
		flow1.add(btnSetup);

		JButton btnSave = new JButton("Speichern");
		btnSave.setFont(Config.fontTextBold);
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				saveAction();
			}
		});
		flow1.add(btnSave);
		lPanel.add(flow1);

		JButton btnDelete = new JButton("Löschen");
		btnDelete.setFont(Config.fontTextBold);
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteAction(e);
			}
		});
		flow1.add(btnDelete);

		return lPanel;
	}

	/**
	 * Einen Eintrag in der DB dazufügen
	 */
	private void addAction() {
		CsvBank lBank = new CsvBank();
		lBank.setBankID(0);
		lBank.setBankName(" ");
		lBank.setKontoNrDefault(" ");
		lBank.setDirPath(" ");
		try {
			mBankData.addData(lBank);
			mTableModel.fireTableDataChanged();
		} catch (FibuException ex2) {
			JOptionPane.showMessageDialog(this, ex2.getMessage(), "\"Fehler in DB", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	/**
	 * Die Setup Daten eingeben
	 */
	private void setupAction() {
		// selektierte row
		int selRow = mTableView.getSelectedRow();
		if (selRow < 0) {
			JOptionPane.showMessageDialog(this, "Einen Eintrag selektieren", "PDF Setup",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		try {
			CsvBank lBank = mTableModel.readAt(selRow);
			if (lBank.getDocType() == CsvBank.docTypePdf) {
				PdfSetupFrame csvSetupFrame = new PdfSetupFrame(lBank);
				csvSetupFrame.setVisible(true);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "PDF setup", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Einen Eintrag löschen
	 */
	private void deleteAction(ActionEvent e) {
		int rowNr = mTableView.getSelectedRow();
		if (rowNr >= 0) {
			try {
				CsvBank lBank = mTableModel.readAt(rowNr);
				int answer = JOptionPane.showConfirmDialog(this, lBank.getBankName() + " wirklich löschen?",
						"Eintrag löschen", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.NO_OPTION) {
					return;
				}
				// hier YES
				mBankData.deleteRow(lBank);
				mTableModel.fireTableDataChanged();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Fehler beim löschen", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/** wenn Fenster geschlossen */
	@Override
	public void setVisible(boolean b) {
		if (!b) {
			Config.winCsvSetupDim = getSize();
			Config.winCsvSetupLoc = getLocation();
			mParent.resetCsvSetupFrame();
		}
		super.setVisible(b);
	}

// ----- Model der Keyword-Tabelle ---------------------------------------------

	/** Schnittstelle zum Daten-Objekt KontoData */
	private class CsvBankModel extends AbstractTableModel {

		private static final long serialVersionUID = -3805602970105237582L;

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return mBankData.getRowCount();
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "BankID";
			case 1:
				return "Bank Name";
			case 2:
				return "KontoNr";
			case 3:
				return "Dir path";
			case 4:
				return "Typ";
			}
			return "";
		}

		/** Steuert das aussehen einer Spalte */
//		@Override
//		public Class<?> getColumnClass(int col) {
//			return getValueAt(0, col).getClass();
//		}

		/**
		 * Alle Zellen 0..1 können nicht editiert werden.
		 */
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
//			if (columnIndex > 0) {
//				return true;
//			}
			return true;
		}

		/**
		 * Wenn eine Zelle edititer wurde, diese speichern
		 */
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			CsvBank lBank = null;
			try {
				lBank = mBankData.readAt(rowIndex);
			} catch (FibuException ex) {
				return;
			}
			if (columnIndex == 0) {
				String i = (String) aValue;
				int x = Integer.valueOf(i);
				lBank.setBankID(x);
			} else if (columnIndex == 1) {
				lBank.setBankName((String) aValue);
			} else if (columnIndex == 2) {
				lBank.setKontoNrDefault((String) aValue);
			} else if (columnIndex == 3) {
				lBank.setDirPath((String) aValue);
			} else if (columnIndex == 4) {
				lBank.setDocString((String) aValue);
			}

			try {
				mBankData.updateAt(rowIndex, lBank);
				mTableModel.fireTableDataChanged();
			} catch (FibuException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "Suchwort ändern", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		/**
		 * Den Eintrag an der Stelle position (0..x) zurückgeben.
		 *
		 * @return Bank an der position, null wenn nicht vorhanden
		 */
		public CsvBank readAt(int row) throws FibuException {
			Trace.println(7, "CsvBank.readAt()");
			try {
				CsvBank lBank = mBankData.readAt(row);
				return lBank;
			} catch (FibuException ex) {
				Trace.println(3, "CsvBank.readAt() " + ex.getMessage());
			}
			return null;
		}

		/**
		 * Gibt den Wert an der Koordinate row / col zurück.
		 */
		@Override
		public Object getValueAt(int row, int col) {
			Trace.println(7, "CsvKeywordModel.getValueAt(" + row + ',' + col + ')');
			try {
				CsvBank lBank = mBankData.readAt(row);
				switch (col) {
				case 0:
					return lBank.getBankID();
				case 1:
					return lBank.getBankName();
				case 2:
					return lBank.getKontoNrDefault();
				case 3:
					return lBank.getDirPath();
				case 4:
					return lBank.getDocString();
				}
			} catch (FibuException ex) {
				Trace.println(3, "getValueAt() " + ex.getMessage());
				return " ";
			}
			return "";
		}
	}

}
