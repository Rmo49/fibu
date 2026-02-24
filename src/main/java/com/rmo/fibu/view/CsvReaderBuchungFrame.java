package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import com.rmo.fibu.exception.BuchungValueException;
import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungCsv;
import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.CsvBank;
import com.rmo.fibu.model.CsvBankData;
import com.rmo.fibu.model.CsvKeyKonto;
import com.rmo.fibu.model.CsvKeyKontoData;
import com.rmo.fibu.model.CsvParserBase;
import com.rmo.fibu.model.CsvParserCs;
import com.rmo.fibu.model.CsvParserMB;
import com.rmo.fibu.model.CsvParserPost;
import com.rmo.fibu.model.CsvParserRaiff;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.JsonFile;
import com.rmo.fibu.model.KontoNrVector;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.PdfParser;
import com.rmo.fibu.util.Trace;
import com.rmo.fibu.util.Datum;
import com.rmo.fibu.view.util.JLabelBold;
import com.rmo.fibu.view.util.JButtonBold;

/**
 * Wird über CsvReaderKeywordFrame gestartet, oder direkt von MainFrame
 * aufgerufen. Das ausgewählte file wird im Konstruktur übergeben. Liest
 * Buchungen von CSV ein siehe datenEinlesen, schreibt in Tabelle. Wenn
 * "Buchungstext anpassen" dann wird der Text angepasst und die Kontonummern
 * eingetragen => changeAction Wenn "Speichern" dann saveAction
 */
public class CsvReaderBuchungFrame extends JFrame {
	private static final long serialVersionUID = 1201522139173678122L;

	/** Die Grösse der Spalten */
	private static final int TEXT_WIDTH = 30;
	private static final int DEFAULT_WIDTH = 4;

	// der Name des Institus von dem csv-buchungen eingelesen werden.
	private String mBankName = null;
	private CsvBank mBank = null;
	// file von dem gelesen werden soll
	private File mFile = null;
	// Datum von bis
	private Datum mDateVon = null;
	private Datum mDateBis = null;
	// Prefix der eingefügt werden soll
	private JTextField mBuchungPrefix = null;
	private final Dimension prefixSize = new Dimension(10 * Config.windowTextSize, Config.windowTextSize + 12);
	// die TextFelder für die Eingabe des Datums
	private final Dimension mDatumSize = new Dimension(6 * Config.windowTextSize, Config.windowTextSize + 12);
	private JTextField mTfDatumAb; // ab Datum
	private Datum mDatumSelVon = null;
	private JTextField mTfDatumBis; // bis Datum
	private Datum mDatumSelBis = null;
	private JComboBox<String> mKontoNr = null;
	private JCheckBox mReplaceKtoNr = null;
	private JRadioButton mSollRadio = null;
	private JRadioButton mHabenRadio = null;

	// view elemente
	private JTable mTableView = new JTable();
	private DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();

	/** Das Model zu dieser View, Verbindung zur DB */
	private CsvBuchungModel mBuchungModel;
	// alle Buchungen in der Liste
	private List<BuchungCsv> mBuchungList = new ArrayList<>();
	Iterator<BuchungCsv> mBuchungListIter = null;

	/** Verbindung zur DB */
	private BuchungData mBuchungData = null;
	private String nextBelegNr = null;
	private int returnValue = 0;
	/** Damit die Version von Csv gelesen werden kann */
//	private CsvKeyKontoData mKeywordData = null;
	/** mit diesem Tag wird der Anfang und Ende der anzahl Worte angezeigt */
	private String tagStart = "/";
	private char tagWort = 'w';

	/**
	 * Konstruktor für einlesen von jsonFile, diese wurde noch nicht gespeichert
	 *
	 * @param bankName
	 */
	public CsvReaderBuchungFrame(String bankName) {
		super("CSV Buchungen noch nicht gespeichert");
		this.mBankName = bankName;
		init();
	}

