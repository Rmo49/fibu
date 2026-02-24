package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.sql.SQLException;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.CsvBank;
import com.rmo.fibu.model.CsvBankData;
import com.rmo.fibu.model.CsvKeyKonto;
import com.rmo.fibu.model.CsvKeyKontoData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.FibuData;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.model.from.CsvBankDataFrom;
import com.rmo.fibu.model.from.CsvKeywordDataFrom;
import com.rmo.fibu.model.from.DataBeanContextFrom;
import com.rmo.fibu.model.from.DbConnectionFrom;
import com.rmo.fibu.model.from.FibuDataBaseFrom;
import com.rmo.fibu.model.from.FibuDataFrom;
import com.rmo.fibu.model.from.KontoDataFrom;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

/**
 * Initialisiert das Frame und die entsprechenden Komponenten. Zeigt die Liste
 * der Fibus an und Buttons für den übertrag von Kontodaten.
 *
 * @author Ruedi
 *
 */
public class FibuCopyFrom extends JFrame implements ComponentListener {

	private static final long serialVersionUID = -1244023781986600652L;
	private final int FENSTER_BREITE = 300;
	private final int FENSTER_HOEHE = 400;

	// view elementes
	private JLabel message;
	private JList<String> fibuListView;
	private String fibuName;
	private JButton btnSelektieren;
	private JButton btnKopieren;
	private JCheckBox delKonto;

	public FibuCopyFrom() {
		this.setTitle("Konto Saldo kopieren");
		init();
	}

	/**
	 * Initialisierung des panels, Grösse, Listeners
	 */
	private void init() {
		Trace.println(2, "FibuCopyFrom.init()");
		initView();
		int breite = (int) (FENSTER_BREITE * Config.windowTextMultiplikator);
		int hoehe = (int) (FENSTER_HOEHE * Config.windowTextMultiplikator);
		this.setSize(new Dimension(breite, hoehe));

		this.setVisible(true);
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.addComponentListener(this);
	}

	/** Intialisierung aller View-Komonenten */
	private void initView() {
		Trace.println(3, "FibuView.initView()");
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(initMessage(), BorderLayout.PAGE_START);
		getContentPane().add(initFibuList(), BorderLayout.CENTER);
		getContentPane().add(initButtons(), BorderLayout.LINE_END);
	}

	private JPanel initMessage() {
		JPanel lPanel = new JPanel();
		message = new JLabel();
		message.setFont(Config.fontTextBold);
		message.setText("Ziel Fibu: " + Config.sFibuDbName);
		lPanel.add(message);
		return lPanel;
	}

	/** Intialisierung der Fibu-Liste */
	private Container initFibuList() {
		Box box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// convert to ArrayList
		DefaultListModel<String> fibuNames = Config.getFibuList();
		// die View der Liste
		fibuListView = new JList<>(fibuNames);
		fibuListView.setFont(Config.fontTextBold);
		fibuListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fibuListView.setSelectedIndex(0);
		fibuListView.setVisibleRowCount(10);

		// Config.sFibuDbName;

		JScrollPane scrollPane = new JScrollPane(fibuListView);
		int breite = (int) (FENSTER_BREITE * Config.windowTextMultiplikator);
		int hoehe = (int) (FENSTER_HOEHE * Config.windowTextMultiplikator);
//		this.setSize(new Dimension(breite, hoehe));
		scrollPane.setPreferredSize(new Dimension(breite, hoehe));
		box.add(scrollPane);
		return box;
	}

