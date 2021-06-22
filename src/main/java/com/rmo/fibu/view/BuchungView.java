package com.rmo.fibu.view;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import com.rmo.fibu.exception.BuchungValueException;
import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.CsvCompany;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Datum;
import com.rmo.fibu.util.Trace;
import com.rmo.fibu.view.util.JFormattedTextFieldExt;
import com.rmo.fibu.view.util.JTextFiledExt;

/** Die View aller Buchungen. UseCases: Buchungen eingeben, suchen, bearbeiten, löschen.
 *  Die View besteht aus einem Anzeige-Panel und einen Eingabe-Panel.
 * In Anzeige-Panel wird auch die KontoListe angezeigt, wenn der 
 * Fokus im Soll- oder HabenKonto ist.
 * Im Eingabe-Panel werden die Eingabe-Felder mit den Steuerbuttons angezeigt.
 * Diese Klasse verwendet:<br>
 * - BuchungListFrame: Anzeige aller Buchungen<br>
 * - KontoListFrame: Alle Konto für die Eingabe der Kontonummer<br>
 * - BuchungSuchenDialog: Eingabe der Suchargumente<br>  
 * <br>Status:<br>
 *  - Eingabe (mind. ein Feld ausgefällt): OK aktiv, Andern inaktiv<br>
 *  - ein Tupel ist im Temp-Speicher: Speicher aktiv<br>
 *  - ein Tupel gespeichern: refresh aktiv.<br>
 *
 * @author: R. Moser 
 */
public class BuchungView extends JFrame  {
	private static final long serialVersionUID = -4904918454266009794L;
	/** Tabelle für die Anzeige der Buchungen, enthält alle Buchungen */
	private BuchungListFrame	mBuchungListe;
	/** KontoListe für die Anzeige aller Konti */
	private KontoListFrame		mKontoListe;
	/** Die view um Buchungen einzulesen */
	private CsvReaderKeywordFrame		mCsvFrame;
	/** Csv Setup, Einstellungen der Companys */
	private CsvSetupFrame				mCsvSetup;
	/** Das Model zu dieser View */
	private BuchungData     	mBuchungData = null;

	/** Das Menu und PopUp */
	private BuchungMenu			mBuchungMenu;
	// ---- die TextFelder für die Eingabe
	private JTextField      		mTfDatum;
	private JTextField      		mTfBeleg;
	private JTextField      		mTfText;
	private JTextField    			mTfSoll;
	private JTextField				mTfHaben;
	// Betrag: View und Model-Feld
//	private FibuDecimalField   		mTfBetrag;
//	private DecimalFormat   		mMoneyFormat; // Formats to format and parse numbers
	private JFormattedTextField		mTfBetrag;
	// Die ID der Buchung, die bearbeitet wird, ist -1 wenn neu.
	private long            		mId = -1;
	//----- die Buttons
	private JButton         mButtonOk;
	private JButton         mButtonSave;
	private JButton         mButtonCancel;
	/** Message-Feld für die Fehlerausgabe */
	private JLabel          mMessage;

	//----- Temporäre Buchung fuer die naechste Eingabe
	private Buchung			mTempBuchung = new Buchung();
	private boolean         mDatumSame = false;
	private boolean         mBelegSame = false;

	//----- Status der Eingabefelder
	/** Die neuen Buchungen sind gesichert */
	private boolean         mNewBookingsSaved = true;
	// noch nicht verwendet
	/** Wenn eine Buchung zur Bearbeitung ausgewählt wurde, 
	 * bis Speichern gedrückt */
	private boolean       	mChangeing = false;
	/** Zur Steuerung, damit die Selektion in der Kontoliste
	 * übernommen werden kann */
	private boolean			mHasKontoLostFocus = false;
	/** Das Feld, das zuletzt den Focus verloren hat,
	 * wird verwendet, wenn etwas in der KontoListe selektiert wurde */
	private JTextComponent	mLastField = null;
		
	/**
	 * BuchungView constructor comment.
	 * @param title String
	 */
	public BuchungView() {
		super("Buchung V2.2");
		init();
	}
	
	// ----- Zugriff auf Objekte der Buchung View ---------------------
	public BuchungListFrame getBuchungListe() {
		return mBuchungListe;
	}
	
	/**
	 * Das CsvReaderFrame löschen, damit beim nächsten mal wieder neu gelesen wird.
	 */
	public void resetCsvReaderFrame() {
		mCsvFrame = null;
	}
	
	
	/**
	 * Das CsvReaderFrame löschen, damit beim nächsten mal wieder neu gelesen wird.
	 */
	public void resetCsvSetupFrame() {
		mCsvSetup = null;
	}

	// ----- Initialisierung ------------------------------------------------
	/**
	 * Start der Initialisierung, muss von jedem Konstruktor aufgerufen werden.
	 */
	private void init() {
		Trace.println(1,"BuchungView.init()");
		mBuchungData = (BuchungData) DataBeanContext.getContext().getDataBean(BuchungData.class);
		
		mBuchungMenu = new BuchungMenu(this);
		initView();
		initKontoListView();
		
		// Buttons setzen
		enableButtons();
		mButtonOk.setEnabled(true);
		mButtonCancel.requestFocus(); // damit nicht Datum den Fokus erhält
		// die letzte Buchung in den Temporären Speicher
		mTempBuchung = mBuchungListe.getLastBuchung();
		// die ID bis zu dieser Buchungen gesichert wurden
		mBuchungData.setIdSaved();
	}
	