	/**
	 * Construtor needs filename with CSV-data
	 */
	public CsvReaderBuchungFrame(File file, CsvBank bank, Date von, Date bis) {
		super("CSV Buchungen anpassen V2.0");
		Trace.println(3, "CsvReaderBuchungFrame(file: " + file.getAbsolutePath() + ")");
		this.mFile = file;
		this.mBank = bank;
		this.mBankName = bank.getBankName();
		this.mDateVon = new Datum();
		this.mDateVon.setDatum(von);
		this.mDateBis = new Datum();
		this.mDateBis.setDatum(bis);;
		init();
	}

	/**
	 * Start der Initialisierung, muss von jedem Konstruktor aufgerufen werden.
	 */
	private void init() {
		Trace.println(4, "CsvReaderBuchungFrame.init()");
//		mKeywordData = (CsvKeyKontoData) DataBeanContext.getContext().getDataBean(CsvKeyKontoData.class);
		// wenn Bank nicht gesetzt, dann von json einlesen
		if (mBankName.length() < 1) {
			mBuchungList = JsonFile.readFromFile();
			if (mBuchungList.size() > 0) {
				Trace.println(5, "Anzahl Buchungen gefunden: " + mBuchungList.size());

				mBankName = mBuchungList.get(0).getCompanyName();
				try {
					CsvBankData bankData = (CsvBankData) DataBeanContext.getContext().getDataBean(CsvBankData.class);
					this.mBank = bankData.readData(mBankName);
				} catch (FibuException ex) {
					Trace.println(1, "CsvReaderBuchungFrame Exception: " + ex.getMessage());
				}
			}
		} else {
			csvEinlesen();
		}
		if (!setDatumVonListe()) {
			return;
		}
		initView();
//		this.setVisible(true);
	}

	/**
	 * Setup view elements.
	 *
	 * @return the border pane
	 */
	private void initView() {
		Trace.println(4, "CsvReaderBuchungFrame.initView()");
		
//		Trace.println(5, "width: " + Config.winCsvReaderBuchungDim.width + " height: " + Config.winCsvReaderBuchungDim.height);
//		Trace.println(5, "x: " + Config.winCsvReaderBuchungLoc.x + " y: " + Config.winCsvReaderBuchungLoc.y);
		
		getContentPane().add(initTable(), BorderLayout.CENTER);
		getContentPane().add(initBottom(), BorderLayout.PAGE_END);
		this.setSize(Config.winCsvReaderBuchungDim);
		this.setLocation(Config.winCsvReaderBuchungLoc);
		this.pack();
	}

	/**
	 * Tabelle mit den Buchungen, kann editiert werden.
	 *
	 * @return
	 */
	private Container initTable() {
//		mKeywordModel = new CsvKeywordModel(mBank.getBankID());
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
				cellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
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
		JComboBox<String> kontoNummern = new JComboBox<>();
		kontoNummern.setModel(new DefaultComboBoxModel<>(new KontoNrVector()));
		kontoNummern.setFont(Config.fontText);

		TableColumn ktoNrColumn = mTableView.getColumnModel().getColumn(3);
		ktoNrColumn.setCellEditor(new DefaultCellEditor(kontoNummern));
		ktoNrColumn = mTableView.getColumnModel().getColumn(4);
		ktoNrColumn.setCellEditor(new DefaultCellEditor(kontoNummern));
	}

