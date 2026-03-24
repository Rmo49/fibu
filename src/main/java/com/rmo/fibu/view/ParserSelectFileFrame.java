package com.rmo.fibu.view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.ParserKeywordData;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.DatumFormat;
import com.rmo.fibu.util.ParserBank;
import com.rmo.fibu.util.ParserBase;
import com.rmo.fibu.util.Trace;

/**
 * Liest CSV Keywords ein, schreibt diese in Tabelle.
 * Wenn: "CSV-datei selektieren":
 * Eine Auswahl von dateien anzeigen, wenn ein File selektiert, wird das file an
 * CsvReaderTableFrame weitergegeben.
 */
public class ParserSelectFileFrame extends JFrame {

	private static final long serialVersionUID = 6445113637284754031L;

	private BuchungView mParent = null;

	// der Name des Institus von dem parser-buchungen eingelesen werden.
	private ParserBank mBank = null;

	private String mBankFullName = null;

	private JTextField mDirPath;
	private final Dimension dirPathSize = new Dimension(30 * Config.windowTextSize, Config.windowTextSize + 12);

	// die TextFelder für die Eingabe des Datums
	private JTextField mTfDatumAb; // ab Datum
	private JTextField mTfDatumBis; // bis Datum
	private final Dimension mDatumSize = new Dimension(8 * Config.windowTextSize, Config.windowTextSize + 12);

	/** Verbindung zur DB */
	private ParserKeywordData mKeywordData = null;

	// Das Frame für Schlüsseldaten bearbeiten
	private ParserKeywordFrame mParserKeywordFrame = null;

	// Das Frame für die Buchungen bearbeiten
	private ParserBuchungFrame mParserBuchungFrame = null;

	/**
	 * Wird gestartet von Buchungen mit der gewählten ID der Bank
	 *
	 * @param pBankId, ID der gewählten Bank
	 * @param pParent     Referenz zu den Buchungen
	 */
	public ParserSelectFileFrame(ParserBank pBank, BuchungView pParent) {
		super("Buchungen einlesen, V2.0");
		mBank = pBank;
		mParent = pParent;
	}

	/**
	 * Start der Initialisierung, muss von jedem Konstruktor aufgerufen werden.
	 */
	public boolean init() {
		Trace.println(3, "ParserSelectFileFrame.init()");
		String err = null;
		// wenn PDF
		if (mBank.getDocType() == ParserBase.docTypePdf) {
			err = ParserBase.parserVorhanden(ParserBase.docTypePdf, mBank.getBankName());
		}
		else {
			// prüfen, ob auch eine Implementation vorhanden ist.
			err = ParserBase.parserVorhanden(ParserBase.docTypeCsv, mBank.getBankName());
		}
		if (err.length() > 1) {
			JOptionPane.showMessageDialog(this, err, "Parser Datei selektieren", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		mKeywordData = (ParserKeywordData) DataBeanContext.getDataBean(ParserKeywordData.class);

		// die Version zurücksetzen, wenn das erstemal geöffnet
		mKeywordData.resetVersion();
		initView();
		return true;
	}

	/**
	 * Initialisierung der verschiedenen Views
	 */
	private void initView() {
		Trace.println(5, "ParserKeywordFrame.initView()");
		getContentPane().add(initPanels());
		if ( (Config.winParserSelectFileDim.height < 100) || (Config.winParserSelectFileDim.width < 300)) {
			Config.winParserSelectFileDim.height = 600;
			Config.winParserSelectFileDim.width = 400;
		}
		setSize(Config.winParserSelectFileDim);
		setLocation(Config.winParserSelectFileLoc);
	}

	/**
	 * Buttons, Selektionsfelder
	 *
	 * @return
	 */
	private Container initPanels() {
		JPanel lPanel = new JPanel();
		lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.PAGE_AXIS));

		JScrollPane scroller = new JScrollPane(lPanel);
		scroller.setPreferredSize(new Dimension(250, 80));
		scroller.setAlignmentX(LEFT_ALIGNMENT);

		lPanel.add(initTitel());
		lPanel.add(initDirectory());
		lPanel.add(initSelectFile());
		lPanel.add(initBottom());
		return scroller;
	}

	private JPanel initTitel( ) {
		JPanel flow = new JPanel(new FlowLayout());
		mBankFullName = mBank.getBankName();
		flow.add(new JLabel("Buchungen von: "));
		JLabel labelBank = new JLabel(mBankFullName);
		labelBank.setFont(Config.fontTextBold);
		flow.add(labelBank);
		flow.add(new JLabel(" einlesen, evt. zuerst Schlüsselworte eingeben"));
		return flow;
	}

