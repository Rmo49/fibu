package com.rmo.fibu.view;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.text.ParseException;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.CsvBank;
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
 * - BuchungenFrame: Anzeige aller Buchungen<br>
 * - KontoListFrame: Alle Konto für die Eingabe der Kontonummer<br>
 * - BuchungSuchenDialog: Eingabe der Suchargumente<br>
 * <br>Status:<br>
 *  - Eingabe (mind. ein Feld ausgefällt): OK aktiv, Andern inaktiv<br>
 *  - ein Tupel ist im Temp-Speicher: Speicher aktiv<br>
 *  - ein Tupel gespeichern: refresh aktiv.<br>
 *
 * @author: R. Moser
 */
public class BuchungView extends JFrame implements BuchungEingabeInterface {
	private static final long serialVersionUID = -4904918454266009794L;
	/** Tabelle für die Anzeige der Buchungen, enthält alle Buchungen */
	private BuchungenFrame				mBuchungenFrame;
	/** Die view um Buchungen einzulesen */
	private CsvReaderKeywordFrame		mCsvFrame;
	/** Csv Setup, Einstellungen der Banks */
	private CsvBankFrame				mCsvSetup;
	/** das Pane der Buchungen im Center */
	private JDesktopPane 				mPaneCenter = null;
	/** Das Model zu dieser View */
	private BuchungData     			mBuchungData = null;
	/** Die Eingabefelder für eine Buchung */
	private BuchungEingabe 				mEingabe = null;

	/** Das Menu und PopUp */
	private BuchungMenu					mBuchungMenu;

	// noch nicht verwendet
	/** Wenn eine Buchung zur Bearbeitung ausgewählt wurde,
	 * bis Speichern gedrückt */
//	private boolean       	mChangeing = false;

	/**
	 * BuchungView constructor comment.
	 * @param title String
	 */
	public BuchungView() {
		super("Buchung V2.3");
		init();
	}

	// ----- Zugriff auf Objekte der Buchung View ---------------------
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
//		enableButtons();
//		mButtonOk.setEnabled(true);
//		mButtonCancel.requestFocus(); // damit nicht Datum den Fokus erhält
		// die letzte Buchung in den Temporären Speicher
//		mTempBuchung = mBuchungenFrame.getLastBuchung();
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
		mPaneCenter = new JDesktopPane();
		// BuchungListe
		mBuchungenFrame = new BuchungenFrame(this);
		mPaneCenter.add(mBuchungenFrame);
		mBuchungenFrame.setVisible(true);
		try {
			mBuchungenFrame.setMaximum(true);
		}
		catch (PropertyVetoException e) {
			// do nothing
		}
		mBuchungenFrame.scrollToLastEntry();

