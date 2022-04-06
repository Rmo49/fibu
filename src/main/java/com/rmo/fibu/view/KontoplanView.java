package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
//import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.FibuData;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoCalculator;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.ExcelExportKontoPlan;
import com.rmo.fibu.util.ExcelImport;
import com.rmo.fibu.util.TextExportKontoPlan;
import com.rmo.fibu.util.Trace;
import com.rmo.fibu.view.util.DoubleRenderer;
import com.rmo.fibu.view.util.IntegerRenderer;

/**
 * Kontorahmen: Die View der Liste aller Konti. Die Konti werden in einer JTable
 * angezeigt. Ein Konto kann selektiert und bearbeitet werden. Die GUI wurde mit
 * dem Designer von JBuilder erstellt.
 * autor R. Moser
 */

public class KontoplanView extends JFrame // , Observer, BuchungListener
{
	private static final long serialVersionUID = 6885094022482697392L;
	private KontoData mKontoData = null; // das Data zu dieser View
	/** Das Model zur Tabelle */
	private KontoModel mKontoTableModel;

	JPanel jPanelSouth = new JPanel();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JTextField jTextFieldKontoNr = new JTextField();
	JTextField jTextFieldText = new JTextField();
	JCheckBox jCheckBoxSoll = new JCheckBox();
	JTextField jTextFieldStartSaldo = new JTextField();
	JPanel jPanelButtons = new JPanel();
	JButton jButtonClose = new JButton();
	JButton jButtonCancel = new JButton();
	JButton jButtonSave = new JButton();
	JButton jButtonRefresh = new JButton();
	JPanel jPanelNorth = new JPanel();
	JButton jButtonDelete = new JButton();
	JButton jButtonChange = new JButton();
	JButton btnExcelExport = new JButton();
	JButton btnExcelImport = new JButton();
	BorderLayout borderLayout1 = new BorderLayout();
	JScrollPane jKontoScroll = new JScrollPane();
	JTable jKontoTable = new JTable();

	/**
	 * KontoplanView constructor. startet initialiserung des Frames
	 */
	public KontoplanView() {
		super("Kontoplan V1.1");
		mKontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);
		// beim Data als Observer anmelden
		// mKtoData.addKtoObserver(this);
		initForm();
	}

	/**
	 * Initialisieren aller Ressourcen.
	 */
	private void initForm() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		initTableKonto();