	/** Initialisierung der verschiedenen Views
	 */
	private void initView() {
		Trace.println(2,"BuchungView.initView()");
		getContentPane().add(initAnzeige(), BorderLayout.CENTER);
		getContentPane().add(initEingabe(), BorderLayout.PAGE_END);
		//Position der BuchungView von Config lesen
		setSize(Config.winBuchungDim);
		setLocation(Config.winBuchungLoc);
	}
	
	/** Initialisierung des Anzeige-Bereiches: Buchungen, Kontoliste
	 * in einem DesktopPane.
	 */
	private Container initAnzeige() {
		Trace.println(3,"BuchungView.initAnzeige()");
		JDesktopPane lPane = new JDesktopPane();
		// BuchungListe
		mBuchungListe = new BuchungListFrame(this);
		lPane.add(mBuchungListe);
		mBuchungListe.setVisible(true);
		try {
			mBuchungListe.setMaximum(true);
		}
		catch (PropertyVetoException e) {
			// do nothing
		}
		mBuchungListe.scrollToLastEntry();

		// KontoListe
		//mKontoListe = new KontoListFrame();
		initKontoListView();
		lPane.add(mKontoListe);
		// Kontoliste initialisieren, in den Hintergrund
		mKontoListe.setVisible(true);
		hideKontoListe();
		// Listener, wenn etwas selektiert wird in der KontoListe
		ListSelectionModel rowSM = mKontoListe.getTable().getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (mHasKontoLostFocus) {
					ListSelectionModel lsm = (ListSelectionModel)e.getSource();
					mLastField.setText(mKontoListe.getKontoNrAt(lsm.getAnchorSelectionIndex()));
				}
			}
		});
		
		return lPane;
	}

	/** Initialisierung des Eingabe-Bereiches.
	 * Eingabefeldery<br>
	 * Buttons<br>
	 * Status-Zeile<br>
	 */
	private Container initEingabe() {
		Trace.println(3,"BuchungView.initEingabe()");
		JPanel lPanel = new JPanel(new GridLayout(0,1));
		lPanel.add(initEnterFields());
		lPanel.add(initEnterButtons());
		lPanel.add(initMessage());

		initListenersDatum();
		initListenersBeleg();
		initListenersText();
		initListenersSoll();
		initListenersHaben();
		initListenersBetrag();

		return lPanel;
	}
	
	/** Initialisierung der Eingabefelder
	 */
	private Container initEnterFields() {
		Trace.println(3,"BuchungView.initEnterFields()");
		GridBagLayout lLayout = new GridBagLayout();
		GridBagConstraints lConstraints = new GridBagConstraints();
		lConstraints.weightx = 1.0;
		lConstraints.fill = GridBagConstraints.HORIZONTAL;
		JPanel lPanel = new JPanel(lLayout);
	
		JLabel labelDatum = new JLabel("Datum");
		labelDatum.setFont(Config.fontTextBold);
		lLayout.setConstraints(labelDatum, lConstraints);
		lPanel.add(labelDatum);
		JLabel labelBeleg = new JLabel("Beleg");
		labelBeleg.setFont(Config.fontTextBold);
		lLayout.setConstraints(labelBeleg, lConstraints);
		lPanel.add(labelBeleg);
		JLabel labelText = new JLabel("Text");
		labelText.setFont(Config.fontTextBold);
		lLayout.setConstraints(labelText, lConstraints);
		lPanel.add(labelText);
		JLabel labelSoll = new JLabel("Soll");
		labelSoll.setFont(Config.fontTextBold);
		lLayout.setConstraints(labelSoll, lConstraints);
		lPanel.add(labelSoll);
		lConstraints.gridwidth = GridBagConstraints.RELATIVE;
		JLabel labelHaben = new JLabel("Haben");
		labelHaben.setFont(Config.fontTextBold);
		lLayout.setConstraints(labelHaben, lConstraints);
		lPanel.add(labelHaben);
		lConstraints.gridwidth = GridBagConstraints.REMAINDER;
		JLabel labelBetrag = new JLabel("Betrag");
		labelBetrag.setFont(Config.fontTextBold);
		lLayout.setConstraints(labelBetrag, lConstraints);
		lPanel.add(labelBetrag);
	
		// ----- Eingabefelder: Buchen
		lConstraints.fill = GridBagConstraints.HORIZONTAL;
		lConstraints.gridwidth = 1;
		lConstraints.weightx = 10.0;
		//--- Datum mit parser
		mTfDatum = new JTextFiledExt();
		lLayout.setConstraints(mTfDatum, lConstraints);
		lPanel.add(mTfDatum);
	
		//--- Beleg ----------------------------------
		lConstraints.weightx = 10.0;
		mTfBeleg = new JTextFiledExt();
		lLayout.setConstraints(mTfBeleg, lConstraints);
		lPanel.add(mTfBeleg);
	
		//--- Text -----------------------------------
		lConstraints.weightx = 100.0;
		mTfText = new JTextFiledExt();
		lLayout.setConstraints(mTfText, lConstraints);
		lPanel.add(mTfText);

		//--- Soll ----------------------------
		lConstraints.weightx = 5.0;
		mTfSoll = new JTextFiledExt();
		mTfSoll.setColumns(4);
		lLayout.setConstraints(mTfSoll, lConstraints);
		lPanel.add(mTfSoll);
	
		//--- Haben ------------------------
		lConstraints.gridwidth = GridBagConstraints.RELATIVE;
		mTfHaben = new JTextFiledExt();
		mTfHaben.setColumns(4);	
		lLayout.setConstraints(mTfHaben, lConstraints);
		lPanel.add(mTfHaben);
	
		//--- Betrag ------------------------------------
		lConstraints.gridwidth = GridBagConstraints.REMAINDER;
		lConstraints.weightx = 20.0;
		
		mTfBetrag = new JFormattedTextFieldExt(NumberFormat.getNumberInstance());
		mTfBetrag.setText("");
		lLayout.setConstraints(mTfBetrag, lConstraints);
		lPanel.add(mTfBetrag);
		return lPanel;
	}
	
	// --- Listener ----------------------------------------------
	
	/** Listeners für das Eingabefeld Datum */
	private void initListenersDatum() {
		// wenn cursor in Feld und wenn verlassen wird, Eingabe prüfen
		mTfDatum.addFocusListener( new FocusListener() {
			public void focusGained(FocusEvent e) {
				focusGainedEnterField(mTfDatum, mTempBuchung.getDatumAsString());
				hideKontoListe();
				if (mTfDatum.getText().length() > 2) {
					mTfDatum.setCaretPosition(2);
				}
				//gehe zum letzten Eintrag in der Buchungsliste
				mBuchungListe.scrollToLastEntry();
			}
			public void focusLost(FocusEvent e) {
				datumFocusLost();
			}
		});
		// wenn Enter-Taste gedrückt
		mTfDatum.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mTfBeleg.requestFocus();
			}
		});
	}
	

	/** Listeners für das Eingabefeld Beleg */
	private void initListenersBeleg() {
		// wenn cursor in Feld und wenn verlassen wird, Eingabe prüfen
		mTfBeleg.addFocusListener( new FocusListener() {
			public void focusGained(FocusEvent e) {
				belegFocusGained();
			}
			public void focusLost(FocusEvent e) {
				belegFocusLost();
			}
		});
		// wenn Enter-Taste gedrückt
		mTfBeleg.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mTfText.requestFocus();
			}
		});
	}
	
	/** Listeners für das Eingabefeld Text */
	private void initListenersText() {
		mTfText.addFocusListener( new FocusListener() {
			public void focusGained(FocusEvent e) {
				focusGainedEnterField(mTfText, mTempBuchung.getBuchungText());
				hideKontoListe();
			}
			public void focusLost(FocusEvent e) {
				focusLostEnterField(mTfText);
				// nix
			}
		});
		
		// wenn Enter-Taste gedrückt
		mTfText.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mTfSoll.requestFocus();
			}
		});
	}
		
	/** Listeners für das Eingabefeld Soll */
	private void initListenersSoll() {
		mTfSoll.addFocusListener( new FocusListener() {
			public void focusGained(FocusEvent e) {
				if ( !e.isTemporary() ) {
					focusGainedEnterField(mTfSoll, mTempBuchung.getSollAsString());
					mKontoListe.moveToFront();
					mHasKontoLostFocus = false;
					try {
						mKontoListe.selectRow(mTfSoll.getText());
					}
					catch (KontoNotFoundException ex) {}
				}
			}
			public void focusLost(FocusEvent e) {
				if ( !e.isTemporary() ) {
					focusLostEnterField(mTfSoll);
					mHasKontoLostFocus = true;
				}
			}
		});
		// überwachung der Tastatur-Eingabe 
		mTfSoll.addKeyListener ( new KeyListener() {
			public void keyTyped (KeyEvent e) {
				kontoKeyTyped(e, mTfSoll, mTfHaben);
			}
			public void keyReleased (KeyEvent e) {
				// nix tun
			}
			public void keyPressed (KeyEvent e) {
				kontoKeyPressed(e);
			}
		});
		// Bewegungen der Mouse kontrollieren
		mTfSoll.addMouseListener(new KontoMouseAdapter(mTfSoll));
		// wenn Enter-Taste gedrückt
		mTfSoll.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mTfHaben.requestFocus();
			}
		});
	}
	
	/** Listeners für das Eingabefeld Haben */
	private void initListenersHaben() {
		mTfHaben.addFocusListener( new FocusListener() {
			public void focusGained(FocusEvent e) {
				if ( ! e.isTemporary() ) {
					if ( ! e.isTemporary() ) {
						mKontoListe.moveToFront();
						mHasKontoLostFocus = false;
						focusGainedEnterField(mTfHaben, mTempBuchung.getHabenAsString());
						try {
							mKontoListe.selectRow(mTfHaben.getText());
						}
						catch (KontoNotFoundException ex) { }
					}
				}
			}
			public void focusLost(FocusEvent e) {
				if ( ! e.isTemporary() ) {
					focusLostEnterField(mTfHaben);
					mHasKontoLostFocus = true;
				}
			}
		});
		// überwachung der Tastatur-Eingabe 
		mTfHaben.addKeyListener ( new KeyListener() {
			public void keyTyped (KeyEvent e) {
				kontoKeyTyped(e, mTfHaben, mTfBetrag);
			}
			public void keyReleased (KeyEvent e) {
				// nix tun
			}
			public void keyPressed (KeyEvent e) {
				kontoKeyPressed(e);
			}
		});
		// Bewegungen der Mouse kontrollieren
		mTfHaben.addMouseListener(new KontoMouseAdapter(mTfHaben));
		// wenn Enter-Taste gedrückt
		mTfHaben.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mTfBetrag.requestFocus();
			}
		});
	}
	
	/** Listeners für das Eingabefeld Betrag */
	private void initListenersBetrag() {
		mTfBetrag.addFocusListener( new FocusListener() {
			public void focusGained(FocusEvent e) {
				if ( ! e.isTemporary() ) {
					focusGainedEnterField(mTfBetrag, mTempBuchung.getBetragAsString());
					hideKontoListe();
					mTfBetrag.selectAll();
				}
			}
			public void focusLost(FocusEvent e) {
				if ( ! e.isTemporary() ) {
					focusLostEnterField(mTfBetrag);
				}
			}
		});		
		// wenn Enter-Taste gedrückt
		mTfBetrag.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mButtonOk.requestFocus();
			}
		});
	}
	
	/** Initialisierung der Buttons für die Eingabe, inkl. Listener.
	 */
	private Container initEnterButtons() {
		Trace.println(3,"BuchungView.initEnterButtons()");
		JPanel lPanel = new JPanel(new GridLayout(1,6,3,3));
		//--- OK Button, die Daten dazufügen
		mButtonOk = new JButton("OK");
		mButtonOk.setFont(Config.fontTextBold);
		lPanel.add(mButtonOk);
		mButtonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okActionPerformed();
			}
		});
		// der Button muss requestFocus haben (siehe FoucusListener)
		mButtonOk.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if ( e.getKeyChar() == KeyEvent.VK_ENTER ) {
					e.consume();
					okActionPerformed();
				}
			}
		});
		// ist nötig, damit der Ok-Button den Focus erhält, um Enter abzufangen ???
		mButtonOk.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				//mButtonOk.requestFocus();
			}
			public void focusLost(FocusEvent e) {
				// nothing
				mBuchungListe.scrollToLastEntry();
			}
		});
	
		//--- Save Button, die Daten in der DB speichern
		mButtonSave = new JButton("Speichern");
		mButtonSave.setFont(Config.fontTextBold);
		lPanel.add(mButtonSave);
		mButtonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveActionPerformed();
			}
		});
		
		//--- Cancel Button, die Eingabe löschen
		mButtonCancel = new JButton("Abbrechen");
		mButtonCancel.setFont(Config.fontTextBold);
		lPanel.add(mButtonCancel);
		mButtonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelActionPerformed(e);
			}
		});

		return lPanel;
	}

	/** Initialisierung des Message-Feldes
	 */
	private Container initMessage() {
		Trace.println(3,"BuchungView.initMessage()");
		mMessage = new JLabel("Status:");
		return mMessage;
	}
	
	/** Initialisiert die Anzeige der Konti */
	private Container initKontoListView() {
		if (mKontoListe == null) {
			mKontoListe = new KontoListFrame();
		}
		this.addComponentListener (new ComponentListener() {
			public void componentResized(ComponentEvent e) {
				changeKontoListPosition();
			}
			public void componentMoved(ComponentEvent e) {
					
			}
			public void componentHidden(ComponentEvent e) {
					
			}
			public void componentShown(ComponentEvent e) {
				changeKontoListPosition();
			}
		});
		return mKontoListe;
	}
	
	/** Grösse und Position der Kontoliste berechnen */
	private void changeKontoListPosition() {		
		mKontoListe.setSize(250, 400);
		mKontoListe.setLocation(100, 20);
	}


	/** Löscht den Inhalt der Eingabefelder */
	private void clearEingabe() {
		//--- Datum
		mTfDatum.setText("");
		mTfDatum.setBackground(Color.white);
		//--- Beleg
		mTfBeleg.setText("");
		mTfBeleg.setBackground(Color.white);
		//--- Text
		mTfText.setText("");
		mTfText.setBackground(Color.white);
		//--- Soll
		mTfSoll.setText("");
		mTfSoll.setBackground(Color.white);
		//--- Text
		mTfHaben.setText("");
		mTfHaben.setBackground(Color.white);
	
		//--- Betrag
		mTfBetrag.setText("");
		mTfBetrag.setBackground(Color.white);
		mId = -1;
	}
	
	/** Kopiert den Inhalt der Eingabefelder in den temporären Speicher */
	private void copyToTemp() {
		try {
			//--- Datum
			if (mTempBuchung.getDatum() != null &&
				mTempBuchung.getDatumAsString().equals(mTfDatum.getText()) )
				mDatumSame = true;
			else mDatumSame = false;
			mTempBuchung.setDatum(mTfDatum.getText());
			//--- Beleg
			if (mTempBuchung.getBeleg() != null &&
				mTempBuchung.getBeleg().equals(mTfBeleg.getText()) )
				mBelegSame = true;
			else mBelegSame = false;
			mTempBuchung.setBeleg(mTfBeleg.getText());
			//--- Text
			mTempBuchung.setBuchungText(mTfText.getText());
			//--- Soll
			mTempBuchung.setSoll(mTfSoll.getText());
			//--- Haben
			mTempBuchung.setHaben(mTfHaben.getText());
			//--- Betrag
			mTempBuchung.setBetrag(((Number)mTfBetrag.getValue()).doubleValue());
		}
		catch (ParseException e) {}
		catch (BuchungValueException e){}
		// nix machen
	}

	//----- Enter-Field Event Handling -------------------------
	
	/** FocusGained Behandlung für alle Standard-Enter Fields. 
	 * In ein leeres Feld wird der letzt Wert eingetragen.
	 * Wenn das Feld leer ist, nichts */
	private void focusGainedEnterField(JTextComponent field, String defaultText) {
		Trace.println(4, "BuchungView.focusGainedEnterField()");
		// nichts eintragen, wenn schon etwas drin.
		if (field.getText() == null || field.getText().length() > 0) return;
		field.setText(defaultText);
		field.selectAll();
	}

	/** FocusLost Behandlung für alle Standard-Enter Fields */
	private void focusLostEnterField(JTextComponent field) {
		Trace.println(4, "BuchungView.focusLostEnterField()");
		isTfEmpty(field, true);
		enableButtons();
		mLastField = field;
	}


	/** Parsed das Datum */
	private void datumFocusLost() {
		Trace.println(4, "BuchungView.datumFocusLost()");
		try {
			Datum datum = new Datum(mTfDatum.getText());
			mTfDatum.setText(datum.toString());
			mTfDatum.setBackground(Color.white);
			// Testen, ob es dem letzten Datum entspricht
			if (mTfDatum.getText().equals(mTempBuchung.getDatumAsString())) mDatumSame = true;
			else mDatumSame = false;
			isTfEmpty(mTfDatum, true);
			deleteMessage();
			enableButtons();
		}
		catch (ParseException pEx) {
			mTfDatum.setBackground(Color.yellow);
			mMessage.setText("Fehler: " + pEx.getMessage() );
		}
	}
	
	/** Werte der letzten Buchung, bzw. selektierten Buchung eintragen */
	private void belegFocusGained() {
		Trace.println(4, "BuchungView.belegFocusGained()");
		hideKontoListe();
		// nichts tun, wenn bereits etwas eingegeben
		if (mTfBeleg.getText() == null || mTfBeleg.getText().length() > 0) return;
		if (mDatumSame && mBelegSame) {
			mTfBeleg.setText(mTempBuchung.getBeleg());
		}
		else {
			mTfBeleg.setText(Config.addOne(mTempBuchung.getBeleg()));
		}
		enableButtons();
	}
	
	/** Beleg Eingaben prüfen, ob etwas eingegeben, Feld markieren */
	private void belegFocusLost() {
		Trace.println(4, "BuchungView.belegFocusLost()");
		if (mTfBeleg.getText() == null || mTfBeleg.getText().length() > 0) return;
		isTfEmpty(mTfBeleg, true);
		enableButtons();
	}

	/** In KonotFelder (TfSoll oder TfHaben) wurde eine Taste gedrückt
	 * nur Backspace */
	private void kontoKeyPressed(KeyEvent e) {
		Trace.println(5,"BuchungView.kontoKeyPressed (KeyCode:" +e.getKeyCode()+")");
		if (e.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
			// nur BackSpace erlaubt (nicht Pfeil links / rechts, Delete etc)
			e.consume();
		}	
	}	

	/** Konto-Felder prüfen, nur Zahlen und Backspace erlaubt.
	 * Die Eingabe zusammenstellen und an die Kontoliste übergeben.
	 * Wenn eine Erweiterung der Kontonummer zurückkommt, diese setzen */
	private void kontoKeyTyped(KeyEvent e, JTextField kontoField, JComponent nextField) {
		Trace.println(5, "BuchungView.kontoKeyTyped(" + e.getKeyChar() +" " +e.getKeyCode() +")");
		// BackSpace gedrückt?
		boolean lBackSpace = (e.getKeyChar() == KeyEvent.VK_BACK_SPACE); // '\b');
		boolean lEnter = (e.getKeyChar() == KeyEvent.VK_ENTER); // '\n');
		// nur BackSpace, Enter und Zahlen erlaubt, sonst konsumieren
		if 	( lBackSpace || lEnter ||
			  (e.getKeyChar() >= '0' && e.getKeyChar() <= '9') ) {
		}
		else {
			e.consume();
			return;
		}
		// Der Enter-Event
		if (lEnter) {
			nextField.requestFocus();
		} 
		// zukünftigen String zusammenstellen
		String lEingabe = kontoField.getText();
		if (lBackSpace) {
			if (lEingabe.length() <= 1) {
				lEingabe = "";
			}
			else {
				lEingabe = lEingabe.substring(0,lEingabe.length()-1);
			}
		}
		else {
			lEingabe += e.getKeyChar();
		}
		try {
			String lKontoNr = mKontoListe.selectRow(lEingabe);
			// wenn KontoNummer vervollständigt, dann diese setzen
			if (!lBackSpace && (lKontoNr.length() > lEingabe.length())) {
				e.consume();
				kontoField.setText(lKontoNr);
			}
		}
		catch (KontoNotFoundException ex) {
			e.consume();
		}
	}

	/** KontoListe in den Hintergrund */
	private void hideKontoListe() {
		mKontoListe.moveToBack();
		mHasKontoLostFocus = false;	
	}

	/** prüft alle Eingabefelder.
	 * @param mark true wenn die Felder markiert werden sollen, die leer sind
	 */
	private int hasEnterFieldsEmpty (boolean mark) {
		int nrEmpty = 0;
		if (isTfEmpty(mTfBeleg, mark) ) nrEmpty++;
		if (isTfEmpty(mTfText, mark) ) nrEmpty++;
		if (isTfEmpty(mTfSoll, mark) ) nrEmpty++;
		if (isTfEmpty(mTfHaben, mark) ) nrEmpty++;
		if (isTfEmpty(mTfBetrag, mark) ) nrEmpty++;
		return nrEmpty;
	}

	/** prüft, ob eine Buchung eingegeben wird.
	 * @return true, wenn mehr als 2 Felder ausgefällt sind
	 */ 
	private boolean enteringBooking() {
		return hasEnterFieldsEmpty(false) < 3;
	}
	
	/** prüft ob das Textfeld leer ist
	 * @param mark true wenn das Feld markiert werden soll
	 * @return true wenn leer ist. */
	private boolean isTfEmpty(JTextComponent textField, boolean mark) {
		//Document doc = textField.getDocument();
		if ((textField.getText() == null) || (textField.getText().length() < 1)) {
			if (mark) textField.setBackground(Color.yellow);
			return true;
		}
		textField.setBackground(Color.white);
		return false;
	}
	
	/** Setzt den Standard-String in die Message */
	private void deleteMessage() {
		mMessage.setText("Status:");
	}
	
	//----- Behandlung der Button-Events --------------------------------

	/** Enables / disables Buttons oder Menus: Ok, Save, Change, Delete.<br>	
	 */
	private void enableButtons() {
		Trace.println(5, "BuchungView.enableButtonsManipulate");
		/* OK: nur aktiv wenn alle Felder bis auf eines ausgefällt,
		 * (damit OK Button aktiv ist beim letzen Feld
		 * inaktiv wenn im Modus mChangeing */
		if (!mChangeing) {
			mButtonOk.setEnabled(hasEnterFieldsEmpty(false) <= 1);			
		}
		else {
			mButtonOk.setEnabled(false);			
		}
		// Save: aktiv wenn !mNewBookingsSaved oder wenn mChangeing
		if (!mNewBookingsSaved || mChangeing) {
			mButtonSave.setEnabled(true);			
		}
		else {
			mButtonSave.setEnabled(false);
		}
		// Cancel: immer aktiv
		mButtonCancel.setEnabled(true);
		//Buchung copy, delete: wenn !enteringBooking und !mChangeing
		if (!enteringBooking() && !mChangeing) {
			mBuchungMenu.setEnableCopy(true);
			mBuchungMenu.setEnableDelete(true);			
		}
		else {
			mBuchungMenu.setEnableCopy(false);
			mBuchungMenu.setEnableDelete(false);						
		}
		// Sort immer true falls es etwas zum sortieren gibt
//		mBuchungMenu.setEnableSort(mBuchungData.getRowCountNew() > 0 );		
		// TODO stimmt das: den Focus immer auf Save setzen, da sonst Eingabe-Felder den Focus erhalten
		//mButtonSave.requestFocus();
	}
			
	public void showPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			mBuchungMenu.getPopMenu().show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/** Ok-Button wurde gedrückt: Werte prüfen, in Buchung kopieren.
	 *  Wenn keine Fehler aufgetreten sind, wird true zurückgegeben, sonst
	 *  false */
	private boolean okActionPerformed () {
		Trace.println(3, "BuchungView.okActionPerformed()");
		hideKontoListe();
		try {
			// Die Buchung im Model speichern
			mBuchungData.add(copyToBuchung());
			int lastRowNr = mBuchungData.getRowCount()-1;
			mBuchungListe.fireRowsInserted(lastRowNr-1,lastRowNr);
			copyToTemp();
			//mBuchungListe.repaint();
			mNewBookingsSaved = false;
			clearEingabe();
			deleteMessage();
			enableButtons();
			//mBuchungListe.scrollToLastEntry();
			return true;
		}
		catch (BuchungValueException pEx) {
			mMessage.setText("Fehler: " + pEx.getMessage() );
			return false;
		}
		finally {
		    Trace.println(3, "BuchungView.okActionPerformed() ===> end");
		}
	}
	
	/** Save-Button wurde gedrückt.
	 *  Wenn die bearbeitete Buchung ID > 0, dann nur diese Buchung sichern
	 *  sonst alle neuen Buchungen sichern. */
	private boolean saveActionPerformed () {
		Trace.println(3, "SaveButton->actionPerformed()");
		hideKontoListe();
		try {
			if (mId < 0) {
				// die neuen Buchungen sichern
				mBuchungData.saveNew();
				mNewBookingsSaved = true;
				mBuchungData.setIdSaved();
				mBuchungListe.repaint();
			}
			else {
				// Buchung wurde vorher gelesen
				mBuchungData.save(copyToBuchung());
				// update buchungListe
				mBuchungData.reloadData();
				clearEingabe();
				mChangeing = false;
				mBuchungListe.repaint();
			}
			enableButtons();
			deleteMessage();
			// @todo damit nicht der Betrag den Focus erhält
			return true;
		}
		catch (FibuException pEx) {
				JOptionPane.showMessageDialog(this, pEx.getMessage(),
					"Fehler beim speichern", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	/** Cancel-Button wurde gedrückt.
	 *  Die Eingabe leeren, Buttons zurücksetzen */
	private void cancelActionPerformed (ActionEvent e) {
		hideKontoListe();
		clearEingabe();
		mChangeing = false;
		enableButtons();
		mBuchungListe.repaint();
	}
	
	/** Die überschriebene Methode hide, prüft zuerst ob noch gespeichert
	 *  werden muss */
	public void setVisible(boolean visible) {
		Trace.println(1, "BuchungView.setVisible(" + visible +")");
		if (visible) {
			// nur wenn das Fenster geöffnet wird.
			super.setVisible(visible);
			return;
		}
		if ( enteringBooking() ) {
			// Bestägigung einholen
			int answer = JOptionPane.showConfirmDialog(
				this, "Offene Eingabe noch übernehmen", "Buchungen",
				JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.YES_OPTION) {
				if (!okActionPerformed()) {
					return;
				}
			}
			else {
				cancelActionPerformed(null);
			}
		}
		if (! mNewBookingsSaved) {
			// Bestägigung einholen
			int answer = JOptionPane.showConfirmDialog(
				this, "Speichern vor verlassen", "Buchungen",
				JOptionPane.YES_NO_CANCEL_OPTION);
			if (answer == JOptionPane.CANCEL_OPTION) {
				return;
			}
			if (answer == JOptionPane.YES_OPTION) {
				if (! saveActionPerformed()) {
					return;
				}
				mNewBookingsSaved = true;
			}
			else {
				mBuchungData.deleteNew();
				mNewBookingsSaved = true;
			}
		}
		if (mCsvSetup != null) {
			mCsvSetup.setVisible(visible);
			mCsvSetup = null;
		}		
		if (mCsvFrame != null) {
			mCsvFrame.setVisible(visible);
			mCsvFrame = null;
		}
		super.setVisible(false);
	}
		
	/** Sortieren wurde gewählt: Sichern, Tabelle neu ordnen  */
	public void sortActionPerformed () {
		Trace.println(3, "RefreshButton->actionPerformed()");
		try {
			mBuchungData.saveNew();
			mBuchungListe.repaint();
			mBuchungData.deleteNew();
			enableButtons();
		}
		catch (FibuException pEx) {
			mMessage.setText("Fehler: " + pEx.getMessage() );
		}
	}
	
	/** Change-Button wurde gedrückt.
	 *  Die gewählte Zeile editieren (in die Eingabe kopieren)
	*/
	public void copyActionPerformed () {
		Trace.println(3, "BuchungView.copyActionPerformed()");
		
		// sollte nie vorkommen
		if ( enteringBooking() ) {
			JOptionPane.showMessageDialog(this, "Eingabe zuerst sichern oder abbrechen",
				"ändern", JOptionPane.ERROR_MESSAGE);
				return;
		}
		mButtonOk.setEnabled(false);
		clearEingabe();
		int [] lRowNrs = mBuchungListe.getSelectedRows();
		// vorerst nur erste selektierte Zeile bearbeiten
		if (lRowNrs.length > 0) {
			try {
				Buchung lBuchung = mBuchungData.read(getId(lRowNrs[0]));
				copyToGui(lBuchung);
				mChangeing = true;
				enableButtons();
			}
			catch (FibuException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(),
					"Fehler beim lesen", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/** Delete-Button wurde gedrückt.
	 *   Die gewählte Zeilen löschen, mit Confirm-Dialog.
	*/
	public void deleteActionPerformed () {
		Trace.println(3, "deleteButton->actionPerformed()");
		int [] lRowNrs = mBuchungListe.getSelectedRows();
		if (lRowNrs.length > 0) {
			// Message zusammenstellen
			StringBuffer lMsgBuffer = new StringBuffer();
			for (int i = 0; i < lRowNrs.length; i++) {
				lMsgBuffer.append(mBuchungListe.getValueAt(lRowNrs[i],1) + ": ");
				lMsgBuffer.append(mBuchungListe.getValueAt(lRowNrs[i],2) + ", ");
			}
			// Bestägigung einholen
			int answer = JOptionPane.showConfirmDialog(
				this, lMsgBuffer.toString(),
				"Buchungen mit BelegNr löschen", JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.YES_OPTION) {
				try {
					for (int i = 0; i < lRowNrs.length; i++) {
						mBuchungData.delete(getId(lRowNrs[i]));
					}
					//mBuchungTable.repaint();
					this.repaint();
				}
				catch (FibuException pEx) {
					JOptionPane.showMessageDialog(this, pEx.getMessage()
						, "Löschen", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		}
	}
	
	/**
	 * Einstellungen für CSV
	 */
	public void csvSetup() {
		if (mCsvSetup == null) {
			mCsvSetup = new CsvSetupFrame(this);
		}
		mCsvSetup.setVisible(true);
	}

	/**
	 * Aufruf der View für CSV einlesen, mit der companyId
	 * @param pCompany
	 */
	public void csvAction (CsvCompany company) {
		if (mCsvFrame == null) {
			mCsvFrame = new CsvReaderKeywordFrame(company, this);
		}
		if (mCsvFrame.init()) {
			mCsvFrame.setVisible(true);
		}
		else {
			mCsvFrame.setVisible(false);
		}
	}

	
	/** prüft die Eingabefelder und kopiert deren Inhalt in Buchung.
	 *  @return Buchung falls alle Felder richtige Werte enthalten, sonst null
	 */
	private Buchung copyToBuchung() throws BuchungValueException {
		Trace.println(3, "BuchungView.copyToBuchung()");
		// zuerst prüfen, ob ein Feld leer ist.
		if (hasEnterFieldsEmpty(true) > 0) {
			throw new BuchungValueException("Eingabe fehlt");
		}
		Buchung lBuchung = new Buchung();
		String lErrorFeld = "";     // Angabe welches Feld
		try {
			lErrorFeld = "Datum";
			lBuchung.setDatum(mTfDatum.getText());
			lErrorFeld = "Beleg";
			lBuchung.setBeleg(mTfBeleg.getText());
			lErrorFeld = "Buchungstext";
			lBuchung.setBuchungText(mTfText.getText());
			lErrorFeld = "Soll-Konto";
			lBuchung.setSoll( Integer.valueOf((String)mTfSoll.getText()).intValue() );
			lErrorFeld = "Haben-Konto";
			lBuchung.setHaben( Integer.valueOf((String)mTfHaben.getText()).intValue() );
			lErrorFeld = "Betrag";
			mTfBetrag.commitEdit();
			double betrag = ((Number)mTfBetrag.getValue()).doubleValue();
			if (betrag <= 0) {
				throw new Exception("Betrag muss Grösser 0 sein");
			}
			lBuchung.setBetrag(betrag);
			lBuchung.setID(mId);
		}
		catch (Exception pEx) {
			pEx.printStackTrace(Trace.getPrintWriter());
			throw new BuchungValueException("Fehler in " + lErrorFeld + ":"  + pEx.getMessage());
		}
		return lBuchung;
	}
	
	/** prüft die Eingabefelder und kopiert deren Inhalt in Buchung.
	 */
	private void copyToGui(Buchung pBuchung) {
		Trace.println(3, "BuchungView.copyToGui()");
		mTfDatum.setText(pBuchung.getDatumAsString());
		mTfBeleg.setText(pBuchung.getBeleg());
		mTfText.setText(pBuchung.getBuchungText());
		mTfSoll.setText(pBuchung.getSollAsString());
		mTfHaben.setText(pBuchung.getHabenAsString());
		mTfBetrag.setText(pBuchung.getBetragAsString());
		mId = pBuchung.getID();
	}
		
	/** Gibt die Id der Row zurück */
	private long getId(int lRowNr) {
		return ((Long)mBuchungListe.getValueAt(lRowNr,6)).longValue();
	}
		
	/****************************************
	* für den Test der View von Buchungen.
	 */
	public static void main(String[] args) {
			// Datumgrenzen setzen, wenn nix gesetzt
		try {
			Config.sDatumVon.setNewDatum("1.1.1990");
			Config.sDatumBis.setNewDatum("31.12.2050");
		}
		catch (ParseException ex) {
			Trace.println(1, "Config.readProperties(): " + ex.getMessage());
		}
		try {
			DbConnection.open("FibuLeer");
			BuchungView lBuchung = new BuchungView();
			lBuchung.setVisible(true);
		}
		catch (FibuRuntimeException ex) {}
	}

}//endOfClass
