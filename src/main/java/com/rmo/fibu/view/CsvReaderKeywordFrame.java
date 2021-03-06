package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.model.CsvCompany;
import com.rmo.fibu.model.CsvCompanyData;
import com.rmo.fibu.model.CsvKeyword;
import com.rmo.fibu.model.CsvKeywordData;
import com.rmo.fibu.model.CsvParserBase;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.KontoNrVector;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;


/** 
 * Liest CSV Keywords ein, schreibt diese in Tabelle.
 * "CSV-datei selektieren": Eine Auswahl von dateien anzeigen,
 * wenn ein File selektiert, wird das file an CsvReaderTableFrame weitergegeben.
 */
public class CsvReaderKeywordFrame extends JFrame {

	private static final long serialVersionUID = 6445113637284754031L;
	
	// TODO Grösse anpassen
	/** Die Breite der KontoListe */
	private static final int SUCHWORT_WIDTH = 10;
	private static final int DEFAULT_WIDTH = 4;
	private final Dimension ktoNrDefaultSize = new Dimension(8 * Config.windowTextSize, 
			Config.windowTextSize + 12);
	private final Dimension dirPathSize = new Dimension(30 * Config.windowTextSize,
			Config.windowTextSize + 12);


	private BuchungView mParent = null;
	
	// der Name des Institus von dem pdf-buchungen eingelesen werden.
	private String 				companyName = null;
	private String 				companyFullName = null;
	private JComboBox<String>	mKtoNrDefault;
	private JTextField 			mDirPath;
	// view elemente

	// view der tabelle
	private JTable 			mTableView = null;
	/** Das Model zur Konto-Tabelle */
	private CsvKeywordModel mKeywordModel;
	/** Verbindung zur DB */
	private CsvKeywordData 	mKeywordData = null;
	
	private CsvCompanyData 	mCompanyData = null;
	private CsvCompany 		mCompany = null;

	private CsvReaderBuchungFrame mCsvBuchungFrame = null;

	public CsvReaderKeywordFrame(String pCompanyName, BuchungView pParent) {
		super("Schlüsselwort für CSV eingenben, V2.0");
		companyName = pCompanyName;
		mParent = pParent;
		init();
	}

	/**
	 * Start der Initialisierung, muss von jedem Konstruktor aufgerufen werden.
	 */
	private void init() {
		Trace.println(1, "CsvReaderFrame.init()");
		// Verbindung zur DB
		mKeywordData = (CsvKeywordData) DataBeanContext.getContext().getDataBean(CsvKeywordData.class);
		mCompanyData = (CsvCompanyData) DataBeanContext.getContext().getDataBean(CsvCompanyData.class);
		datenEinlesen();
		initView();
		this.setVisible(true);
	}

	/**
	 * Initialisierung der verschiedenen Views
	 */
	private void initView() {
		Trace.println(5, "CsvReaderFrame.initView()");
		getContentPane().add(initTop(), BorderLayout.PAGE_START);
		getContentPane().add(initTable(), BorderLayout.CENTER);
		getContentPane().add(initBottom(), BorderLayout.PAGE_END);
		setSize(Config.winCsvReaderKeywordDim);
		setLocation(Config.winCsvReaderKeywordLoc);
	}

	/**
	 * Top-Zeile setzen
	 * @return
	 */
	private Container initTop() {
		if (companyName.equalsIgnoreCase(CsvParserBase.companyNamePost)) {
			companyFullName = "Post Finance (CSV)";
		} else {
			companyFullName = "Credit Suisse (CSV)";
		}
		JLabel labelCompany = new JLabel(companyFullName);
		labelCompany.setFont(Config.fontTextBold);

		return labelCompany;
	}

	/**
	 * Tabelle setzen
	 * @return
	 */
	private Container initTable() {
		mKeywordModel = new CsvKeywordModel(mCompany.getCompanyID());

		mTableView = new JTable(mKeywordModel);
		mTableView.getTableHeader().setFont(Config.fontText);
		mTableView.setRowHeight(Config.windowTextSize + 4);
		mTableView.setFont(Config.fontText);
	
		JScrollPane lScrollPane = new JScrollPane(mTableView);
		setColWidth();
		setColKontoNummern();
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
			case 0:
				column.setPreferredWidth(SUCHWORT_WIDTH * Config.windowTextSize);
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
		JComboBox<String> comboBoxKtonr = new JComboBox<String>();
		comboBoxKtonr.setModel(new DefaultComboBoxModel<String>(new KontoNrVector()));
		comboBoxKtonr.setFont(Config.fontText);
		TableColumn ktoColumn = mTableView.getColumnModel().getColumn(1);
		ktoColumn.setCellEditor(new DefaultCellEditor(comboBoxKtonr));
	}

