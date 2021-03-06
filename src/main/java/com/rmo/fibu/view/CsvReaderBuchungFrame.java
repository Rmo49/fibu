package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import com.rmo.fibu.exception.BuchungValueException;
import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungCsv;
import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.CsvCompanyData;
import com.rmo.fibu.model.CsvKeyword;
import com.rmo.fibu.model.CsvKeywordData;
import com.rmo.fibu.model.CsvParserBase;
import com.rmo.fibu.model.CsvParserCs;
import com.rmo.fibu.model.CsvParserPost;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.KontoNrVector;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;


/**
 * Wird über CsvReaderKeywordFrame gestartet.
 * Das ausgewählte file wird im Konstruktur übergeben.
 * Liest Buchungen von CSV ein siehe datenEinlesen, schreibt in Tabelle.
 * Wenn "Buchungstext anpassen" dann wird Belegnummer und die Kontonummern eingetragen => changeAction
 * Wenn "Speichern" dann saveAction
 */
public class CsvReaderBuchungFrame extends JFrame {
	private static final long serialVersionUID = 1201522139173678122L;
	
	/** Die Grösse der Spalten */
	private static final int TEXT_WIDTH = 30;
	private static final int DEFAULT_WIDTH = 4;
	
	// der Name des Institus von dem pdf-buchungen eingelesen werden.
	private String mCompanyName = null;
	// file von dem gelesen werden soll
	private File mFile = null;
	// view elemente
	private JTable mTableView = new JTable();
	private DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();

	/** Das Model zu dieser View, Verbindung zur DB */
	private CsvBuchungModel mBuchungModel;
	private List<BuchungCsv> mBuchungList = new ArrayList<BuchungCsv>();
	/** Verbindung zur DB */
	private BuchungData mBuchungData = null;
	private String nextBelegNr = null;
	private int returnValue = 0;

	/**
	 * Construtor
	 * needs filename with CSV-data
	 */
	public CsvReaderBuchungFrame(File file, String companyName) {
		Trace.println(3,"CsvReaderBuchungFrame(file: " + file.getAbsolutePath() +")");
		this.mFile = file;
		this.mCompanyName = companyName;
		init();		
	}

	/**
	 * Start der Initialisierung, muss von jedem Konstruktor aufgerufen werden.
	 */
	private void init() {
		Trace.println(4, "CsvReaderBuchungFrame.init()");
		datenEinlesen();
		initView();
	}

	
	/** Setup view elements.
	 * @return the border pane
	 */
	private void initView() {
		Trace.println(5,"CsvReaderBuchungFrame.initView()");
		getContentPane().add(initTable(), BorderLayout.CENTER);
		getContentPane().add(initBottom(), BorderLayout.PAGE_END);
		setSize(Config.winCsvReaderBuchungDim);
		setLocation(Config.winCsvReaderBuchungLoc);
	}