		return mPaneCenter;
	}

	/**
	 * Das Panel mit der Anzeige der Buchungen.
	 * @return
	 */
	@Override
	public JDesktopPane getPaneCenter() {
		return mPaneCenter;
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

		return lPanel;
	}


	/**
	 * Die letzte Buchung der Liste
	 */
	@Override
	public Buchung getLastBuchung() {
		return mBuchungenFrame.getLastBuchung();
	}

	/**
	 * Die Liste der angezeigten Buchungen
	 * @return
	 */
	public BuchungenFrame getBuchungListe() {
		return mBuchungenFrame;
	}


	/**
	 * Verbindung zu der Datenbank
	 */
	@Override
	public BuchungData getBuchungData() {
		return mBuchungData;
	}

	/**
	 * Ans Ende der Liste scrollen
	 */
	@Override
	public void scrollToEnd() {
		mBuchungenFrame.scrollToLastEntry();
	}

	/**
	 * Die Menus setzen, je nach Zustand der Eingabe einer Buchung
	 * Wird von BuchungEingabe getriggert
	 */
	@Override
	public void setBuchungMenu(boolean enteringBooking, boolean mChangeing) {
		//Buchung copy, delete: wenn !enteringBooking und !mChangeing
		if (!mEingabe.enteringBooking() && !mChangeing) {
			mBuchungMenu.setEnableCopy(true);
			mBuchungMenu.setEnableDelete(true);
		}
		else {
			mBuchungMenu.setEnableCopy(false);
			mBuchungMenu.setEnableDelete(false);
		}
		// Sort immer true falls es etwas zum sortieren gibt
		mBuchungMenu.setEnableSort(mBuchungData.getRowCountNew() > 0 );
	}


	public void showPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			mBuchungMenu.getPopMenu().show(e.getComponent(), e.getX(), e.getY());
		}
	}


	/** Die überschriebene Methode hide, prüft zuerst ob noch gespeichert
	 *  werden muss */
	@Override
	public void setVisible(boolean isVisible) {
		Trace.println(3, "BuchungView.setVisible(" + isVisible +")");
		if (isVisible) {
			// nur wenn das Fenster geöffnet wird.
			super.setVisible(isVisible);
			return;
		}
		if ( mEingabe.enteringBooking() ) {
			// Bestägigung einholen
			int answer = JOptionPane.showConfirmDialog(
				this, "Offene Eingabe noch übernehmen", "Buchungen",
				JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.NO_OPTION) {
				mEingabe.clearEingabe();
				super.setVisible(isVisible);
			}
			else {
				// wenn noch übernehmen dann immer noch anzeigen
				return;
			}
		}
		if (! mEingabe.newBookingSaved()) {
			// Bestägigung einholen
			int answer = JOptionPane.showConfirmDialog(
				this, "Noch nicht gespeichert, trotzdem verlassen", "Buchungen",
				JOptionPane.YES_NO_CANCEL_OPTION);
			if (answer == JOptionPane.CANCEL_OPTION) {
				return;
			}
			if (answer == JOptionPane.YES_OPTION) {
				// temporäre Buchung löschen
				mBuchungData.deleteNewBookings();
				super.setVisible(isVisible);
			}
			else {
				// nicht verlassen, noch speichern
				return;
			}
		}
		if (mCsvSetup != null) {
			mCsvSetup.setVisible(isVisible);
			mCsvSetup = null;
		}
		if (mCsvFrame != null) {
			mCsvFrame.setVisible(isVisible);
			mCsvFrame = null;
		}
		super.setVisible(false);
	}

	/** Sortieren wurde gewählt: Sichern, Tabelle neu ordnen  */
	public void sortActionPerformed () {
		Trace.println(3, "RefreshButton->actionPerformed()");
		try {
			mBuchungData.saveNew();
			mBuchungenFrame.repaint();
			mBuchungData.deleteNewBookings();
//			enableButtons();
		}
		catch (FibuException pEx) {
		}
	}

	/** Change-Button wurde gedrückt: Eine Buchung ändern.
	 *  Die gewählte Zeile editieren (in die Eingabe kopieren)
	*/
	public void copyActionPerformed () {
		Trace.println(3, "BuchungView.copyActionPerformed()");

		// sollte nie vorkommen
		if ( mEingabe.enteringBooking() ) {
			JOptionPane.showMessageDialog(this, "Eingabe zuerst sichern oder abbrechen",
				"ändern", JOptionPane.ERROR_MESSAGE);
				return;
		}
		int [] lRowNrs = mBuchungenFrame.getSelectedRows();
		// vorerst nur erste selektierte Zeile bearbeiten
		if (lRowNrs.length > 0) {
			try {
				Buchung lBuchung = mBuchungData.read(getId(lRowNrs[0]));
				mEingabe.copyToFields(lBuchung);
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
		int [] lRowNrs = mBuchungenFrame.getSelectedRows();
		if (lRowNrs.length > 0) {
			// Message zusammenstellen
			StringBuffer lMsgBuffer = new StringBuffer();
			for (int lRowNr : lRowNrs) {
				lMsgBuffer.append(mBuchungenFrame.getValueAt(lRowNr,1) + ": ");
				lMsgBuffer.append(mBuchungenFrame.getValueAt(lRowNr,2) + ", ");
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
	 * Gemäss Interface muss diese Methode impl. werden.
	 * Wird in okAction aufgerufen
	 */
	public void addBuchungData(Buchung buchung) {
		mBuchungData.add(buchung);
		int lastRowNr = mBuchungData.getRowCount()-1;
		mBuchungenFrame.fireRowsInserted(lastRowNr-1,lastRowNr);
	}


	/**
	 * Einstellungen für CSV
	 */
	public void csvSetup() {
		if (mCsvSetup == null) {
			mCsvSetup = new CsvBankFrame(this);
		}
		mCsvSetup.setVisible(true);
	}

	/**
	 * Aufruf der View für CSV einlesen, mit der bankId
	 * @param pCompany
	 */
	public void csvAction (CsvBank bank) {
		if (mCsvFrame == null) {
			mCsvFrame = new CsvReaderKeywordFrame(bank, this);
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
		return ((Long)mBuchungenFrame.getValueAt(lRowNr,6)).longValue();
	}



	@Override
	public BuchungenBaseFrame getBuchungenFrame() {
		return mBuchungenFrame;
	}


	@Override
	public boolean isBuchungView() {
		return true;
	}

	@Override
	public void hideEingabe() {
		// nichts machen in dieser Klasse

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