	/**
	 * Buttons setzen
	 * 
	 * @return
	 */
	private Container initBottom() {
		JPanel lPanel = new JPanel(new GridLayout(0,1));
		
		JPanel flow1 = new JPanel(new FlowLayout());	
		JButton btnAdd = new JButton("Dazufügen");
		btnAdd.setFont(Config.fontTextBold);

		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addAction();
			}
		});
		flow1.add(btnAdd);
		
		JButton btnDelete = new JButton("Löschen");
		btnDelete.setFont(Config.fontTextBold);
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteAction(e);
			}
		});
		flow1.add(btnDelete);

		JButton btnSave = new JButton("Speichern");
		btnSave.setFont(Config.fontTextBold);
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});
		flow1.add(btnSave);
		lPanel.add(flow1);
		
		// die default-KontoNummer
		JPanel flow2 = new JPanel(new FlowLayout());	
		JLabel label1 = new JLabel("KontoNr Default: ");
		label1.setFont(Config.fontTextBold);

		flow2.add(label1);
		mKtoNrDefault = new JComboBox<String>();
		mKtoNrDefault.setModel(new DefaultComboBoxModel<String>(new KontoNrVector()));
		mKtoNrDefault.setPreferredSize(ktoNrDefaultSize);
		mKtoNrDefault.setFont(Config.fontText);
		
		flow2.add(mKtoNrDefault);
		lPanel.add(flow2);

		// Directory eingenben
		JPanel flow3 = new JPanel(new FlowLayout());
		JLabel label2 = new JLabel("Directory: ");
		label2.setFont(Config.fontTextBold);
		flow3.add(label2);
		
		mDirPath = new JTextField();
		mDirPath.setPreferredSize(dirPathSize);
		mDirPath.setText(mCompany.getDirPath());
		mDirPath.setFont(Config.fontText);
		
		flow3.add(mDirPath);
		lPanel.add(flow3);


		// datei selektieren
		JPanel flow4 = new JPanel(new FlowLayout());
		JButton btnSelectFile = new JButton("CSV-datei selektieren");
		btnSelectFile.setFont(Config.fontTextBold);

		btnSelectFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showCsvFiles();
			}
		});
		flow4.add(btnSelectFile);
		lPanel.add(flow4);
		
		return lPanel;
	}


	/**
	 * einlesen alle Daten von der DB.
	 */
	private void datenEinlesen() {
		Trace.println(3, "CsvReaderFrame.datenEinlesen()");
		try {
			mCompany = mCompanyData.readData(companyName);
		}
		catch (FibuException ex) {
			// nicht gefunden, somit neu
			mCompany = new CsvCompany();
			mCompany.setCompanyName(companyName);
		}
		try {
			mCompanyData.addData(mCompany);
		}
		catch (FibuException ex2) {
			JOptionPane.showMessageDialog(this, ex2.getMessage(), "\"Fehler in DB", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	/**
	 * Einen Eintrag dazufügen
	 */
	private void addAction() {
		CsvKeyword lKeyword = new CsvKeyword();
		lKeyword.setCompanyId(mCompany.getCompanyID());
		lKeyword.setKontoNr(" ");
		lKeyword.setSh("S");
		lKeyword.setSuchWort("TEXT EINGEBEN");
		try {
			mKeywordData.addEmptyRow(lKeyword);
			mKeywordModel.fireTableDataChanged();
		} catch (FibuException ex2) {
			JOptionPane.showMessageDialog(this, ex2.getMessage(), "\"Fehler in DB", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	/**
	 * Einen Eintrag löschen
	 */
	private void deleteAction(ActionEvent e) {
		int rowNr = mTableView.getSelectedRow();
		if (rowNr >= 0) {
			try {
				CsvKeyword lKeyword = mKeywordData.readAt(mCompany.getCompanyID(), rowNr);
				mKeywordData.deleteRow(lKeyword.getId());
				mKeywordModel.fireTableDataChanged();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Fehler beim löschen", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Die Keywords in der DB speichern.
	 */
	private void saveAction() {
		CsvKeyword pdfKeyword = null;

		Iterator<CsvKeyword> iter = mKeywordData.getIterator( mCompany.getCompanyID());
		try {
			while (iter.hasNext()) {
				pdfKeyword = iter.next();
				mKeywordData.add(pdfKeyword);
			}
			// save company data
			String selecteKto = (String) mKtoNrDefault.getSelectedItem();
			mCompany.setKontoNrDefault(selecteKto);
			mCompany.setDirPath(mDirPath.getText());
			mCompanyData.addData(mCompany);
		} catch (FibuException e) {
			Trace.println(1, "Fehler in PdfKeywordPanel.saveAction: " + e.getMessage());
		}
	}

	/**
	 * Mögliche CSV-files anzeigen, eines selektieren
	 */
	private void showCsvFiles() {
		// prüfen, ob eintrag im Feld directory
		if (mDirPath.getText() == null) {
			JOptionPane.showMessageDialog(this, "CSV-file name fehlt", "CSV einlesen", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			File file = new File(mDirPath.getText());
			if (file.isDirectory()) {
				// save the new name
				Config.sPdfFileName = mDirPath.getText();
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(file);
				chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));
				int returnValue = chooser.showOpenDialog(this);
				if ((returnValue == JFileChooser.APPROVE_OPTION)) {
					file = chooser.getSelectedFile();
					// btnEinlesen.setDisable(true);
		            mCsvBuchungFrame = new CsvReaderBuchungFrame(file, companyName);
		            mCsvBuchungFrame.setVisible(true);
				}
				else {
					return;
				}
			}
			else {
				JOptionPane.showMessageDialog(this, "'" + mDirPath.getText() + "' ist kein Directory", "CSV Datei selektieren", JOptionPane.ERROR_MESSAGE);
				
			}
		} catch (FibuRuntimeException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "CSV einlesen", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	public String getCompanyName() {
		return companyName;
	}
	
	
	/** wenn Fenster geschlossen */
	@Override	
	public void setVisible(boolean b) {
		if (!b) {
			Config.winCsvReaderKeywordDim = getSize();
			Config.winCsvReaderKeywordLoc = getLocation();
			mParent.resetCsvReaderFrame();
			if (mCsvBuchungFrame != null) {
				mCsvBuchungFrame.setVisible(b);
			}
		}
		super.setVisible(b);
	}


	// ----- Model der Keyword-Tabelle ----------------------------
	/** Schnittstelle zum Daten-Objekt KontoData */
	private class CsvKeywordModel extends AbstractTableModel {

		private static final long serialVersionUID = -3805602970105237582L;
		
		private int companyId = 0;
		
		public CsvKeywordModel(int compamyId) {
			this.companyId = compamyId;
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return mKeywordData.getRowCount(companyId);
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Such Wort";
			case 1:
				return "Konto Nr.";
			case 2:
				return "S/H";
			case 3:
				return "compID";
			case 4:
				return "ID";
			}
			return "";
		}

		/** Steuert das aussehen einer Spalte */
//		@Override
//		public Class<?> getColumnClass(int col) {
//			return getValueAt(0, col).getClass();
//		}

		/**
		 * Wenn eine Zelle edititer wurde, diese speichern
		 */
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			CsvKeyword lKeyword = null;
			try {
				lKeyword = mKeywordData.readAt(mCompany.getCompanyID(), rowIndex);
			}
			catch (FibuException ex) {
				return;
			}
			if (columnIndex == 0) {
				lKeyword.setSuchWort((String) aValue);
			}
			else if (columnIndex == 1) {
				lKeyword.setKontoNr((String) aValue);
			}
			else if (columnIndex == 2) {
				lKeyword.setSh((String) aValue);				
			}
			else if (columnIndex == 3) {
				lKeyword.setCompanyId((Integer) aValue);				
			}
			else if (columnIndex == 4) {
				lKeyword.setId((Integer) aValue);				
			}
			
			try {
				mKeywordData.updateAt(rowIndex, lKeyword);
				mKeywordModel.fireTableDataChanged();
			}
			catch (FibuException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "Suchwort ändern", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		/**
		 * Alle Zellen 0..1 können nicht editiert werden.
		 */
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex > 2) {
				return false;
			}
			return true;
		}

		/**
		 * Gibt den Wert an der Koordinate row / col zurück.
		 */
		@Override
		public Object getValueAt(int row, int col) {
			Trace.println(7, "CsvKeywordModel.getValueAt(" + row + ',' + col + ')');
			try {
				CsvKeyword lKeyword = mKeywordData.readAt(companyId, row);
				switch (col) {
				case 0:
					return lKeyword.getSuchWort();
				case 1:
					return lKeyword.getKontoNr();
				case 2:
					return lKeyword.getSh();
				case 3:
					return lKeyword.getCompanyId();
				case 4:
					return lKeyword.getId();
				}
			} catch (FibuException ex) {
				Trace.println(3, "getValueAt() "  +ex.getMessage());
				return " ";
			}
			return "";
		}
		
		
	}

}