	/**
	 * Tabelle mit den Buchungen, kann editiert werden.
	 * 
	 * @return
	 */
	private Container initTable() {
//		mKeywordModel = new CsvKeywordModel(mCompany.getCompanyID());
		mBuchungModel = new CsvBuchungModel();
		mTableView = new JTable(mBuchungModel);
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
			case 2:
				column.setPreferredWidth(TEXT_WIDTH * Config.windowTextSize);
				break;				
			case 5:
				cellRenderer.setHorizontalAlignment(JLabel.RIGHT);
				column.setCellRenderer(cellRenderer);
			default:
				column.setPreferredWidth(DEFAULT_WIDTH * Config.windowTextSize);
			}
		}
	}

	/**
	 * Setzt die Kontonummern Combobox
	 */
	private void setColKontoNummern() {
		JComboBox<String> kontoNummern = new JComboBox<String>();
		kontoNummern.setModel(new DefaultComboBoxModel<String>(new KontoNrVector()));
		kontoNummern.setFont(Config.fontText);
		
		TableColumn sollColumn = mTableView.getColumnModel().getColumn(3);
		sollColumn.setCellEditor(new DefaultCellEditor(kontoNummern));
		sollColumn = mTableView.getColumnModel().getColumn(4);
		sollColumn.setCellEditor(new DefaultCellEditor(kontoNummern));
	}

	
	/**
	 * Buttons setzen
	 * 
	 * @return
	 */
	private Container initBottom() {
		JPanel flow = new JPanel(new FlowLayout());	

		JButton btnChange = new JButton("Buchungstext anpassen");
		btnChange.setFont(Config.fontTextBold);
		
		btnChange.addActionListener(new ActionListener() {				
			@Override
			public void actionPerformed(ActionEvent e) {
				changeAction();
			}
		});
		flow.add(btnChange);

		JButton btnSave = new JButton("Speichern");
		btnSave.setFont(Config.fontTextBold);
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAction();
				mBuchungModel.fireTableDataChanged();
			}
		});
		flow.add(btnSave);

		return flow;
	}

	/**
	 * Alle Buchungstexte anpassen mit keywords
	 */
	private void changeAction() {
		Iterator<BuchungCsv> iter = mBuchungList.iterator();
		while (iter.hasNext()) {
			changeBuchungsText(iter.next());
		}
		mTableView.repaint();
	}
	
	/** prüft Buchungstext, wenn search-text gefunden, wird das entsprechende Konto gesetzt
	 */
	protected void changeBuchungsText(BuchungCsv buchungPdf) {
		if (buchungPdf == null) {
			return;
		}
		CsvKeywordData keywordData = (CsvKeywordData) DataBeanContext.getContext().getDataBean(CsvKeywordData.class);
		Iterator<CsvKeyword> lIter = keywordData.getIterator(getCompanyId());	
		int pos = -1;
		while (lIter.hasNext()) {
			CsvKeyword keyword = lIter.next();
			pos = buchungPdf.getText().toUpperCase().indexOf(keyword.getSuchWort().toUpperCase());
			if (pos >= 0) {
				setKonto(buchungPdf, keyword);
				changeText(buchungPdf, pos);
				return;
			}
		}
	}

	/** Die Kontonummer setzen wenn etwas gefunden im Text.
	 */
	private void setKonto(BuchungCsv buchungPdf, CsvKeyword keyword) {	
		if (keyword.getSh().equalsIgnoreCase("H")) {
			buchungPdf.setHaben(keyword.getKontoNr());
		}
		else {
			buchungPdf.setSoll(keyword.getKontoNr());
		}
	}

	/**
	 * Den Text kürzen, falls sehr Lang
	 */
	private void changeText(BuchungCsv buchungPdf, int pos) {
		int maxLength = buchungPdf.getText().length();
		if (maxLength > pos+Config.sPdfTextLen) {
			maxLength = pos+Config.sPdfTextLen;
		}
		buchungPdf.setText(buchungPdf.getText().substring(pos, maxLength));
	}
	
	
	
	/**
	 * Die ID der Company
	 */
	protected int getCompanyId() {
		CsvCompanyData companyData = (CsvCompanyData) DataBeanContext.getContext().getDataObject(CsvCompanyData.class);
		try {
			return companyData.readData(mCompanyName).getCompanyID();
		}
		catch (FibuException ex){
			// do nothing
		}
		return 0;
	}

	/**
	 * Die standard KontoNr
	 */
	protected String getKontoNrDefault() {
		CsvCompanyData companyData = (CsvCompanyData) DataBeanContext.getContext().getDataObject(CsvCompanyData.class);
		try {
			return companyData.readData(mCompanyName).getKontoNrDefault();
		}
		catch (FibuException ex){
			// do nothing
		}
		return "";
	}

	/** Die Buchungen in der DB speichern.
	 */
	private void saveAction() {
		mBuchungData = (BuchungData) DataBeanContext.getContext().getDataBean(BuchungData.class);
		BuchungCsv buchungPdf = null;
		Buchung buchungNew =  new Buchung();
		Iterator<BuchungCsv> iter = mBuchungList.iterator();
		while (iter.hasNext()) {
			buchungPdf = iter.next();
			returnValue = copyToBuchung(buchungPdf, buchungNew);			
			if (returnValue > 0) {
				returnValue = IsBuchungInDb(buchungNew);
				if (returnValue == 0) {
					iter.remove();
				}			
				if (returnValue > 0) {
					returnValue = saveBuchung(buchungNew);
					iter.remove();
				}
			}
			if (returnValue < 0) {
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param buchungPdf
	 * @param buchung
	 * @return -1: Fehler und abbrechen, 0: Fehler, nicht abbrechen 1: alles ok  
	 */
	private int copyToBuchung(BuchungCsv buchungPdf, Buchung buchung) {
		buchung.setID(-1);
		try {
			buchung.setDatum(buchungPdf.getDatum());
		}
		catch (ParseException ex) {
			int reply = JOptionPane.showConfirmDialog(this, 
					makeMessage(buchungPdf.getDatum(), buchungPdf.getText(), ex.getMessage()),
					"Datum falsch", JOptionPane.OK_CANCEL_OPTION);
			if (reply == JOptionPane.OK_OPTION) {
				returnValue = 0;
			} else {
			    return -1;
			}
		}
		
		// damit exist ohne beleg funktioniert
		buchung.setBeleg(null);

		buchung.setBuchungText(buchungPdf.getText());
		try {
			buchung.setSoll(buchungPdf.getSoll());
			buchung.setHaben(buchungPdf.getHaben());
		} 
		catch (BuchungValueException ex) {
			int reply = JOptionPane.showConfirmDialog(this, 
					makeMessage(buchungPdf.getDatum(), buchungPdf.getText(), ex.getMessage()),
					"KontoNr falsch", JOptionPane.OK_CANCEL_OPTION);
			if (reply == JOptionPane.OK_OPTION) {
				return 0;
			} else {
			    return -1;
			}
		}
		
		try {
			double betrag = parseBetrag(buchungPdf.getBetrag());
			buchung.setBetrag(betrag);
		}
		catch (NumberFormatException ex) {
			int reply = JOptionPane.showConfirmDialog(this, 
					makeMessage(buchungPdf.getDatum(), buchungPdf.getText(), ex.getMessage()),
					"Betrag falsch", JOptionPane.OK_CANCEL_OPTION);
			if (reply == JOptionPane.OK_OPTION) {
				return 0;
			} else {
			    return -1;
			}
		}
		return 1;
	}
	
	/**
	 * Prüft, ob Buchung in der DB ist.
	 * @return 1: trotzdem speichern, 0: nicht speichern, -1: Abbrechen
	 */
	private int IsBuchungInDb(Buchung buchungNew) {
		Buchung buchungIst = mBuchungData.isInDb(buchungNew);
		if (buchungIst != null) {				
			StringBuffer sb = new StringBuffer(100);
			sb.append(buchungIst.getDatum());
			sb.append(", ");
			sb.append("Soll: ");
			sb.append(buchungIst.getSoll());
			sb.append(" Haben: ");
			sb.append(buchungIst.getHaben());
			sb.append(" Betrag: ");
			sb.append(buchungIst.getBetragAsString());
			sb.append("\n Buchung in DB: ");
			sb.append(buchungIst.getBuchungText());
			sb.append("\n Buchung neu:");
			sb.append(buchungNew.getBuchungText());
			sb.append("\n Trotzdem übernehmen?");
			int reply = JOptionPane.showConfirmDialog(this, sb.toString(),
					"Buchung schon vorhanden", JOptionPane.YES_NO_CANCEL_OPTION);
			if (reply == JOptionPane.YES_OPTION) {
				return 1;
			}
			else if (reply == JOptionPane.NO_OPTION) {
				return 0;
			}
			else {
				return -1;
			}
		}
		return 1;
	}

	/**
	 * Speichern der Buchung in der DB
	 * @param buchungNew
	 * @return 1: gespeichert, -1: Abbrechen
	 */
	private int saveBuchung(Buchung buchungNew) {
		if (nextBelegNr == null) {
			nextBelegNr = mBuchungData.getNextBelegNr();
		}
		else {
			nextBelegNr = Config.addOne(nextBelegNr);
		}
		buchungNew.setBeleg(nextBelegNr);
		// hier wird die Buchung gesichert
		mBuchungData.add(buchungNew);
		try {
			mBuchungData.saveNew();
		}
		catch (FibuException ex) {
			StringBuffer sb = new StringBuffer(100);
			sb.append("Buchung: ");
			sb.append(buchungNew.getDatum());
			sb.append(" ");
			sb.append(buchungNew.getBuchungText());
			sb.append("\n Exception: ");
			sb.append(ex.getMessage());						
			int reply = JOptionPane.showConfirmDialog(this, sb.toString(),
					"Fehler beim sichern", JOptionPane.OK_CANCEL_OPTION);						
			if (reply == JOptionPane.OK_OPTION){
				return 1;
			} else {
				return -1;
			}
		}
		return 1;
	}

	
	/**
	 * Den Betrag in double zurückgeben
	 * @param betrag
	 * @return
	 * @throws NumberFormatException
	 */
	private double parseBetrag(String betrag) throws NumberFormatException {
		// zuerst leezeichen entfernen
		betrag = betrag.trim();
		int pos = betrag.indexOf(" ");
		if (pos > 0) {
			betrag = betrag.substring(0, pos) + betrag.substring(pos+1);
		}
		double betrag2 = 0;
		betrag2 = Double.parseDouble(betrag);
		return betrag2;
	}

	/**
	 * Die Error-Message zusammenstellen
	 * @return
	 */
	private String makeMessage(String datum, String text, String message) {
		StringBuffer sb = new StringBuffer(100);
		sb.append("Buchung: ");
		sb.append(datum);
		sb.append(", ");
		sb.append(text);
		if (message.length() > 0) {
			sb.append("\n Exception: ");
			sb.append(message);
		}
		return sb.toString();
	}
	
	
	/**
	 * Einlesen alle Daten vom CSV file, Zeile um Zeile.
	 */
	private void datenEinlesen() {
		Trace.println(5,"CsvReaderBuchungFrame.datenEinlesen()");
		CsvParserBase parser = null;
		if (mCompanyName.equalsIgnoreCase(CsvParserBase.companyNamePost)) {
			parser = new CsvParserPost(mFile);
			}
		else if (mCompanyName.equalsIgnoreCase(CsvParserBase.companyNameCS)) {
			parser = new CsvParserCs(mFile);
		}
			
		// hier werden die Daten eingelesen, Zeile um Zeile
		Trace.println(5,"CsvReaderBuchungFrame.datenEinlesen() => start Parsing");
		BuchungCsv buchungPdf = parser.nextBuchung();
		while (buchungPdf != null) {
			if (buchungPdf.getDatum().length() > 0) {
				mBuchungList.add(buchungPdf);
			}
			buchungPdf = parser.nextBuchung();
		}
		Trace.println(5,"CsvReaderBuchungFrame.datenEinlesen() => end Parsing");		
	}

	/** wenn Fenster geschlossen */
	@Override	
	public void setVisible(boolean b) {
		if (!b) {
			Config.winCsvReaderBuchungDim = getSize();
			Config.winCsvReaderBuchungLoc = getLocation();
		}
		super.setVisible(b);
	}
	
	
	
	// ----- Model der Buchung-Tabelle --------------------------------------
	private class CsvBuchungModel extends AbstractTableModel {

		private static final long serialVersionUID = -3805602970105237582L;
				
		public CsvBuchungModel() {
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public int getRowCount() {
			return mBuchungList.size();
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Datum";
			case 1:
				return "Beleg";
			case 2:
				return "Text";
			case 3:
				return "Soll";
			case 4:
				return "Haben";
			case 5:
				return "Betrag";
			}
			return "";
		}

		/** Steuert das aussehen einer Spalte */
//		@Override
//		public Class<?> getColumnClass(int col) {
//			return getValueAt(0, col).getClass();
//		}

		/**
		 * Wenn eine Zelle edititer wurde, diesen Wert in der Liste speichern
		 */
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			BuchungCsv lBuchung = mBuchungList.get(rowIndex);
			if (columnIndex == 0) {
				lBuchung.setDatum((Date) aValue);
			}
			else if (columnIndex == 1) {
				lBuchung.setBeleg((String) aValue);
			}
			else if (columnIndex == 2) {
				lBuchung.setText((String) aValue);				
			}
			else if (columnIndex == 3) {
				lBuchung.setSoll((String) aValue);				
			}
			else if (columnIndex == 4) {
				lBuchung.setHaben((String) aValue);				
			}
			else if (columnIndex == 5) {
				lBuchung.setBetrag((String) aValue);				
			}
			mBuchungList.set(rowIndex, lBuchung);
		}

		/**
		 * Alle Zellen können editiert werden.
		 */
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		/**
		 * Gibt den Wert an der Koordinate row / col zurück.
		 */
		@Override
		public Object getValueAt(int row, int col) {
			Trace.println(7, "CsvBuchungModel.getValueAt(" + row + ',' + col + ')');
			BuchungCsv lBuchung = mBuchungList.get(row);
			switch (col) {
			case 0:
				return lBuchung.getDatum();
			case 1:
				return lBuchung.getBeleg();
			case 2:
				return lBuchung.getText();
			case 3:
				return lBuchung.getSoll();
			case 4:
				return lBuchung.getHaben();
			case 5:
				return lBuchung.getBetrag();
			}
			return "";
		}
	}
	
}