	/**
	 * Buttons initialisieren
	 *
	 * @return
	 */
	private Container initButtons() {
		JPanel lPanel = new JPanel();
		lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.PAGE_AXIS));
		lPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		btnSelektieren = new JButton("selektieren");
		btnSelektieren.setFont(Config.fontTextBold);
		int breite = (int) (100 * Config.windowTextMultiplikator);
		int hoehe = (int) (20 * Config.windowTextMultiplikator);
		btnSelektieren.setPreferredSize(new Dimension(breite, hoehe));
		btnSelektieren.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionSelect();
			}
		});
		lPanel.add(btnSelektieren);
		lPanel.add(Box.createRigidArea(new Dimension(5, 10)));

		delKonto = new JCheckBox("Konto zuerst löschen");
		delKonto.setSelected(true);
		lPanel.add(delKonto);
		lPanel.add(Box.createRigidArea(new Dimension(5, 5)));

		btnKopieren = new JButton("kopieren");
		btnKopieren.setFont(Config.fontTextBold);
		btnKopieren.setPreferredSize(new Dimension(breite, hoehe));
		btnKopieren.setEnabled(false);

		btnKopieren.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyFibu();
			}
		});
		lPanel.add(btnKopieren);

		return lPanel;
	}

	/**
	 * Konto übertragen, wenn nicht vorhanden, sonst Start-Saldo setzen.
	 */
	private void actionSelect() {
		message.setText("");
		if (!DbConnection.isFibuOpen()) {
			message.setText("Keine Fibu geöffnet");
			return;
		}
		fibuName = fibuListView.getSelectedValue();
		if (fibuName == null || fibuName.length() <= 0) {
			message.setText("Fibu wählen");
			return;
		}
		if (fibuName.equalsIgnoreCase(FibuData.getFibuData().getFibuName())) {
			message.setText("Zu dieser Fibu wird kopiert, andere wählen");
			return;
		}
		message.setText("kopiere Daten von: " + fibuName + " nach: " + Config.sFibuDbName);
		btnSelektieren.setEnabled(false);
		fibuListView.setEnabled(false);
		btnKopieren.setEnabled(true);
	}

	/**
	 * Alle daten kopieren
	 */
	private void copyFibu() {
		try {
			FibuDataBaseFrom.openFibu(fibuName);
		} catch (FibuException ex) {
			message.setText(ex.getMessage());
			return;
		} catch (FibuRuntimeException ex) {
			message.setText(ex.getMessage());
			return;
		}
		copyFibuTitel();
		copyKonto();
		copyCsvBank();
		copyCvsKeywords();

		btnKopieren.setEnabled(false);
		message.setText("Alles kopiert");
		// DB schliesse
		try {
			DbConnectionFrom.close();
		} catch (FibuException ex) {
			// nix tun
		}

	}

	/**
	 * Kopiert den Titel der Fibu
	 */
	private void copyFibuTitel() {
		FibuDataFrom fibuDataFrom = (FibuDataFrom) DataBeanContextFrom.getContext().getDataBean(FibuDataFrom.class);
		FibuData fibuData = (FibuData) DataBeanContext.getContext().getDataBean(FibuData.class);
		try {
			String fibuTitel = fibuDataFrom.readFibuTitel();
			fibuData.writeFibuName(fibuTitel);
			Config.sFibuTitel = fibuTitel;
			// TODO Datum von und bis + 1 Jahr?, DatumFormat
		} catch (FibuException ex) {
			message.setText("Fehler bei copyFibuTitel: " + ex.getMessage());
		}

	}

	/**
	 * Kopiert alle Konto daten, wenn nicht gefunden wird neues Konto angelegt.
	 */
	private void copyKonto() {
		KontoData kontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);

		if (delKonto.isSelected()) {
			// alle Konti löschen, falls noch keine Buchung darauf
			delKonti(kontoData);
		}
		// zuerst Startsalo aller bestehenden Konti auf 0 setzen
		setSaldoToZero(kontoData);
		copyKonti(kontoData);
	}


	/**
	 * Setzt alle Start-Saldo auf 0
	 * @param kontoData
	 */
	private void delKonti(KontoData kontoData) {
		Iterator<Konto> iter = kontoData.getIterator();
		while (iter.hasNext()) {
			Konto lKonto = iter.next();
			try {
				kontoData.delete(lKonto.getKontoNr());
			}
			catch (KontoNotFoundException ex) {
				// nix tun
			}
		}
	}

	/**
	 * Setzt alle Start-Saldo auf 0
	 * @param kontoData
	 */
	private void setSaldoToZero(KontoData kontoData) {
		Iterator<Konto> iter = kontoData.getIterator();
		while (iter.hasNext()) {
			Konto lKonto = iter.next();
			lKonto.setStartSaldo(0);
			try {
				kontoData.add(lKonto);
			}
			catch (KontoNotFoundException ex) {
				// nix tun
			}
		}
	}

	/**
	 * Alle Saldos kopieren
	 * @param kontoData
	 */
	private void copyKonti(KontoData kontoData) {
		KontoDataFrom kontoDataFrom = (KontoDataFrom) DataBeanContextFrom.getContext().getDataBean(KontoDataFrom.class);
		Iterator<Konto> iterFrom = kontoDataFrom.getIterator();
		Konto lKonto = new Konto();
		while (iterFrom.hasNext()) {
			Konto lKontoFrom = iterFrom.next();
			try {
				lKonto = kontoData.read(lKontoFrom.getKontoNr());
			} catch (KontoNotFoundException ex) {
				// wenn nicht gefunden, neue Anlegen
				lKonto.setKontoNr(lKontoFrom.getKontoNr());
				lKonto.setIstSollKonto(lKontoFrom.isSollKonto());
			}
			if (lKontoFrom.getKontoNr() < Config.sERStart) {
				lKonto.setStartSaldo(lKontoFrom.getSaldo());
				lKonto.setSaldo(lKontoFrom.getSaldo());
			} else {
				lKonto.setStartSaldo(0);
				lKonto.setSaldo(0);
			}
			// bei allen Konti Text nach alt setzen
			lKonto.setText(lKontoFrom.getText());
			try {
				kontoData.add(lKonto);
			} catch (KontoNotFoundException ex) {
				// do nothing
			}
		}
	}

	/**
	 * Kopiert die Bank daten für CSV.
	 */
	private void copyCsvBank() {
		// zuerst alle Daten löschen
		CsvBankData mReadSetAll = (CsvBankData) DataBeanContext.getContext()
				.getDataBean(CsvBankData.class);
		try {
			mReadSetAll.deleteAll();
		}
		catch (SQLException ex) {
			// nix machen
		}

		CsvBankDataFrom mPdfBankDataFrom = (CsvBankDataFrom) DataBeanContextFrom.getContext()
				.getDataBean(CsvBankDataFrom.class);
		Iterator<CsvBank> iterFrom = mPdfBankDataFrom.getIterator();
		CsvBankData mPdfBankData = (CsvBankData) DataBeanContext.getContext()
				.getDataBean(CsvBankData.class);
		CsvBank lPdfBank = new CsvBank();
		while (iterFrom.hasNext()) {
			CsvBank lPdfBankFrom = iterFrom.next();
			lPdfBank.setBankID(lPdfBankFrom.getBankID());
			lPdfBank.setBankName(lPdfBankFrom.getBankName());
			lPdfBank.setDirPath(lPdfBankFrom.getDirPath());
			lPdfBank.setKontoNrDefault(lPdfBankFrom.getKontoNrDefault());
			lPdfBank.setDocType(lPdfBankFrom.getDocType());
			if (lPdfBank.getDocType() == 0) {
				// das ist der default, wenn nichts gesetzt
				lPdfBank.setDocType(1);
			}
			lPdfBank.setWordBefore(lPdfBankFrom.getWordBefore());
			lPdfBank.setSpaltenArray(lPdfBankFrom.getSpaltenArray());
			try {
				mPdfBankData.addData(lPdfBank);
			} catch (FibuException ex) {
				message.setText("Fehler bei PdfCompany anlegen: " + ex.getMessage());
			}
		}
	}

	/**
	 * Kopiert die Keywords für CSV.
	 */
	private void copyCvsKeywords() {
		CsvKeywordDataFrom mPdfKeywordDataFrom = (CsvKeywordDataFrom) DataBeanContextFrom.getContext()
				.getDataBean(CsvKeywordDataFrom.class);
		Iterator<CsvKeyKonto> iterFrom = mPdfKeywordDataFrom.getIterator();
		CsvKeyKontoData mCsvKeywordData = (CsvKeyKontoData) DataBeanContext.getContext()
				.getDataBean(CsvKeyKontoData.class);
		CsvKeyKonto lCvsKeyword = new CsvKeyKonto();
		while (iterFrom.hasNext()) {
			CsvKeyKonto lCvsKeywordFrom = iterFrom.next();
			lCvsKeyword.setBankId(lCvsKeywordFrom.getBankId());
			lCvsKeyword.setKontoNr(lCvsKeywordFrom.getKontoNr());
			lCvsKeyword.setSh(lCvsKeywordFrom.getSh());
			lCvsKeyword.setSuchWort(lCvsKeywordFrom.getSuchWort());
			if (lCvsKeywordFrom.getTextNeu() == null) {
				lCvsKeyword.setTextNeu("");
			} else {
				lCvsKeyword.setTextNeu(lCvsKeywordFrom.getTextNeu());
			}
			try {
				mCsvKeywordData.add(lCvsKeyword);
			} catch (SQLException ex) {
				Trace.println(6, "copyCvsKeywords SQLException: " + ex.getMessage());
				message.setText("Fehler beim CvsKeyword kopieren, siehe Trace");
				message.setVisible(true);
			}
		}
	}

	// --- Component Listener
	@Override
	public void componentShown(ComponentEvent e) {

	}

	/**
	 * zurücksetzen wenn abgebrochen
	 */
	@Override
	public void componentHidden(ComponentEvent e) {
		btnSelektieren.setEnabled(true);
		btnKopieren.setEnabled(false);
		fibuListView.setEnabled(true);
		// message.setText(" ");
	}

	/**
	 * wird nicht aufgerufen, wenn window verändert
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		Dimension d = e.getComponent().getSize();
		this.setSize(d);
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}
}