	/**
	 * Buttons setzen
	 *
	 * @return
	 */
	private Container initBottom() {
		JPanel buttons = new JPanel(new GridLayout(4, 1));

		JPanel buttons1 = new JPanel(new FlowLayout());

		JButtonBold btnChange = new JButtonBold("Buchungstext anpassen");
		btnChange.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buchungenAnpassen();
			}
		});
		buttons1.add(btnChange);
		buttons.add(buttons1);

		// ------ die 2. Zeile
		JPanel buttons2 = new JPanel(new FlowLayout());

		JLabelBold labelDatum = new JLabelBold("ändern von: ");
		buttons2.add(labelDatum);
		mTfDatumAb = new JTextField(mDatumSelVon.toString());
		mTfDatumAb.setFont(Config.fontText);
		mTfDatumAb.setPreferredSize(mDatumSize);
		buttons2.add(mTfDatumAb);

		// --- bis Datum
		JLabelBold labelDatum2 = new JLabelBold("bis: ");
		buttons2.add(labelDatum2);
		mTfDatumBis = new JTextField(mDatumSelBis.toString());
		mTfDatumBis.setFont(Config.fontText);
		mTfDatumBis.setPreferredSize(mDatumSize);
		buttons2.add(mTfDatumBis);

		JLabelBold label2 = new JLabelBold("Prefix: ");
		buttons2.add(label2);

		mBuchungPrefix = new JTextField();
		mBuchungPrefix.setPreferredSize(prefixSize);
		mBuchungPrefix.setFont(Config.fontText);
		buttons2.add(mBuchungPrefix);

		JButtonBold btnPrefix = new JButtonBold("Prefix einsetzen");
		btnPrefix.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				prefixInsert();
			}
		});
		buttons2.add(btnPrefix);
		buttons.add(buttons2);

		// ----- 3. Zeile
		JPanel buttons3 = new JPanel(new FlowLayout());

		JLabelBold label3 = new JLabelBold("KontoNr: ");
		buttons3.add(label3);

		mKontoNr = new JComboBox<>();
		mKontoNr.setModel(new DefaultComboBoxModel<>(new KontoNrVector()));
		mKontoNr.setFont(Config.fontText);
		buttons3.add(mKontoNr);

		mSollRadio = new JRadioButton("Soll");
		mSollRadio.setSelected(true);
		buttons3.add(mSollRadio);

		mHabenRadio = new JRadioButton("Haben");
		buttons3.add(mHabenRadio);

		ButtonGroup group = new ButtonGroup();
		group.add(mSollRadio);
		group.add(mHabenRadio);

		mReplaceKtoNr = new JCheckBox("überschreiben");
		mReplaceKtoNr.setSelected(false);
		buttons3.add(mReplaceKtoNr);

		JButtonBold btnKtoNr = new JButtonBold("KontoNr. ändern");
		btnKtoNr.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setKontoNummer();
			}
		});
		buttons3.add(btnKtoNr);
		buttons.add(buttons3);

		// ----- 4. Zeile
		JPanel buttons4 = new JPanel(new FlowLayout());

		JButtonBold btnSaveFibu = new JButtonBold("Speichern in Fibu");
		btnSaveFibu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveInFibu();
				mBuchungModel.fireTableDataChanged();
			}
		});
		buttons4.add(btnSaveFibu);

		JButtonBold btnSaveFile = new JButtonBold("Speichern in Datei");
		btnSaveFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveInDatei();
			}
		});
		buttons4.add(btnSaveFile);
		buttons.add(buttons4);

		return buttons;
	}

	// ---------- Einlesen ----------------------------

	/**
	 * Einlesen alle Daten vom CSV file oder PDF file, Zeile um Zeile.
	 */
	private void csvEinlesen() {
		Trace.println(5, "CsvReaderBuchungFrame.csvEinlesen()");
		if (mBank.getDocType() == CsvBank.docTypeCsv) {
			// wenn von CSV einlesen
			CsvParserBase csvParser = null;
			if (mBankName.equalsIgnoreCase(CsvParserBase.companyNamePost)) {
				csvParser = new CsvParserPost(mFile, mDateVon, mDateBis);
			} else if (mBankName.equalsIgnoreCase(CsvParserBase.companyNameCS)) {
				csvParser = new CsvParserCs(mFile, mDateVon, mDateBis);
			} else if (mBankName.equalsIgnoreCase(CsvParserBase.companyNameRaiff)) {
				csvParser = new CsvParserRaiff(mFile, mDateVon, mDateBis);
			} else if (mBankName.equalsIgnoreCase(CsvParserBase.companyNameMB)) {
				csvParser = new CsvParserMB(mFile, mDateVon, mDateBis);
			} else {
				StringBuffer sb = new StringBuffer(100);
				sb.append("Kein Setup für: '");
				sb.append(mBankName);
				sb.append("' gefunden \n Impmentationen vorhanden von: ");
				sb.append(CsvParserBase.companyNamePost);
				sb.append(", ");
				sb.append(CsvParserBase.companyNameCS);
				sb.append(", ");
				sb.append(CsvParserBase.companyNameRaiff);
				sb.append(", ");
				sb.append(CsvParserBase.companyNameMB);
				sb.append(" gefunden");
				JOptionPane.showMessageDialog(this, sb.toString(), "CSV Datei selektieren", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// hier werden die Daten eingelesen, Zeile um Zeile
			Trace.println(5, "CsvReaderBuchungFrame.datenEinlesen() => start Parsing CSV");
			mBuchungList = csvParser.startParsing(mBank);
		} else {
			// wenn von PDF einlesen
			Trace.println(5, "CsvReaderBuchungFrame.datenEinlesen() => start Parsing PDF");
			PdfParser pdfParser = new PdfParser(mFile, mDateVon, mDateBis);
			mBuchungList = pdfParser.startParsing(mBank);
			if (mBuchungList.size() <= 0) {
				StringBuffer sb = new StringBuffer(100);
				sb.append("Keine Buchungen für: '");
				sb.append(mBankName);
				sb.append("' gefunden. \n");
				sb.append("PDF Steuerdaten anpassen in: Setup > [PDF Steuerdaten eingeben]");
				JOptionPane.showMessageDialog(this, sb.toString(), "PDF Datei selektieren", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		Trace.println(5, "CsvReaderBuchungFrame.csvEinlesen() => end Parsing");
	}

	// ------- Buchungen automatisch anpasse ---------------------------------

	/**
	 * Die standard KontoNr
	 */
	protected String getKontoNrDefault() {
		return mBank.getKontoNrDefault();
	}

	/**
	 * Alle Buchungen anpassen Buchungstext und Konto falls Tag gefunden in Keyword.
	 */
	private void buchungenAnpassen() {
		Iterator<BuchungCsv> iter = mBuchungList.iterator();
		// Iteration über alle Buchungen
		while (iter.hasNext()) {
			buchungAnpassen(iter.next());
		}
		mTableView.repaint();
	}

	/**
	 * prüft Buchungstext, wenn tag in Keywords gefunden, wird das entsprechende
	 * Konto gesetzt falls dieses gesetzt, wenn kein Konto angegeben, wird nur der
	 * Text gelöscht, bzw. ersetzt mit dem neuen Text
	 */
	protected void buchungAnpassen(BuchungCsv buchungCsv) {
		Trace.println(7, "CsvReaderBuchungFrame.buchungAnpassen()");
		if (buchungCsv == null) {
			return;
		}
		// Die Liste der Keyword, iterator mit der ID der Bank gelesen
		CsvKeyKontoData keywordData = (CsvKeyKontoData) DataBeanContext.getContext().getDataBean(CsvKeyKontoData.class);
		Iterator<CsvKeyKonto> lIter = keywordData.getIterator(mBank.getBankID());
		int pos = -1;
		// Iteration über alle Keywörter
		while (lIter.hasNext()) {
			CsvKeyKonto keyword = lIter.next();
			pos = posSuchWort(buchungCsv.getText(), keyword.getSuchWort());
			// Suchwort gefunden
			if (pos >= 0) {
				// wenn keine KontoNummer gesetzt, nur Text ändern
				if (keyword.getKontoNr().length() < 2) {
					// nur Text entfernen
					buchungCsv.setText(textDelete(buchungCsv.getText(), keyword.getSuchWort().length(), pos));
				} else {
					// Kontonummer einsetzen
					setKonto(buchungCsv, keyword);
					buchungCsv.setText(textChange(buchungCsv.getText(), keyword.getTextNeu(), pos));
				}
			}
		}
		// zum Schluss Länge noch prüfen
		textMaxLen(buchungCsv, pos);
	}

	/**
	 * Wort Suchen im Text gibt position zurück. Wenn nur ein Wort gesucht wird,
	 * startet bei ganzen Wörtern. Wenn SuchWort aus mehreren Wörtern, dann wird der
	 * gesamte String durchsucht.
	 *
	 * @param buchungText
	 * @param suchWort
	 * @return
	 */
	private int posSuchWort(String buchungText, String suchWort) {
		if (suchWort.indexOf(" ") > 0) {
			return buchungText.indexOf(suchWort);
		}
		int pos = 0;
		int wortStart = 0;
		boolean found = false;
		String text = new String();
		while (pos < buchungText.length()) {
			if (buchungText.charAt(pos) == 32) {
				text = buchungText.substring(wortStart, pos).toUpperCase();
				found = text.startsWith(suchWort.toUpperCase());
				if (!found) {
					wortStart = ++pos;

				} else {
					return wortStart;
				}
			}
			pos++;
		}
		return -1;
	}

	/**
	 * Die Kontonummer setzen wenn etwas gefunden im Text.
	 */
	private void setKonto(BuchungCsv buchungCsv, CsvKeyKonto keyword) {
		if (keyword.getSh().equalsIgnoreCase("H")) {
			buchungCsv.setHaben(keyword.getKontoNr());
		} else {
			buchungCsv.setSoll(keyword.getKontoNr());
		}
	}

	/**
	 * Den Text kürzen, falls sehr Lang
	 */
	private void textMaxLen(BuchungCsv buchungCsv, int pos) {
		int maxLength = buchungCsv.getText().length();
		if (pos < 0) {
			pos = 0;
		} else {
			if (pos >= maxLength) {
				pos = 0;
			}
		}
		if (maxLength > pos + Config.sCsvTextLen) {
			maxLength = pos + Config.sCsvTextLen;
		}
		buchungCsv.setText(buchungCsv.getText().substring(pos, maxLength));
	}

	/**
	 * Den Text der länge textLen löschen, falls keine Kontonummer angegeben. Für
	 * Version 3 und höher.
	 */
	private String textDelete(String buchungText, int textLen, int pos) {
		StringBuffer textNew = new StringBuffer(100);
		// wenn nicht am Anfang, dann ersten Bereich kopieren
		if (pos > 0) {
			textNew.append(buchungText.substring(0, pos));
		}
		int posStart = pos + textLen;
		// Leerzeichen löschen
		while (posStart < buchungText.length() && buchungText.charAt(posStart) == ' ') {
			posStart++;
		}
		textNew.append(buchungText.substring(posStart));
		return textNew.toString();
	}

	/**
	 * Den Text ersetzen, gemäss angaben im CsvKeyKonto. Für Version 3 und höher.
	 */
	private String textChange(String buchungText, String keywordText, int pos) {
		StringBuffer textNew = new StringBuffer(100);
		// tag für next words lesen, also den "/"
		int posTag = -1;
		if (keywordText != null) {
			keywordText = keywordText.trim();
			posTag = keywordText.indexOf(tagStart);
		}
		int anzWorte = 0;
		if (posTag >= 0) {
			for (int i = posTag; i < keywordText.length(); i++) {
				if (keywordText.charAt(i) == tagWort) {
					anzWorte++;
				}
			}
		}
		if (posTag < 0) {
			// kein Tag gefunden, wenn Eintrag in keyword dann diesen eingeben
			if ((keywordText != null) && (keywordText.length() > 0)) {
				textNew.append(keywordText);
			} else {
				textNew.append(buchungText.substring(pos));
			}
		} else if (posTag == 0) {
			// Wurde kein Text vor dem Tag eingegeben, dann das erste Wort von der Buchung
			// kopieren
			int posSpace = buchungText.indexOf(" ", pos);
			textNew.append(buchungText.substring(pos, posSpace + 1));
			pos = posSpace;
		} else if (posTag > 1) {
			// ein Text wurde eingegeben, diese in den Text kopieren
			textNew.append(keywordText.substring(0, posTag));
			// Tag in der Buchung überspringen
			pos = buchungText.indexOf(" ", pos + 1);
		}
		if (anzWorte > 0) {
			// Leezeichen suchen
			int p = pos + 1;
			while (p < buchungText.length()) {
				p = buchungText.indexOf(" ", p);
				if (p == -1) {
					p = buchungText.length();
				}
				anzWorte--;
				if (anzWorte <= 0) {
					break;
				}
				p++;
			}
			if (p > buchungText.length()) {
				p = buchungText.length();
			}
			textNew.append(buchungText.substring(pos + 1, p));
		}
		if (textNew.length() > Config.sCsvTextLen) {
			textNew.delete(Config.sCsvTextLen, textNew.length());
		}
		return textNew.toString();
	}

	// ------- manuell bearbeiten -----------------------

	/**
	 * Die Datum von bis gemäss eingelesener Liste setzen
	 */
	private boolean setDatumVonListe() {
		Trace.println(5, "CsvReaderBuchungFrame.setDatumVonListe()");
		boolean returnValue = true;
		mBuchungListIter = mBuchungList.iterator();
		String von = Config.sDatumVon.toString();
		try {
			// das erste Datum
			von = mBuchungListIter.next().getDatum();
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Keine Daten vorhanden", "Buchungen einlesen", JOptionPane.INFORMATION_MESSAGE);
			returnValue = false;
		}
		String bis = Config.sDatumBis.toString();
		while (mBuchungListIter.hasNext()) {
			bis = mBuchungListIter.next().getDatum();
		}
		try {
			mDatumSelVon = new Datum(von);
			mDatumSelBis = new Datum(bis);
		}
		catch (ParseException ex) {
			Trace.println(1, "CsvReaderBuchungFrame.setDatumVonListe(), Probleme beim Datum lesen");
		}
		Trace.println(5, "CsvReaderBuchungFrame.setDatumVonListe() : " + returnValue);
		return returnValue;
	}

	/**
	 * Den Prefix-Wert in selektierten Buchungen eintragen
	 */
	private void prefixInsert() {
		if (!setSelectedDate()) return;
		mBuchungListIter = mBuchungList.iterator();
		// Iteration über alle Buchungen
		while (mBuchungListIter.hasNext()) {
			prefixInBuchung(mBuchungListIter.next());
		}
		mTableView.repaint();
	}

	/**
	 * Den insert-String in den Buchungstext einbauen.
	 * 
	 * @param csvBuchung
	 */
	private void prefixInBuchung(BuchungCsv csvBuchung) {
		if (inRange(csvBuchung)) {
			String text = mBuchungPrefix.getText() + csvBuchung.getText();
			text = text.length() > Config.sCsvTextLen ? text.substring(0, Config.sCsvTextLen) : text;
			csvBuchung.setText(text);
		}
	}

	/**
	 * Die selektierte Datum in Date setzen
	 */
	private boolean setSelectedDate() {
		try {
			mDatumSelVon = new Datum(mTfDatumAb.getText());
			mDatumSelBis = new Datum(mTfDatumBis.getText());
		}
		catch (ParseException ex) {
			JOptionPane.showMessageDialog(this, "Datumformat falsch", "Datum", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		return true;
	}
	
	/**
	 * Ist das Datum der Buchung innerhalb des Ranges
	 * 
	 * @param csvBuchung
	 * @return
	 */
	private boolean inRange(BuchungCsv csvBuchung) {
		Datum datum = null;
		try {
			datum = new Datum(csvBuchung.getDatum());
		} catch (ParseException ex) {
			// TODO wenn falsches Datum Meldung
		}
		if ((datum.compareTo(mDatumSelVon) >= 0) && (datum.compareTo(mDatumSelBis) <= 0)) {
			return true;
		}
		return false;
	}

	/**
	 * Die Kontonummer einsetzen in selektierten Buchungen
	 */
	private void setKontoNummer() {
		if (!setSelectedDate()) return;
		mBuchungListIter = mBuchungList.iterator();
		// Iteration über alle Buchungen
		while (mBuchungListIter.hasNext()) {
			setKtoNrInBuchung(mBuchungListIter.next());
		}
		mTableView.repaint();
	}

	/**
	 * Kontonummer in der Buchung einsetzen
	 * @param csvBuchung
	 */
	private void setKtoNrInBuchung(BuchungCsv csvBuchung) {
		if (inRange(csvBuchung)) {
			String ktoNr = (String) mKontoNr.getSelectedItem();
			if (mSollRadio.isSelected()) {
				if (mReplaceKtoNr.isSelected()) {
					csvBuchung.setSoll(ktoNr);
				} else {
					if ((csvBuchung.getSoll() == null) || (csvBuchung.getSoll().length() == 0)) {
						csvBuchung.setSoll(ktoNr);
					}
				}
			}
			else {
				if (mReplaceKtoNr.isSelected()) {
					csvBuchung.setHaben(ktoNr);
				} else {
					if ((csvBuchung.getHaben()== null) || (csvBuchung.getHaben().length() == 0)) {
						csvBuchung.setHaben(ktoNr);
					}
				}
			}
		}
	}

	
	/**
	 * Das eingegebenen Datums ab
	 */
//	private Date getSelectedDate() {
//		Date datum = null;
//		if (mTfDatumAb.getText().length() == 0) {
//			mTfDatumAb.setText(Config.sDatumVon.toString());
//		}
//		DatumFormat df = DatumFormat.getDatumInstance();
//		try {
//			datum = df.parse(mTfDatumAb.getText());
//			return datum;
//		} catch (ParseException ex) {
//			JOptionPane.showMessageDialog(this, ex.getMessage(),
//					"Datum fehlerhaft", JOptionPane.ERROR_MESSAGE);
//			return null;
//		}
//	}

	// ------- speichern ---------------------

	/**
	 * Die Buchungen in der DB speichern.
	 */
	private void saveInFibu() {
		mBuchungData = (BuchungData) DataBeanContext.getContext().getDataBean(BuchungData.class);
		BuchungCsv buchungCsv = null;
		Buchung buchungNew = new Buchung();
		Iterator<BuchungCsv> iter = mBuchungList.iterator();
		while (iter.hasNext()) {
			buchungCsv = iter.next();
			returnValue = copyToBuchung(buchungCsv, buchungNew);
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
		// das backup-file löschen
		JsonFile.delete();
	}

	/**
	 * In einen Json-File speichern.
	 */
	private void saveInDatei() {
		String antwort = JsonFile.saveInFile(mBankName, mBuchungList);
//		mBuchungModel.fireTableDataChanged();
		JOptionPane.showMessageDialog(this, antwort, "Sichern in Datei", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Von der CSV Liste in Buchung für Fibu schreiben
	 * 
	 * @param buchungCsv
	 * @param buchung
	 * @return -1: Fehler und abbrechen, 0: Fehler, nicht abbrechen 1: alles ok
	 */
	private int copyToBuchung(BuchungCsv buchungCsv, Buchung buchung) {
		buchung.setID(-1);
		try {
			buchung.setDatum(buchungCsv.getDatum());
		} catch (ParseException ex) {
			int reply = JOptionPane.showConfirmDialog(this,
					makeMessage(buchungCsv.getDatum(), buchungCsv.getText(), ex.getMessage()), "Datum falsch",
					JOptionPane.OK_CANCEL_OPTION);
			if (reply == JOptionPane.OK_OPTION) {
				returnValue = 0;
			} else {
				return -1;
			}
		}

		// damit exist ohne beleg funktioniert
		buchung.setBeleg(null);

		buchung.setBuchungText(buchungCsv.getText());
		try {
			buchung.setSoll(buchungCsv.getSoll());
			buchung.setHaben(buchungCsv.getHaben());
		} catch (BuchungValueException ex) {
			int reply = JOptionPane.showConfirmDialog(this,
					makeMessage(buchungCsv.getDatum(), buchungCsv.getText(), ex.getMessage()), "KontoNr falsch",
					JOptionPane.OK_CANCEL_OPTION);
			if (reply == JOptionPane.OK_OPTION) {
				return 0;
			} else {
				return -1;
			}
		}

		try {
			double betrag = parseBetrag(buchungCsv.getBetrag());
			buchung.setBetrag(betrag);
		} catch (NumberFormatException ex) {
			int reply = JOptionPane.showConfirmDialog(this,
					makeMessage(buchungCsv.getDatum(), buchungCsv.getText(), ex.getMessage()), "Betrag falsch",
					JOptionPane.OK_CANCEL_OPTION);
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
	 *
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
			sb.append("\n Buchung neu: ");
			sb.append(buchungNew.getBuchungText());
			sb.append("\n\n Trotzdem übernehmen?");
			int reply = JOptionPane.showConfirmDialog(this, sb.toString(), "Buchung schon vorhanden",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (reply == JOptionPane.YES_OPTION) {
				return 1;
			} else if (reply == JOptionPane.NO_OPTION) {
				return 0;
			} else {
				return -1;
			}
		}
		return 1;
	}

	/**
	 * Speichern der Buchung in der DB
	 *
	 * @param buchungNew
	 * @return 1: gespeichert, -1: Abbrechen
	 */
	private int saveBuchung(Buchung buchungNew) {
		if (nextBelegNr == null) {
			nextBelegNr = mBuchungData.getNextBelegNr();
		} else {
			nextBelegNr = Config.addOne(nextBelegNr);
		}
		buchungNew.setBeleg(nextBelegNr);
		// hier wird die Buchung gesichert
		mBuchungData.add(buchungNew);
		try {
			mBuchungData.saveNew();
		} catch (FibuException ex) {
			StringBuffer sb = new StringBuffer(100);
			sb.append("Buchung: ");
			sb.append(buchungNew.getDatum());
			sb.append(" ");
			sb.append(buchungNew.getBuchungText());
			sb.append("\n Exception: ");
			sb.append(ex.getMessage());
			int reply = JOptionPane.showConfirmDialog(this, sb.toString(), "Fehler beim sichern",
					JOptionPane.OK_CANCEL_OPTION);
			if (reply == JOptionPane.OK_OPTION) {
				return 1;
			} else {
				return -1;
			}
		}
		return 1;
	}

	/**
	 * Den Betrag in double zurückgeben
	 *
	 * @param betrag
	 * @return
	 * @throws NumberFormatException
	 */
	private double parseBetrag(String betrag) throws NumberFormatException {
		// zuerst leezeichen entfernen
		betrag = betrag.trim();
		int pos = betrag.indexOf(" ");
		if (pos > 0) {
			betrag = betrag.substring(0, pos) + betrag.substring(pos + 1);
		}
		double betrag2 = 0;
		betrag2 = Double.parseDouble(betrag);
		return betrag2;
	}

	/**
	 * Die Error-Message zusammenstellen
	 *
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
		 * Wenn eine Zelle editiert wurde, diesen Wert in der Liste speichern
		 */
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			BuchungCsv lBuchung = mBuchungList.get(rowIndex);
			if (columnIndex == 0) {
				lBuchung.setDatum((Date) aValue);
			} else if (columnIndex == 1) {
				lBuchung.setBeleg((String) aValue);
			} else if (columnIndex == 2) {
				lBuchung.setText((String) aValue);
			} else if (columnIndex == 3) {
				lBuchung.setSoll((String) aValue);
			} else if (columnIndex == 4) {
				lBuchung.setHaben((String) aValue);
			} else if (columnIndex == 5) {
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
