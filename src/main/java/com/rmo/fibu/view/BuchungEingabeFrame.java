package com.rmo.fibu.view;

import java.awt.BorderLayout;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.util.Config;

/**
 * Ein Frame das Buchungen ändern aufnimmt, damit die zu ändernde Buchung
 * separat angezeigt werden kann.
 */
public class BuchungEingabeFrame extends JFrame implements BuchungEingabeInterface {
	private static final long serialVersionUID = -1293860257068482857L;

	// die Verbindung zur Klasse, die alles zusammenhält
	private KontoView		mKontoView;
	// die Buchung, die geändert werden soll
	private Buchung 		mBuchung;
	// die Anzeige der Buchnug mit allen Feldern
	private BuchungEingabe	mEingabe;
	/** das Model für alle Buchungen */
	protected BuchungData mBuchungData = null;


	public BuchungEingabeFrame(KontoView kontoView) {
		super("Buchnung ändern");
		mKontoView = kontoView;
	}

	/**
	 *
	 * @param buchungId
	 */
	public void init(long buchungId) {
		if (buchungId >= 0) {
			try {
				mBuchung = getBuchungData().read(buchungId);
			}
			catch (FibuException ex) {
				mBuchung = null;
			}
		}
		else {
			// wenn keine Buchung gewählt
			mBuchung = new Buchung();
			mBuchung.setBuchungText("keine Buchung selektiert");
		}

		if (mEingabe == null) {
			mEingabe = new BuchungEingabe(this);
			getContentPane().add(mEingabe.initView(), BorderLayout.CENTER);
		}
		// eine Buchung übertragen
		if (mBuchung != null) {
			mEingabe.copyToFields(mBuchung);
		}
		setSize(Config.winKontoBuchungDim);
		setLocation(Config.winKontoBuchungLoc);
	}

	/** Die überschriebene Methode hide
	 *
	 */
	@Override
	public void setVisible(boolean isVisible) {
		if (!isVisible) {
			Config.winKontoBuchungDim = getSize();
			Config.winKontoBuchungLoc = getLocation();
		}
		super.setVisible(isVisible);
	}


	@Override
	public Buchung getLastBuchung() {
		// die gleiche zurückgeben für diesen Falls
		return mBuchung;
	}

	@Override
	public BuchungData getBuchungData() {
		if (mBuchungData == null) {
			mBuchungData = (BuchungData) DataBeanContext.getContext().getDataBean(BuchungData.class);
		}
		return mBuchungData;
	}

	@Override
	public void setBuchungMenu(boolean enteringBooking, boolean mChangeing) {
		// wird hier nicht verwendet

	}

	@Override
	public void scrollToEnd() {
		mKontoView.getBuchungenFrame().scrollToLastEntry();
	}


	@Override
	public BuchungenBaseFrame getBuchungenFrame() {
		return mKontoView.getBuchungenFrame();
	}


	@Override
	public JDesktopPane getPaneCenter() {
		// nichts tun, ist nur für Buchung
		return null;
	}


	@Override
	public boolean isBuchungView() {
		return false;
	}

	/**
	 * Das Frame auf invisible setzen
	 */
	@Override
	public void hideEingabe() {
		this.setVisible(false);
	}

	/**
	 * Die Daten in die Eingabe kopieren
	 * @param pBuchung
	 */
	public void copyToFields(Buchung pBuchung) {
		mEingabe.copyToFields(pBuchung);
	}


}
