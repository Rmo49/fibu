package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.CsvBank;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.FibuData;
import com.rmo.fibu.model.FibuDataBase;
import com.rmo.fibu.model.JsonFile;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.DatumFormat;
import com.rmo.fibu.util.Trace;

/**
 * Fibu Start-View. Auswahl der Fibu-DB, setzen der Parameauter und starten der
 * Unterprogramme. Diese View wurde mit dem Painter von JBuilder erstellt.
 *
 * @author R. Moser
 * @version (Drucker-Font: Arial)
 */

public class FibuView extends JFrame
// implements ListSelectionListener
{
	private static final long serialVersionUID = -6489792909275868353L;

	private final int FENSTER_BREITE = 300;
	private final int FENSTER_HOEHE = 420;

	private final int FIBU_LIST_BREITE = 150;
	private final int FIBU_LIST_HOEHE = 200;

	/** Verbindung zu Data bean */
	FibuData mFibuData;

	// --- Die anderen Windows
	private BuchungView mBuchung = null;
	private CsvReaderBuchungFrame mCsvBuchung = null;
	private FibuCopyFrom mOtherFibu = null;
	private KontoView mKontoView = null;
	private KontoplanView mKontoplan = null;
	private BilanzenView mAuswertung = null;

	// --- Buttons die allg. gesteuert werden
	JButton btnOpen = new JButton();
	JButton btnKontoplan = new JButton();
	JButton btnKontoblatt = new JButton();
	JButton btnBuchung = new JButton();
	JButton btnClose = new JButton();
	JButton btnTest = new JButton();

	// --- Die Liste mit allen Fibus
	JList<String> jListFibu;
	// --- Button für die Liste Steuerung
	JButton btnUp = new JButton();
	JButton btnDown = new JButton();
	private final String upString = "moveUp";
	private final String downString = "moveDown";

	// --- Textfields
	JTextField tfFibuTitel = new JTextField();
	JTextField tfDatumVon = new JTextField();
	JTextField tfDatumBis = new JTextField();

	// --- Menu-einträge die allg. gesteuert werden
	JMenuItem mnuBilanz = new JMenuItem();
	JMenuItem mnuAbschluss = new JMenuItem();
	JMenuItem mnuNeu = new JMenuItem();
	JMenuItem mnuBestehend = new JMenuItem();
	JMenuItem mnuConfig = new JMenuItem();
	JMenuItem mnuFibu2 = new JMenuItem();
	JMenuItem mnuDelete = new JMenuItem();
	JMenuItem mnuBeenden = new JMenuItem();
	JMenuItem mnuJournal = new JMenuItem();
	JMenuItem mnuKontoListDrucken = new JMenuItem();

//	private static Logger logger = Logger.getLogger(FibuView.class.getName());

	/**
	 * Konstruktor
	 */
	public FibuView(String version) {
		super(version);
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Intialisierung aller Komonenten */
	private void init() {
		Trace.println(2, "FibuView.init()");
		initProperties();
		initView();
		initData();
		initMenu();
		// --- Grössen setzen abhängig von Font-Grösse
		double multiplikator = (double) Config.windowTextSize / (double) 12;
		int breite = (int) (FENSTER_BREITE * multiplikator);
		int hoehe = (int) (FENSTER_HOEHE * multiplikator);
		setSize(new Dimension(breite, hoehe));
	}

	/**
	 * Liest die Properties ein. Zeigt an, wenn ein property nicht gelesen werden
	 * kann
	 */
	private void initProperties() {
		Trace.println(3, "FibuView.initProperties()");
		// Config.readProperties();
		Locale newLocal = new Locale(Config.languageLanguage, Config.languageCountry);
		Locale.setDefault(newLocal);
		Locale loc = Locale.getDefault();
		Trace.println(3, "Locale County:" + loc.getCountry() + " Language:" + loc.getLanguage());
		Config.initFont();
	}

	/** Intialisierung aller View-Komonenten */
	private void initView() {
		Trace.println(3, "FibuView.initView()");
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(initButtons(), BorderLayout.LINE_END);
		getContentPane().add(initFibuList(), BorderLayout.CENTER);
		getContentPane().add(initEnterFields(), BorderLayout.PAGE_END);
	}

	/** Intialisierung der Buttons */
	private Container initButtons() {
		JPanel lPanel = new JPanel(new GridLayout(6, 1, 1, 5));
		lPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		// JPanel lPanel = new JPanel();
		btnOpen.setFont(Config.fontTextBold);
		btnOpen.setText("öffnen");
		btnOpen.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openFibuButtonAction(e);
			}
		});
		btnKontoplan.setEnabled(false);
		btnKontoplan.setFont(Config.fontTextBold);
		btnKontoplan.setText("Kontoplan");
		btnKontoplan.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openKontoplanAction(e);
			}
		});
		btnKontoblatt.setEnabled(false);
		btnKontoblatt.setFont(Config.fontTextBold);
		btnKontoblatt.setText("Kontoblatt");
		btnKontoblatt.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openKontoblattAction(e);
			}
		});
		btnBuchung.setEnabled(false);
		btnBuchung.setFont(Config.fontTextBold);
		btnBuchung.setText("Buchungen");
		btnBuchung.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openBuchungAction(e);
			}
		});
		btnClose.setEnabled(false);
		btnClose.setFont(Config.fontTextBold);
		btnClose.setToolTipText("");
		btnClose.setText("Schliessen");
		btnClose.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeFibuAction(e);
			}
		});
		btnTest.setEnabled(false);
		btnTest.setFont(Config.fontTextBold);
		btnTest.setToolTipText("");
		btnTest.setText("Test");
		btnTest.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				testAction(e);
			}
		});

		lPanel.add(btnOpen, null);
		lPanel.add(btnClose, null);
		lPanel.add(btnBuchung, null);
		lPanel.add(btnKontoblatt, null);
		lPanel.add(btnKontoplan, null);
		lPanel.add(btnTest, null);
		return lPanel;
	}

	/** Intialisierung der Fibu-Liste */
	private Container initFibuList() {
		Box box = Box.createVerticalBox();
		JLabel label = new JLabel("Liste der Fibu's");
		label.setAlignmentY(Component.LEFT_ALIGNMENT);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		label.setFont(Config.fontTextBold);
		box.add(label);
		// box.add(Box.createVerticalStrut(5));
		jListFibu = new JList<>();
		jListFibu.setFont(Config.fontTextBold);
		jListFibu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jListFibu.setSelectedIndex(0);
		jListFibu.setVisibleRowCount(10);
		jListFibu.setToolTipText("Mit Doppelklick eine Fibu auswählen");
		JScrollPane scrollPane = new JScrollPane(jListFibu);
		int breite = (int) (FIBU_LIST_BREITE * Config.windowTextMultiplikator);
		int hoehe = (int) (FIBU_LIST_HOEHE * Config.windowTextMultiplikator);
		this.setSize(new Dimension(breite, hoehe));
		scrollPane.setPreferredSize(new Dimension(breite, hoehe));
		box.add(scrollPane);

		final JPanel upPanel = new JPanel();
		upPanel.setLayout(new FlowLayout());

		btnUp.setFont(Config.fontTextBold);
		btnUp.setText("up");
		btnUp.setActionCommand(upString);
		btnUp.addActionListener(new UpDownListener());
		upPanel.add(btnUp);

		btnDown.setFont(Config.fontTextBold);
		btnDown.setText("down");
		btnDown.setActionCommand(downString);
		btnDown.addActionListener(new UpDownListener());
		upPanel.add(btnDown);
		box.add(upPanel);

		JPanel lPanel = new JPanel();
		lPanel.add(box, BorderLayout.CENTER);

		// jListFibu.addListSelectionListener(this);
		// wenn Doppelklick
		jListFibu.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				openFibuMouseAction(e);
			}
		});
		return lPanel;
	}

	/** Intialisierung der Eingabe-Felder */
	private Container initEnterFields() {
		tfFibuTitel.setText("Name der Fibu");
		tfFibuTitel.setFont(Config.fontText);
		// --- Datum von
		tfDatumVon.setText("");
		tfDatumVon.setFont(Config.fontText);
		tfDatumVon.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				tfDatumVon_actionPerformed(e);
			}
		});
		// --- Datum bis
		tfDatumBis.setText("");
		tfDatumBis.setFont(Config.fontText);
		tfDatumBis.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				tfDatumBis_actionPerformed(e);
			}
		});
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				this_windowClosing(e);
			}
		});
		// --- Create and populate the panel.
		String[] lLabels = { "Titel:", "von:", "bis:" };
		int numPairs = lLabels.length;
		JPanel lPanel = new JPanel(new SpringLayout());
		JLabel lLabel = new JLabel(lLabels[0], SwingConstants.TRAILING);
		lPanel.add(lLabel);
		lLabel.setFont(Config.fontTextBold);
		lLabel.setLabelFor(tfFibuTitel);
		lPanel.add(tfFibuTitel);
		lLabel = new JLabel(lLabels[1], SwingConstants.TRAILING);
		lPanel.add(lLabel);
		lLabel.setFont(Config.fontTextBold);
		lLabel.setLabelFor(tfDatumVon);
		lPanel.add(tfDatumVon);
		lLabel = new JLabel(lLabels[2], SwingConstants.TRAILING);
		lPanel.add(lLabel);
		lLabel.setFont(Config.fontTextBold);
		lLabel.setLabelFor(tfDatumBis);
		lPanel.add(tfDatumBis);
		// --- Lay out the panel (from SpringUtilities)
		makeCompactGrid(lPanel, numPairs, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		return lPanel;
	}

	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu mnuDatei = new JMenu();
		JMenu mnuAuswertung = new JMenu();
		mnuDatei.setFont(Config.fontTextBold);
		mnuDatei.setText("Datei");
		mnuAuswertung.setFont(Config.fontTextBold);
		mnuAuswertung.setText("Auswertung");
		mnuBilanz.setEnabled(false);
		mnuBilanz.setFont(Config.fontTextBold);
		mnuBilanz.setText("Bilanzen ...");
		mnuBilanz.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openBilanzAction(e);
			}
		});
		mnuAbschluss.setEnabled(false);
		mnuAbschluss.setFont(Config.fontTextBold);
		mnuAbschluss.setText("Abschluss");
		mnuAbschluss.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mnuAbschlussAction(e);
			}
		});

		mnuNeu.setFont(Config.fontTextBold);
		mnuNeu.setText("Neue Fibu anlegen");
		mnuNeu.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mnuNeuAction(e);
			}
		});

		mnuBestehend.setFont(Config.fontTextBold);
		mnuBestehend.setText("Bestehende Fibu dazufügen");
		mnuBestehend.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mnuBestehendAction(e);
			}
		});

		mnuFibu2.setFont(Config.fontTextBold);
		mnuFibu2.setEnabled(false);
		mnuFibu2.setText("Daten von alter Fibu kopieren");
		mnuFibu2.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mnuOtherFibuAction(e);
			}
		});

		mnuDelete.setFont(Config.fontTextBold);
		mnuDelete.setText("Fibu löschen");
		mnuDelete.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mnuDeleteAction(e);
			}
		});

		mnuBeenden.setFont(Config.fontTextBold);
		mnuBeenden.setText("Program beenden");
		mnuBeenden.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				beendenAction(e);
			}
		});
		mnuJournal.setFont(Config.fontTextBold);
		mnuJournal.setEnabled(false);
		mnuJournal.setText("Journal drucken");
		mnuJournal.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mnuJournalAction(e);
			}
		});
		mnuKontoListDrucken.setFont(Config.fontTextBold);
		mnuKontoListDrucken.setEnabled(false);
		mnuKontoListDrucken.setText("Konto Liste drucken");
		mnuKontoListDrucken.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mnuKontoListAction(e);
			}
		});
		// setup menu hierarchie
		menuBar.add(mnuDatei);
		mnuDatei.add(mnuNeu);
		mnuDatei.add(mnuBestehend);
		mnuDatei.add(mnuFibu2);
		mnuDatei.add(mnuDelete);
		mnuDatei.add(mnuBeenden);
		menuBar.add(mnuAuswertung);
		mnuAuswertung.add(mnuBilanz);
		mnuAuswertung.addSeparator();
		mnuAuswertung.add(mnuAbschluss);
		mnuAuswertung.add(mnuJournal);
		mnuAuswertung.add(mnuKontoListDrucken);
		setJMenuBar(menuBar);
	}

	/** Initialisert die Datenwerte der Fibu: Config, */
	private void initData() {
		Trace.println(3, "FibuView.initData()");
		// Model für die Liste der Fibu
		jListFibu.setModel(Config.getFibuList());
	}

	void openBuchungAction(ActionEvent e) {
		if (mBuchung == null) {
			mBuchung = new BuchungView();
		}
		mBuchung.setVisible(true);
	}

	void openKontoblattAction(ActionEvent e) {
		if (mKontoView == null) {
			mKontoView = new KontoView();
		}
		mKontoView.setVisible(true);
	}

	void openKontoplanAction(ActionEvent e) {
		if (mKontoplan == null) {
			mKontoplan = new KontoplanView();
		}
		mKontoplan.setVisible(true);
	}

	void openBilanzAction(ActionEvent e) {
		if (mAuswertung == null) {
			mAuswertung = new BilanzenView();
		}
		mAuswertung.setVisible(true);
	}

	void closeFibuAction(ActionEvent e) {
		closeFibu();
	}

	void testAction(ActionEvent e) {
		CsvBank bank = new CsvBank();
		bank.setBankName("Cumulus");
		PdfSetupFrame setup = new PdfSetupFrame(bank);
		setup.leseBuchungsZeileTest();
	}

	/** überschriebene Methode */
	@Override
	public void hide() {
		disposeSubWindows();
		super.setVisible(false);
	}

	/**
	 * Alle Unterwinodows schliessen (dispose). Noch prüfen, ob etwas gespeichert
	 * werden soll
	 */
	private void disposeSubWindows() {
		Trace.println(1, "FibuView.disposeSubWindows()");
		if (mBuchung != null) {
			// mBuchung.hide();
			mBuchung.setVisible(false);
			mBuchung.dispose();
			mBuchung = null;
		}
		if (mCsvBuchung != null) {
			mCsvBuchung.dispose();
			mCsvBuchung = null;
		}
		if (mKontoView != null) {
			mKontoView.dispose();
			mKontoView = null;
		}
		if (mKontoplan != null) {
			mKontoplan.dispose();
			mKontoplan = null;
		}
		if (mAuswertung != null) {
			mAuswertung.dispose();
			mAuswertung = null;
		}
	}

	/** öffnen einer Fibu mit den öffnen-button */
	void openFibuButtonAction(ActionEvent e) {
		Trace.println(0, "FibuView.btnOpen()");
		if (jListFibu.getModel().getSize() > 0) {
			// prüfen, ob andere Buchhaltung offen
			if (DbConnection.isFibuOpen()) {
				JOptionPane.showMessageDialog(null, "geöffente Fibu zuerst schliessen", "Fibu öffnen",
						JOptionPane.ERROR_MESSAGE);
			} else {
				// den Name der Fibu setzen
				String dbName = jListFibu.getSelectedValue();
				if (dbName == null || dbName.length() < 1) {
					JOptionPane.showMessageDialog(null, "Fibu wählen", "Fibu öffnen", JOptionPane.ERROR_MESSAGE);
				} else {
					openFibu(dbName);
				}
			}
		}
		else {
			// wenn nichts in der Liste
			btnOpen.setEnabled(false);
		}
	}

	/**
	 * Wenn in der Liste der Buchhaltungen ein Doppleklick festgestellt wird, diese
	 * Buchhaltung öffen
	 */
	private void openFibuMouseAction(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (jListFibu.getModel().getSize() > 0) {
				int index = jListFibu.locationToIndex(e.getPoint());
				String dbName = jListFibu.getModel().getElementAt(index);
				openFibu(dbName);
			}
		}
	}

	/**
	 * öffnet die Fibu.
	 *
	 * @param dbName der Name der Fibu.
	 */
	private void openFibu(String dbName) {
		try {
			FibuDataBase.openFibu(dbName);
			// wenn ok: Buttons aktivieren
			enableButtons(true);
			setFibuDaten(dbName);
			checkPendingBookings();
		} catch (FibuException ex) {
			showMessage("Probleme beim Fibu öffnen", ex);
		} catch (FibuRuntimeException ex) {
			showMessage("Probleme beim Fibu öffnen", ex);
		}
	}

	/** Felder der Fibu in der View setzen */
	private void setFibuDaten(String dbName) {
		Config.sFibuDbName = dbName;
		tfFibuTitel.setText(Config.sFibuTitel);
		tfDatumVon.setText(Config.sDatumVon.toString());
		tfDatumBis.setText(Config.sDatumBis.toString());
		FibuData.getFibuData().setFibuName(dbName);
	}

	/**
	 * Ist das file von csv noch vorhanden, wenn ja, Frame öffnen
	 */
	private void checkPendingBookings() {
		Trace.println(2, "FibuView.checkPendingBookings()");
		if (JsonFile.exist()) {
			Trace.println(3, "File gefunden");
			mCsvBuchung = new CsvReaderBuchungFrame("");
			mCsvBuchung.setVisible(true);
		} else {
			Trace.println(3, "kein File gefunden");
		}
	}

	/**
	 * Fibu schliessen, zuerst Stammdaten speichern
	 *
	 * @todo: prüfen, ob noch etwas zu speichern ist (Buchung)
	 */
	private void closeFibu() {
		Trace.println(2, "FibuView.closeFibu()");
		try {
			if (DbConnection.isConnected()) {
				writeFibuData();
				// Datenzugriff entfernen
				DbConnection.close();
			} else {
				JOptionPane.showMessageDialog(null, "Kann nicht speichern, da kein Zugriff auf die Datenbank (mehr).",
						"Fibu schliessen", JOptionPane.ERROR_MESSAGE);
			}
			DataBeanContext.removeAll();
			// Buttons setzen
			enableButtons(false);
			disposeSubWindows();
			tfDatumVon.setText("");
			tfDatumBis.setText("");
			tfFibuTitel.setText("");
		} catch (FibuException ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Fibu schliessen", JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Aktiviert alle Buttons */
	private void enableButtons(boolean enable) {
		Trace.println(4, "FibuView.enableButtons()");
		btnBuchung.setEnabled(enable);
		btnOpen.setEnabled(enable);
		btnKontoblatt.setEnabled(enable);
		btnKontoplan.setEnabled(enable);
		btnClose.setEnabled(enable);
		btnTest.setEnabled(enable);
		mnuBilanz.setEnabled(enable);
		mnuAbschluss.setEnabled(enable);
		mnuJournal.setEnabled(enable);
		mnuKontoListDrucken.setEnabled(enable);
		mnuFibu2.setEnabled(enable);
		// Open disable
		btnOpen.setEnabled(!enable);
		jListFibu.setEnabled(!enable);
	}

	/** Daten in die DB speichern */
	private void writeFibuData() throws FibuException {
		Trace.println(4, "FibuView.writeFibuData()");
		Config.sFibuTitel = tfFibuTitel.getText();
		FibuDataBase.writeFibuData();
		// die Positionen der Windows sichern
		if (mBuchung != null) {
			Config.winBuchungDim = mBuchung.getSize();
			Config.winBuchungLoc = mBuchung.getLocation();
		}
		if (mKontoView != null) {
			Config.winKontoblattDim = mKontoView.getSize();
			Config.winKontoblattLoc = mKontoView.getLocation();
		}
		if (mKontoplan != null) {
			Config.winKontoplanDim = mKontoplan.getSize();
			Config.winKontoplanLoc = mKontoplan.getLocation();
		}
		Config.saveProperties();
	}

	/** Das DatumVon im Config ändern */
	private void tfDatumVon_actionPerformed(FocusEvent e) {
		Trace.println(4, "FibuView.tfDatumVon_actionPerformed()");
		// kann nicht Datum.setDatum() verwenden, da dort Range überprüft
		DatumFormat df = DatumFormat.getDatumInstance();
		try {
			tfDatumVon.setText(df.parseDatum(tfDatumVon.getText()));
			Config.sDatumVon.setNewDatum(tfDatumVon.getText());
		} catch (ParseException ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Datum setzen", JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Das DatumBis im Config ändern */
	private void tfDatumBis_actionPerformed(FocusEvent e) {
		Trace.println(4, "FibuView.tfDatumBis_actionPerformed()");
		// kann nicht Datum.setDatum() verwenden, da dort Range überprüft
		DatumFormat df = DatumFormat.getDatumInstance();
		try {
			tfDatumBis.setText(df.parseDatum(tfDatumBis.getText()));
			Config.sDatumBis.setNewDatum(tfDatumBis.getText());
		} catch (ParseException ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Datum setzen", JOptionPane.ERROR_MESSAGE);
		}

	}

	/** Journal drucken */
	private void mnuJournalAction(ActionEvent e) {
		JournalPrintDialog journalPrinter = new JournalPrintDialog(this, "Journal drucken", true);
		journalPrinter.setVisible(true);
	}

	/** KontoListe drucken */
	private void mnuKontoListAction(ActionEvent e) {
		KontoListPrintDialog ktoPrinter = new KontoListPrintDialog(this, "KontoListe drucken", true);
		ktoPrinter.setVisible(true);
	}

	/**
	 * Neue Fibu anlegen Fenster öffnen, Name erfragen. Fibu anlegen, wenn noch
	 * nicht existiert
	 */
	private void mnuNeuAction(ActionEvent e) {
		String fibuName = JOptionPane.showInputDialog("Name der neuen Fibu: ");
		try {
			FibuDataBase.newFibu(fibuName);
			JOptionPane.showMessageDialog(null, "Fibu " + fibuName + " angelegt!", "Neue Fibu",
					JOptionPane.INFORMATION_MESSAGE);

		} catch (FibuException ex) {
			showMessage(ex);
		}
	}

	/**
	 * Neue Fibu anlegen Fenster öffnen, Name erfragen. Fibu anlegen, wenn noch
	 * nicht existiert
	 */
	private void mnuBestehendAction(ActionEvent e) {
//		String fibuName = JOptionPane.showInputDialog("Name der neuen Fibu in DB: ");
		JOptionPane.showMessageDialog(null, "Noch nicht impl. => Eintrag in Config anpassen",
				"Bestehende Fibu dazufügen", JOptionPane.INFORMATION_MESSAGE);
//		try {
//			FibuDataBase.openFibu(fibuName);
//		} catch (Exception ex) {
//			showMessage(ex);
//		}
	}

	/**
	 * Abschluss einer Fibu, Name erfragen. Fibu anlegen, wenn noch nicht existiert
	 */
	private void mnuAbschlussAction(ActionEvent e) {
		JOptionPane.showMessageDialog(this, "Noch nicht implementiert");
	}

	/**
	 * Daten von anderer Fibu kopieren, startet Frame mit Listen der Fibus und
	 * Buttons
	 */
	private void mnuOtherFibuAction(ActionEvent e) {
		if (mOtherFibu == null) {
			mOtherFibu = new FibuCopyFrom();
		}
		mOtherFibu.setVisible(true);
		// wenn das kopieren Fenster geschlossen wird, dann Name der Fibu anzeigen
		mOtherFibu.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				tfFibuTitel.setText(Config.sFibuTitel);
			}
		});
		this.repaint();
	}

	/**
	 * Bestehende Fibu löschen.
	 */
	private void mnuDeleteAction(ActionEvent e) {
		// prüfen, ob Buchhaltung offen
		if (DbConnection.isFibuOpen()) {
			JOptionPane.showMessageDialog(null, "geöffente Fibu zuerst schliessen", "Fibu löschen",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// den Name der Fibu setzen
		String dbName = jListFibu.getSelectedValue();
		if (dbName == null) {
			JOptionPane.showMessageDialog(null, "Keine Fibu selektiert", "Fibu löschen", JOptionPane.ERROR_MESSAGE);
			return;
		}
		// prüfen, ob Buchungen vorhanden
		try {
			DbConnection.open(dbName);
			BuchungData buchungen = (BuchungData) DataBeanContext.getContext().getDataObject(BuchungData.class);
			if (buchungen.getRowCountTable() > 0) {
				int answer = JOptionPane.showConfirmDialog(null,
						"Fibu: " + dbName + " Buchungen vorhanden, trotzdem löschen?");
				if (answer != 0) {
					return;
				}
			}
			// --- DB löschen
			int answer = JOptionPane.showConfirmDialog(null, "Fibu: " + dbName + " löschen?");
			if (answer == 0) {
				try {
					FibuDataBase.deleteFibu(dbName);
				} catch (FibuException ex) {
					showMessage(ex);
				}
			}
		} catch (Exception ex) {
			showError(ex);
		}
	}

	/**
	 * Eine Exception anzeigen
	 * 
	 * @param ex
	 */
	private void showMessage(Exception ex) {
		JOptionPane.showMessageDialog(null, ex.getMessage(), "Fibu", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Eine Exception mit tiel anzeigen anzeigen
	 * 
	 * @param ex
	 */
	private void showMessage(String titel, Exception ex) {
		JOptionPane.showMessageDialog(null, ex.getMessage(), titel, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Beenden, Buchhaltung muss geschlossen sein, Exit, damit das DOS-Fenster
	 * verschwindet
	 */
	private void beendenAction(ActionEvent e) {
		doExit();
	}

	/** wenn das Fenster geschlossen wird */
	private void this_windowClosing(WindowEvent e) {
		doExit();
	}

	/** Fibu beenden, vorher noch prüfen, ob eine Fibu offen ist */
	private void doExit() {
		if (DbConnection.isFibuOpen()) {
			closeFibu();
		}
		try {
			Config.saveProperties();
			System.exit(0);
		} catch (FibuException ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Fibu schliessen", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Fehler anzeigen
	 *
	 * @param ex die Exception
	 */
	public void showError(Exception ex) {
		StringBuffer sb = new StringBuffer(100);
		sb.append("Message: ");
		sb.append(ex.getMessage());
		JOptionPane.showMessageDialog(null, sb.toString(), "Fehler beim lesen von Config", JOptionPane.ERROR_MESSAGE);
	}

	// --- Von Sun.SpringUtilities kopiert ------------------------------
	/** Used by makeCompactGrid. */
	private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent(row * cols + col);
		return layout.getConstraints(c);
	}

	/**
	 * Aligns the first <code>rows</code> * <code>cols</code> components of
	 * <code>parent</code> in a grid. Each component in a column is as wide as the
	 * maximum preferred width of the components in that column; height is similarly
	 * determined for each row. The parent is made just big enough to fit them all.
	 *
	 * @param rows     number of rows
	 * @param cols     number of columns
	 * @param initialX x location to start the grid at
	 * @param initialY y location to start the grid at
	 * @param xPad     x padding between cells
	 * @param yPad     y padding between cells
	 */
	public static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad,
			int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			Trace.println(1, "error: The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}

		// Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		// Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		// Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}

	// Listen for clicks on the up and down arrow buttons.
	class UpDownListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// This method can be called only when
			// there's a valid selection,
			// so go ahead and move the list item.
			int moveMe = jListFibu.getSelectedIndex();

			if (e.getActionCommand().equals(upString)) {
				// UP ARROW BUTTON
				if (moveMe > 0) {
					// not already at top
					swap(moveMe, moveMe - 1);
					jListFibu.setSelectedIndex(moveMe - 1);
					jListFibu.ensureIndexIsVisible(moveMe - 1);
				}
			} else {
				// DOWN ARROW BUTTON
				if (moveMe < 0) {
					return;
				}
				if (moveMe != Config.getFibuList().getSize() - 1) {
					// not already at bottom
					swap(moveMe, moveMe + 1);
					jListFibu.setSelectedIndex(moveMe + 1);
					jListFibu.ensureIndexIsVisible(moveMe + 1);
				}
			}
		}
	}

	// Swap two elements in the list.
	private void swap(int a, int b) {
		String aObject = Config.getFibuList().getElementAt(a);
		String bObject = Config.getFibuList().getElementAt(b);
		Config.getFibuList().set(a, bObject);
		Config.getFibuList().set(b, aObject);
	}

}// endOfClass