//		setTitle("Kontoplan V1.0");
		
		setSize(Config.winKontoplanDim);
		setLocation(Config.winKontoplanLoc);

	}

	/**
	 * Initialisierung, wird von JBuilder generiert
	 */
	private void jbInit() throws Exception {
		jPanelSouth.setLayout(gridBagLayout1);
		jLabel1.setText("KontoNr");
		jLabel1.setFont(Config.fontText);
		jLabel2.setText("Text");
		jLabel2.setFont(Config.fontText);
		jLabel3.setText("SollKto");
		jLabel3.setFont(Config.fontText);
		jLabel4.setText("Start-Saldo");
		jLabel4.setFont(Config.fontText);
		
		jTextFieldKontoNr.setFont(Config.fontText);
		jTextFieldText.setFont(Config.fontText);
		jTextFieldStartSaldo.setFont(Config.fontText);
		jCheckBoxSoll.setFont(Config.fontText);
		// TODO wie die Grösse setzen abhöngig von TextGrösse
		jCheckBoxSoll.setSize(20,20);
		
		jButtonClose.setText("Schliessen");
		jButtonClose.setFont(Config.fontTextBold);
		jButtonClose.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jButtonClose_actionPerformed(e);
			}
		});
		jButtonCancel.setText("Abbrechen");
		jButtonCancel.setFont(Config.fontTextBold);
		jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jButtonCancel_actionPerformed(e);
			}
		});
		jButtonSave.setText("Speichern");
		jButtonSave.setFont(Config.fontTextBold);
		jButtonSave.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jButtonSave_actionPerformed(e);
			}
		});
		jButtonRefresh.setText("Refresh");
		jButtonRefresh.setFont(Config.fontTextBold);
		jButtonRefresh.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jButtonRefresh_actionPerformed(e);
			}
		});
		jButtonDelete.setText("Löschen");
		jButtonDelete.setFont(Config.fontTextBold);
		jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jButtonDelete_actionPerformed(e);
			}
		});
		jButtonChange.setToolTipText("ändern eines Eintrages");
		jButtonChange.setText("ändern");
		jButtonChange.setFont(Config.fontTextBold);
		jButtonChange.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jButtonChange_actionPerformed(e);
			}
		});
		btnExcelExport.setActionCommand("ExcelExport");
		btnExcelExport.setText("Export");
		btnExcelExport.setFont(Config.fontTextBold);
		btnExcelExport.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnExcelExport_actionPerformed(e);
			}
		});
		btnExcelImport.setActionCommand("ExcelImport");
		btnExcelImport.setText("Import");
		btnExcelImport.setFont(Config.fontTextBold);
		btnExcelImport.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnExcelImport_actionPerformed(e);
			}
		});

		this.getContentPane().setLayout(borderLayout1);
		this.getContentPane().add(jPanelSouth, BorderLayout.SOUTH);
		jPanelSouth.add(jLabel1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanelSouth.add(jLabel2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanelSouth.add(jLabel3, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanelSouth.add(jLabel4, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanelSouth.add(jTextFieldKontoNr, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanelSouth.add(jTextFieldText, new GridBagConstraints(2, 1, 1, 1, 20.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanelSouth.add(jCheckBoxSoll, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanelSouth.add(jTextFieldStartSaldo, new GridBagConstraints(4, 1, 1, 1, 5.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanelSouth.add(jPanelButtons, new GridBagConstraints(1, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanelButtons.add(jButtonSave, null);
		jPanelButtons.add(btnExcelExport, null);
		jPanelButtons.add(btnExcelImport, null);
		jPanelButtons.add(jButtonCancel, null);
		jPanelButtons.add(jButtonClose, null);
		this.getContentPane().add(jPanelNorth, BorderLayout.NORTH);
		jPanelNorth.add(jButtonChange, null);
		jPanelNorth.add(jButtonDelete, null);
		jPanelNorth.add(jButtonRefresh, null);

		this.getContentPane().add(jKontoScroll, BorderLayout.CENTER);
		jKontoScroll.getViewport().add(jKontoTable, null);
	}

	/**
	 * Initialisierung der Tabelle für Konto mit dem Model
	 */
	private void initTableKonto() {
		Trace.println(3, "KontoplanView.initTable()");
		// ----- die Tabelle mit dem Model
		mKontoTableModel = new KontoModel();
		jKontoTable.setModel(mKontoTableModel);
		jKontoTable.setFont(Config.fontText);
		jKontoTable.setRowHeight(Config.windowTextSize + 4);
		jKontoTable.getTableHeader().setFont(Config.fontText);
		mKontoTableModel.addTableModelListener(jKontoTable);
		// die Breite der Cols
		TableColumn column = null;
		for (int i = 0; i < mKontoTableModel.getColumnCount(); i++) {
			column = jKontoTable.getColumnModel().getColumn(i);
			switch (i) {
			case 0:
				column.setPreferredWidth(50);
				break;
			case 1:
				column.setPreferredWidth(250);
				break;
			case 2:
				column.setPreferredWidth(20);
				break;
			default:
				column.setPreferredWidth(100);
			}
		}
		// Den default-Renderer für Spalten mit Double-Werten
		jKontoTable.setDefaultRenderer(Double.class, new DoubleRenderer());
		jKontoTable.setDefaultRenderer(Integer.class, new IntegerRenderer());
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void run() {
		try {
			// show Form and go
			this.setVisible(true);
		} catch (Exception e) {
			System.out.println("Konto-Exception");
		}
	}

	// ----- Model der Konto-Tabelle ----------------------------
	/**
	 * Die Klasse hält eine Verbindung zu den KontoDaten
	 */
	private class KontoModel extends AbstractTableModel {

		private static final long serialVersionUID = -4384615067091089292L;

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return mKontoData.getRowCount();
		}

		/** Die Bezeichnung der Spalten */
		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Nummer";
			case 1:
				return "Konto";
			case 2:
				return "S/H";
			case 3:
				return "StartSaldo";
			case 4:
				return "Saldo";
			}
			return "";
		}

		/** Steuert das aussehen einer Spalte */
		@Override
		public Class<?> getColumnClass(int col) {
			return getValueAt(0, col).getClass();
			/*
			 * if (col == 0) return Integer.valueOf(0).getClass(); else return new
			 * String().getClass();
			 */
		}

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
				case 2:
					if (lKonto.isSollKonto()) return "S";
					else return "H";
				case 3:
					return Double.valueOf(lKonto.getStartSaldo());
				case 4:
					return Double.valueOf(lKonto.getSaldo());
				}
			} catch (KontoNotFoundException ex) {
				ex.printStackTrace(Trace.getPrintWriter());
			}
			return "";
		}
	}

	// ----- Implementierung der Observer-Methode ----------------------
	/**
	 * Wird aufgerufen wenn ein Konto geändert wurde, d.h. wenn dort die Methode
	 * notifyObservers aufgerufen wurde
	 */
//	public void update(Observable pObservable, Object pArg) {
//		/*
//		 * rmo: mit Events? if (pObservable instanceof KontoData.KtoObservable)
//		 * { // die KontoNr, sichtbar? String lKtoNr = (String) pArg;
//		 * this.repaint(); }
//		 */
//	}

	// ----- von Borland generiert ----------------------------------
	/**
	 * Ein Konto löschen.
	 */
	void jButtonDelete_actionPerformed(ActionEvent e) {
		try {
			// Konto lesen
			Konto lKonto = null;
			int rowNr = jKontoTable.getSelectedRow();
			if (rowNr < 0) return;
			lKonto = mKontoData.readAt(rowNr);
			if (lKonto != null) {
				// Konto löschen, Bestätigen
				int lResult = JOptionPane.showConfirmDialog(this, "Konto " + lKonto.getKontoNr() + " löschen",
						"Kontorahmen", JOptionPane.YES_NO_OPTION);
				if (lResult == JOptionPane.NO_OPTION) return;
				mKontoData.deleteAt(rowNr);
				mKontoTableModel.fireTableRowsDeleted(rowNr, rowNr);
				repaint();
			}
		} catch (KontoNotFoundException pEx) {
			JOptionPane.showMessageDialog(this, pEx.getMessage(), "Fehler Kontorahmen", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Ein Konto soll bearbeitet werden, die Werte in Felder kopieren.
	 */
	void jButtonChange_actionPerformed(ActionEvent e) {
		// Konto lesen
		Konto lKonto = null;
		if (jKontoTable.getSelectedRow() < 0) return;
		try {
			lKonto = mKontoData.readAt(jKontoTable.getSelectedRow());
			if (lKonto != null) {
				jTextFieldKontoNr.setText(lKonto.getKontoNrAsString());
				jTextFieldText.setText(lKonto.getText());
				jTextFieldStartSaldo.setText(String.valueOf(lKonto.getStartSaldo()));
				jCheckBoxSoll.setSelected(lKonto.isSollKonto());
			}
		} catch (KontoNotFoundException pEx) {
			JOptionPane.showMessageDialog(this, pEx.getMessage(), "Fehler Kontorahmen", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Den Eintrag in der Eingabezeile speichern
	 */
	void jButtonSave_actionPerformed(ActionEvent e) {
		Konto lKonto = null;
		try {
			lKonto = mKontoData.read(jTextFieldKontoNr.getText());
		} catch (KontoNotFoundException pEx) {
			lKonto = new Konto();
		}
		try {
			lKonto.setKontoNr(jTextFieldKontoNr.getText());
			lKonto.setText(jTextFieldText.getText());
			lKonto.setStartSaldo(jTextFieldStartSaldo.getText());
			lKonto.setIstSollKonto(jCheckBoxSoll.isSelected());
			mKontoData.add(lKonto);
			Trace.println(7, "add: " + lKonto.toString());
			mKontoTableModel.fireTableDataChanged();
			// jKontoTable.repaint();
			repaint();
		} catch (FibuException pEx) {
			JOptionPane.showMessageDialog(this, pEx.getMessage(), "Fehler Kontorahmen", JOptionPane.ERROR_MESSAGE);
		} catch (NumberFormatException pEx) {
			JOptionPane.showMessageDialog(this, "Start-Saldo: " + pEx.getMessage(), "Fehler Kontorahmen",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Das Fenster schliessen */
	void jButtonClose_actionPerformed(ActionEvent e) {
		dispose();
		setVisible(false);
	}

	void jButtonRefresh_actionPerformed(ActionEvent e) {
		int lCursorType = 0;
		try {
			lCursorType = this.getCursor().getType();
			// Cursor verändern während Berechnung
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			KontoCalculator calculator = new KontoCalculator();
			calculator.calculateSaldo();
			mKontoTableModel.fireTableDataChanged();
			// jKontoTable.repaint();
		} catch (FibuException pEx) {
			JOptionPane.showMessageDialog(this, pEx.getMessage(), "Fehler Kontorahmen", JOptionPane.ERROR_MESSAGE);
		} finally {
			this.setCursor(Cursor.getPredefinedCursor(lCursorType));
		}
	}

	/** Alle Felder zurücksetzen */
	void jButtonCancel_actionPerformed(ActionEvent e) {
		jTextFieldKontoNr.setText("");
		jTextFieldText.setText("");
		jTextFieldStartSaldo.setText("");
		jCheckBoxSoll.setSelected(false);
	}

	/**
	 * Excel-Button wurde gedrückt.
	 */
	private void btnExcelExport_actionPerformed(ActionEvent e) {
		Trace.println(3, "KontoPlanView.actionExcelExoprt()");
		// Printer aufrufen, Daten siehe Interface: TablePrinterModel
		ExcelExportKontoPlan lExcel = new ExcelExportKontoPlan(jKontoTable.getModel());
		TextExportKontoPlan lTxt = new TextExportKontoPlan(jKontoTable.getModel());
		try {
			lExcel.doExport(FibuData.getFibuData().getFibuName() + "_Kontoplan");
			lTxt.doExport(FibuData.getFibuData().getFibuName() + "_Kontoplan");
			String msg = "Exportiert nach: " + Config.sDefaultDir;
			JOptionPane.showMessageDialog(this, msg, "Excel Export", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			JOptionPane
					.showMessageDialog(this, "Fehler: " + ex.getMessage(), "Excel Export", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Excel-Button wurde gedrückt.
	 */
	private void btnExcelImport_actionPerformed(ActionEvent e) {
		Trace.println(3, "KontoPlan.actionExcelImport()");
		// Printer aufrufen, Daten siehe Interface: TablePrinterModel
		ExcelImport lExcel = new ExcelImport();
		JFileChooser fc = new JFileChooser(Config.sDefaultDir);

		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				File file = fc.getSelectedFile();
				// This is where a real application would open the file.
				Trace.println(5, "Opening: " + file.getName());
				lExcel.doOpen(file);
				mKontoData.copyExcelToKonto(lExcel);
				lExcel.doClose();
			} catch (Exception pEx) {
				JOptionPane.showMessageDialog(this, pEx.getMessage(), "Fehler Kontorahmen", JOptionPane.ERROR_MESSAGE);
				return;
			}
			JOptionPane.showMessageDialog(this, "alle Daten eingelesen", "Kontorahmen", JOptionPane.OK_OPTION);
		}
	}

	/**********************************************
	 * für den einzel-Test der View.
	 */
	public static void main(String[] args) {
		try {
			DbConnection.open("FibuLeer");
			KontoplanView lKonto = new KontoplanView();
			lKonto.setVisible(true);
		} catch (FibuRuntimeException ex) {
		}
	}

}
