package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.model.CsvBank;
import com.rmo.fibu.model.CsvBankData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.PdfDokument;
import com.rmo.fibu.util.PdfParser;
import com.rmo.fibu.util.PdfWordStripper;
import com.rmo.fibu.util.Trace;

/**
 * Liest CSV Keywords ein, schreibt diese in Tabelle. "CSV-datei selektieren":
 * Eine Auswahl von dateien anzeigen, wenn ein File selektiert, wird das file an
 * CsvReaderTableFrame weitergegeben.
 */
public class PdfSetupFrame extends JFrame
	implements TableModelListener {

	private static final long serialVersionUID = -6429166001920978382L;

	// die View Elemente
	private CsvBank mBank;

	private JTextField mWordBefore;
	private JButton mBtnSearch;
	private JScrollPane mTableScroll;

	private JTextField mDatumSpalte;
	private JTextField mTextSpalte;
	private JTextField mSollSpalte;
	private JTextField mHabenSplalte;

	// view der tabelle
	private JTable mTableView = null;
	/** Das interne Model zur Tabelle */
	private PdfBuchungModel mTableModel;

	private JButton mBtnSpeichern;

	// File von dem gelesen wird
	private File mPdfFile;

	/** Verbindung zur DB */
	private CsvBankData mBankData = null;


	/**
	 * Wird gestartet von Buchungen für Einstellungen.
	 *
	 * @param pParent Referenz zu den Buchungen
	 */
	public PdfSetupFrame(CsvBank bank) {
		super("Steuerdaten eingeben für PDF-Datei");
		mBank = bank;
		init();
	}

	/**
	 * Start der Initialisierung, muss von jedem Konstruktor aufgerufen werden.
	 */
	private void init() {
		Trace.println(3, "PdfSetupFrame.init()");
		// view anzeigen
		initView();
		readData();
		this.setVisible(true);
	}

	/**
	 * Daten von der DB lesen
	 */
	private void readData() {
		// Verbindung zur DB
		mWordBefore.setText(mBank.getWordBefore());
		mDatumSpalte.setText(Integer.toString(mBank.getSpalteDatum()));
		mTextSpalte.setText (Integer.toString(mBank.getSpalteText()));
		mSollSpalte.setText (Integer.toString(mBank.getSpalteSoll()));
		mHabenSplalte.setText (Integer.toString(mBank.getSpalteHaben()));
	}


	/**
	 * Initialisierung der verschiedenen Views
	 */
	private void initView() {
		Trace.println(5, "PdfSetupFrame.initView()");
		getContentPane().add(initAll(), BorderLayout.CENTER);
//		getContentPane().add(initBottom(), BorderLayout.PAGE_END);
		setSize(Config.winPdfSetupDim);
		setLocation(Config.winPdfSetupLoc);
	}


	private Container initAll() {
		JPanel allPanel = new JPanel();
		allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.Y_AXIS));

		Border blackline;

		blackline = BorderFactory.createLineBorder(Color.black);
		JLabel lLabel;

		lLabel = new JLabel("Steuerdaten für: " + mBank.getBankName());
		lLabel.setFont(Config.fontText);
		lLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		allPanel.add(lLabel);

		JPanel lPanel2 = new JPanel(new FlowLayout());
		lLabel = new JLabel("Zeile vor Buchungen");
		lLabel.setFont(Config.fontText);
		lLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		lPanel2.add(lLabel);

		mWordBefore = new JTextField(20);
		mWordBefore.setFont(Config.fontTextBold);
		mWordBefore.setAlignmentX(Component.LEFT_ALIGNMENT);
		mWordBefore.setText("Kartenlimite");
		lPanel2.add(mWordBefore);

