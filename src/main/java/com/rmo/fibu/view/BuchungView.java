package com.rmo.fibu.view;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.rmo.fibu.exception.BuchungValueException;
import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.CsvCompany;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

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
public class BuchungView extends JFrame implements BuchungInterface {
	private static final long serialVersionUID = -4904918454266009794L;
	/** Tabelle für die Anzeige der Buchungen, enthält alle Buchungen */
	private BuchungListFrame	mBuchungListe;
	/** Die view um Buchungen einzulesen */
	private CsvReaderKeywordFrame		mCsvFrame;
	/** Csv Setup, Einstellungen der Companys */
	private CsvCompanyFrame				mCsvSetup;
	/** Das Model zu dieser View */
	private BuchungData     	mBuchungData = null;
	/** Die Eingabefelder für eine Buchung */
	private BuchungEingabe mEingabe = null;

	/** Das Menu und PopUp */
	private BuchungMenu			mBuchungMenu;

	// Die ID der Buchung, die bearbeitet wird, ist -1 wenn neu.
	private long            		mId = -1;
	//----- die Buttons
	private JButton         mButtonOk;
	private JButton         mButtonSave;
	private JButton         mButtonCancel;
	/** Message-Feld für die Fehlerausgabe */
	private JLabel          mMessage;

	//----- Temporäre Buchung fuer die naechste Eingabe
//	private Buchung			mTempBuchung = new Buchung();
	private boolean         mDatumSame = false;
	private boolean         mBelegSame = false;

	//----- Status der Eingabefelder
	/** Die neuen Buchungen sind gesichert */
	private boolean         mNewBookingsSaved = true;
	// noch nicht verwendet
	/** Wenn eine Buchung zur Bearbeitung ausgewählt wurde,
	 * bis Speichern gedrückt */
	private boolean       	mChangeing = false;

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

		// Buttons setzen
		enableButtons();
		mButtonOk.setEnabled(true);
		mButtonCancel.requestFocus(); // damit nicht Datum den Fokus erhält
		// die letzte Buchung in den Temporären Speicher
//		mTempBuchung = mBuchungListe.getLastBuchung();
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
		mEingabe = new BuchungEingabe(this);
		lPanel.add(mEingabe.initView());

		lPanel.add(initEnterButtons());
		lPanel.add(initMessage());

//		initListenersDatum();
//		initListenersBeleg();
//		initListenersText();
//		initListenersSoll();
//		initListenersHaben();
//		initListenersBetrag();

		return lPanel;
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
			@Override
			public void actionPerformed(ActionEvent e) {
				okActionPerformed();
			}
		});
		// der Button muss requestFocus haben (siehe FoucusListener)
		mButtonOk.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if ( e.getKeyChar() == KeyEvent.VK_ENTER ) {
					e.consume();
					okActionPerformed();
				}
			}
		});
		// ist nötig, damit der Ok-Button den Focus erhält, um Enter abzufangen ???
		mButtonOk.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				//mButtonOk.requestFocus();
			}
			@Override
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
			@Override
			public void actionPerformed(ActionEvent e) {
				saveActionPerformed();
			}
		});

		//--- Cancel Button, die Eingabe löschen
		mButtonCancel = new JButton("Abbrechen");
		mButtonCancel.setFont(Config.fontTextBold);
		lPanel.add(mButtonCancel);
		mButtonCancel.addActionListener(new ActionListener() {
			@Override
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

	/**
	 * Die letzte Buchung der Liste
	 */
	@Override
	public Buchung getLastBuchung() {
		return mBuchungListe.getLastBuchung();
	}



	/** prüft, ob eine Buchung eingegeben wird.
	 * @return true, wenn mehr als 2 Felder ausgefüllt sind
	 */
	private boolean enteringBooking() {
		return mEingabe.hasEnterFieldsEmpty(false) < 3;
	}


	/** Setzt den Standard-String in die Message */
	private void deleteMessage() {
		mMessage.setText("Status:");
	}

	@Override
	public void setMessage(String text) {
		mMessage.setText(text);
	}

	//----- Behandlung der Button-Events --------------------------------

	/** Enables / disables Buttons oder Menus: Ok, Save, Change, Delete.<br>
	 */
	@Override
	public void enableButtons() {
		Trace.println(5, "BuchungView.enableButtonsManipulate");
		/* OK: nur aktiv wenn alle Felder bis auf eines ausgefällt,
		 * (damit OK Button aktiv ist beim letzen Feld
		 * inaktiv wenn im Modus mChangeing */
		if (!mChangeing) {
			mButtonOk.setEnabled(mEingabe.hasEnterFieldsEmpty(false) <= 1);
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
		// RTODO
//		hideKontoListe();
		try {
			// Die Buchung im Model speichern
			mBuchungData.add(mEingabe.copyToBuchung());
			int lastRowNr = mBuchungData.getRowCount()-1;
			mBuchungListe.fireRowsInserted(lastRowNr-1,lastRowNr);
			// TODO copyToTemp
//			copyToTemp();
			//mBuchungListe.repaint();
			mNewBookingsSaved = false;
			mEingabe.clearEingabe();
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
		// TODO hideKontoListe evt. nicht nötig
//		mEingabe.hideKontoListe();
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
				mBuchungData.save(mEingabe.copyToBuchung());
				// update buchungListe
				mBuchungData.reloadData();
				mEingabe.clearEingabe();
				mChangeing = false;
				mBuchungListe.repaint();
			}
			enableButtons();
			deleteMessage();
			mBuchungListe.fireTableDataChanged();
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
		// TODO hideKontoListe
//		hideKontoListe();
		mEingabe.clearEingabe();
		mChangeing = false;
		enableButtons();
		mBuchungListe.repaint();
	}

	/** Die überschriebene Methode hide, prüft zuerst ob noch gespeichert
	 *  werden muss */
	@Override
	public void setVisible(boolean visible) {
		Trace.println(3, "BuchungView.setVisible(" + visible +")");
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
		mEingabe.clearEingabe();
		int [] lRowNrs = mBuchungListe.getSelectedRows();
		// vorerst nur erste selektierte Zeile bearbeiten
		if (lRowNrs.length > 0) {
			try {
				Buchung lBuchung = mBuchungData.read(getId(lRowNrs[0]));
				mId = lBuchung.getID();
				mEingabe.copyToGui(lBuchung);
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
			for (int lRowNr : lRowNrs) {
				lMsgBuffer.append(mBuchungListe.getValueAt(lRowNr,1) + ": ");
				lMsgBuffer.append(mBuchungListe.getValueAt(lRowNr,2) + ", ");
			}
			// Bestägigung einholen
			int answer = JOptionPane.showConfirmDialog(
				this, lMsgBuffer.toString(),
				"Buchungen mit BelegNr löschen", JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.YES_OPTION) {
				try {
					for (int lRowNr : lRowNrs) {
						mBuchungData.delete(getId(lRowNr));
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
			mCsvSetup = new CsvCompanyFrame(this);
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