	/**

	/**
	 * Die Zeile mit dem Directory
	 * @return
	 */
	private JPanel initDirectory() {
		// Directory eingenben
		JPanel flow3 = new JPanel(new FlowLayout());
		JLabel label2 = new JLabel("Directory: ");
		label2.setFont(Config.fontTextBold);
		flow3.add(label2);

		mDirPath = new JTextField();
		mDirPath.setPreferredSize(dirPathSize);
		mDirPath.setText(mBank.getDirPath());
		mDirPath.setFont(Config.fontText);

		flow3.add(mDirPath);
		return flow3;
	}

	/**
	 * Selektion der File Dautm von ... bis
	 * @return
	 */
	private JPanel initSelectFile() {
		// datei selektieren
		JPanel flow4 = new JPanel(new FlowLayout());
		JButton btnSelectFile = new JButton("Datei mit Buchungen selektieren");
		btnSelectFile.setFont(Config.fontTextBold);

		btnSelectFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO geht das?
				mBank.setDirPath(mDirPath.getText());
				showFiles();
			}
		});
		flow4.add(btnSelectFile);

		// --- Ab Datum
		JLabel labelDatum = new JLabel("von: ");
		labelDatum.setFont(Config.fontTextBold);
		flow4.add(labelDatum);
		mTfDatumAb = new JTextField(Config.sDatumVon.toString());
		mTfDatumAb.setFont(Config.fontText);
		mTfDatumAb.setPreferredSize(mDatumSize);
		flow4.add(mTfDatumAb);

		// --- bis Datum
		JLabel labelDatum2 = new JLabel("bis: ");
		labelDatum2.setFont(Config.fontTextBold);
		flow4.add(labelDatum2);
		mTfDatumBis = new JTextField(Config.sDatumBis.toString());
		mTfDatumBis.setFont(Config.fontText);
		mTfDatumBis.setPreferredSize(mDatumSize);
		flow4.add(mTfDatumBis);

		return flow4;
	}


	/**
	 * Button Schlüsselworte
	 *
	 * @return
	 */
	private Container initBottom() {
		JPanel flow3 = new JPanel(new FlowLayout());
		JLabel label2 = new JLabel("Für " + mBankFullName + " Steuerung anpassen: ");
		label2.setFont(Config.fontText);
		flow3.add(label2);

		JButton btnKeyWords = new JButton("Schlüsselworte eingeben");
		btnKeyWords.setFont(Config.fontTextBold);

		btnKeyWords.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mParserKeywordFrame = new ParserKeywordFrame(mBank, mParent);
				mParserKeywordFrame.setVisible(true);
			}
		});
		flow3.add(btnKeyWords);
		return flow3;
	}



	/**
	 * Mögliche CSV-files anzeigen, eines selektieren
	 */
	private void showFiles() {
		// prüfen, ob eintrag im Feld directory
		if (mDirPath.getText() == null) {
			JOptionPane.showMessageDialog(this, "Directory fehlt", "Buchungen einlesen", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			File file = new File(mDirPath.getText());
			if (file.isDirectory()) {
				// save the new name
				Config.sParserFileName = mDirPath.getText();
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(file);
				if (mBank.getDocType() == ParserBase.docTypeCsv) {
					chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));
				}
				else {
					chooser.setFileFilter(new FileNameExtensionFilter("PDF", "pdf"));
				}
				int returnValue = chooser.showOpenDialog(this);
				if ((returnValue == JFileChooser.APPROVE_OPTION)) {
					file = chooser.getSelectedFile();

					mParserBuchungFrame = new ParserBuchungFrame(file, mBank, getSelectedDateAb(), getSelectedDateBis());
					mParserBuchungFrame.setVisible(true);
				} else {
					return;
				}
			} else {
				JOptionPane.showMessageDialog(this, "'" + mDirPath.getText() + "' ist kein Directory",
						"Buchungen einlesen", JOptionPane.ERROR_MESSAGE);

			}
		} catch (FibuRuntimeException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Buchungen einlesen", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	/**
	 * Das eingegebenen Datums ab
	 */
	private Date getSelectedDateAb() {
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

	/**
	 * Das eingegebenen Datums ab
	 */
	private Date getSelectedDateBis() {
		Date datum = null;
		if (mTfDatumBis.getText().length() == 0) {
			mTfDatumBis.setText(Config.sDatumVon.toString());
		}
		DatumFormat df = DatumFormat.getDatumInstance();
		try {
			datum = df.parse(mTfDatumBis.getText());
			return datum;
		} catch (ParseException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(),
					"Datum fehlerhaft", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}


	/** wenn Fenster geschlossen */
	@Override
	public void setVisible(boolean b) {
		if (!b) {
			Config.winParserSelectFileDim = getSize();
			Config.winParserSelectFileLoc = getLocation();
			mParent.resetParserKeywordFrame();
			if (mParserBuchungFrame != null) {
				mParserBuchungFrame.setVisible(b);
			}
		}
		super.setVisible(b);
	}
}