//		lPanel2.setBorder(blackline);
		lPanel2.setAlignmentX(Component.LEFT_ALIGNMENT);
		Dimension size = new Dimension(500,30);
		lPanel2.setMaximumSize(size);
		lPanel2.setPreferredSize(size);
		lPanel2.setMinimumSize(size);

		allPanel.add(lPanel2);

		mBtnSearch = new JButton("Nächste Zeile einlesen");
		mBtnSearch.setFont(Config.fontTextBold);
		mBtnSearch.setAlignmentX(Component.LEFT_ALIGNMENT);
		allPanel.add(mBtnSearch);

		mBtnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				leseBuchungsZeile();
			}
		});

		// model initialisieren, da vor der mTableView gebraucht
		mTableModel = new PdfBuchungModel();
		mTableModel.addTableModelListener(this);

		mTableScroll = new JScrollPane(initTable());
//		size = new Dimension(500,50);
//		mTableScroll.setMaximumSize(size);
//		mTableScroll.setPreferredSize(size);
//		mTableScroll.setMinimumSize(size);
//		mTableScroll.setSize(200,30);
		mTableScroll.setBorder(blackline);
		allPanel.add(mTableScroll);

		JPanel paneList = new JPanel(new GridLayout(0,2));
		paneList.add(new JLabel("Was"));
		paneList.add(new JLabel("Spalte"));
		paneList.add(new JLabel("Datum"));
		paneList.add(mDatumSpalte = new JTextField() );
		paneList.add(new JLabel("Text"));
		paneList.add(mTextSpalte = new JTextField() );
		paneList.add(new JLabel("Soll"));
		paneList.add(mSollSpalte = new JTextField() );
		paneList.add(new JLabel("Haben"));
		paneList.add(mHabenSplalte = new JTextField() );
		size = new Dimension(200,150);
		paneList.setMaximumSize(size);
		paneList.setPreferredSize(size);
		paneList.setMinimumSize(size);

		allPanel.add(paneList);

		mBtnSpeichern = new JButton("Speichern");
		mBtnSpeichern.setFont(Config.fontTextBold);
		mBtnSpeichern.setAlignmentX(Component.LEFT_ALIGNMENT);
		allPanel.add(mBtnSpeichern);

		mBtnSpeichern.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				speichern();
			}
		});


		return allPanel;
	}


	/**
	 * Tabelle setzen
	 *
	 * @return
	 */
	private Container initTable() {
		mTableView = new JTable(mTableModel);
		mTableView.getTableHeader().setFont(Config.fontText);
		mTableView.setRowHeight(Config.windowTextSize + 4);
		mTableView.setFont(Config.fontText);
		return mTableView;
//		setColWidth();
	}


	/**
	 * Die erste Zeile mit Buchungen lesen.
	 * File öffnen
	 */
	private void leseBuchungsZeile() {
		File datei = selectPdfFile();
		if (datei == null) {
			JOptionPane.showMessageDialog(this, "Kann Datei nicht öffnen", "PDF setup", JOptionPane.ERROR_MESSAGE);
		}
		else {
			leseBuchungsZeile(datei);
		}
	}

	/**
	 * Die erste Zeile mit Buchungen lesen.
	 * alles einlesen, erste Zeile auslesen.
	 */
	private void leseBuchungsZeile(File datei) {
		mPdfFile = datei;
		if (mWordBefore.getText().length() < 3) {
			JOptionPane.showMessageDialog(this, "Such-Wort mind. 3 Buchstaben", "PDF setup", JOptionPane.ERROR_MESSAGE);
		}
		PdfWordStripper stripper = PdfParser.pdfStripWords(mPdfFile);
		PdfDokument buchung = new PdfDokument(stripper.pdfWords);
		List<String> zeile = buchung.getFirstRow(mWordBefore.getText());
		if (zeile == null) {
			// suchwort nicht gefunden
			JOptionPane.showMessageDialog(this, mWordBefore.getText() + " nicht gefunnden", "PDF setup", JOptionPane.ERROR_MESSAGE);
			return;
		}
		mTableModel.addData(zeile);
//		pdfZeile = PfdBuchnungen.
	}

	/**
	 * Testen mit file
	 * File öffnen, alles einlesen, erste Zeile auslesen.
	 */
	public void leseBuchungsZeileTest() {
		File datei = new File("Cumulus23-10.pdf");
		mWordBefore.setText("Kartenlimite");
		leseBuchungsZeile(datei);
	}


	/**
	 * Mögliche PDF-files anzeigen, eines selektieren
	 */
	private File selectPdfFile() {
		// prüfen, ob eintrag im Feld directory
		if (mBank.getDirPath() == null) {
			JOptionPane.showMessageDialog(this, "PDF-Directory fehlt", "CSV einlesen", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		try {
			File file = new File(mBank.getDirPath());
			if (file.exists()) {
				// save the new name
				Config.sCsvFileName = mBank.getDirPath();
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(file);
				chooser.setFileFilter(new FileNameExtensionFilter("PDF", "pdf"));
				int returnValue = chooser.showOpenDialog(this);
				if ((returnValue == JFileChooser.APPROVE_OPTION)) {
					return chooser.getSelectedFile();
				} else {
					return null;
				}
			} else {
				JOptionPane.showMessageDialog(this, "'" + mBank.getDirPath() + "' ist kein Directory",
						"PDF Datei selektieren", JOptionPane.ERROR_MESSAGE);

			}
		} catch (FibuRuntimeException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "PDF einlesen", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return null;
	}


	/**
	 * Test PDF mit PDFBox File öffnen, funktioniert
	 */
	public PdfWordStripper pdfStripWords(File file) {
		PdfWordStripper stripper = null;
		if (!file.exists()) {
			return null;
		}
		try {
			PDDocument document = PDDocument.load(file);
			stripper = new PdfWordStripper();
			stripper.setSortByPosition(true);
			stripper.setStartPage(0);
			stripper.setEndPage(document.getNumberOfPages());
			Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
			// hier werden die Worte in den Stripper geschrieben
			stripper.writeText(document, dummy);
		} catch (Exception ex) {
			Trace.println(1, "error: " + ex.getMessage());
		}
		return stripper;
	}



	@Override
	public void tableChanged(TableModelEvent e) {
		mTableView.setModel(mTableModel);
//        initTable();
 	}


	/**
	 * In der DB speichern
	 */
	private void speichern() {
		mBank.setWordBefore(mWordBefore.getText());
		mBank.setSpalteDatum(mDatumSpalte.getText());
		mBank.setSpalteText(mTextSpalte.getText());
		mBank.setSpalteSoll(mSollSpalte.getText());
		mBank.setSpalteHaben(mHabenSplalte.getText());
		mBank.setSpaltenArray(mBank.getSpaltenArray());

		try {
			mBankData = (CsvBankData) DataBeanContext.getContext().getDataBean(CsvBankData.class);
			mBankData.addData(mBank);
		}
		catch (FibuException ex) {
			Trace.println(3, "PdfSetupFrame.readData() Fehler: " + ex.getMessage());
		}
	}


	/** wenn Fenster geschlossen */
	@Override
	public void setVisible(boolean b) {
		if (!b) {
			Config.winPdfSetupDim = getSize();
			Config.winPdfSetupLoc = getLocation();
		}
		super.setVisible(b);
	}



// ----- Model der ersten Zeile ---------------------------------------------

	/** Schnittstelle zum Daten-Objekt KontoData */
	private class PdfBuchungModel extends AbstractTableModel {

		private static final long serialVersionUID = -3805602970105237582L;

		private List<String> data;

		public PdfBuchungModel() {
			data = new ArrayList<>();
			data.add("1 ");
			data.add("2 ");
		}

		public void addData(List<String> data) {
			this.data = data;
			fireTableDataChanged();
			fireTableStructureChanged();
		}

		@Override
		public int getColumnCount() {
			return data.size();
		}

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public String getColumnName(int col) {
			return "Spalte " + Integer.toString(col + 1);
		}

		/**
		 * Alle Zellen 0..1 können nicht editiert werden.
		 */
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data.get(columnIndex);
		}
	}

}
