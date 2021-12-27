package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
import com.rmo.fibu.model.CsvCompany;
import com.rmo.fibu.model.CsvCompanyData;
import com.rmo.fibu.model.CsvKeyKonto;
import com.rmo.fibu.model.CsvKeyKontoData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.FibuData;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.model.from.CsvCompanyDataFrom;
import com.rmo.fibu.model.from.CsvKeywordDataFrom;
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
		lPanel.add(Box.createRigidArea(new Dimension(5, 5)));

		btnKopieren = new JButton("kopieren");
		btnKopieren.setFont(Config.fontTextBold);
		btnKopieren.setPreferredSize(new Dimension(breite, hoehe));
		btnKopieren.setEnabled(false);

		btnKopieren.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionKopieren();
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
	private void actionKopieren() {
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
		copyCsvCompany();
		copyCvsKeywords();

		btnKopieren.setEnabled(false);
		message.setText("Alles kopiert");
	}

	/**
	 * Kopiert den Titel der Fibu
	 */
	private void copyFibuTitel() {
		FibuDataFrom fibuDataOld = (FibuDataFrom) DataBeanContext.getContext().getDataBean(FibuDataFrom.class);
		FibuData fibuData = (FibuData) DataBeanContext.getContext().getDataBean(FibuData.class);
		try {
			String fibuTitel = fibuDataOld.readFibuTitel();
			fibuData.writeFibuName(fibuTitel);
			Config.sFibuTitel = fibuTitel;
		} catch (FibuException ex) {
			message.setText("Fehler bei copyFibuTitel: " + ex.getMessage());
		}

	}

	/**
	 * Kopiert alle Konto daten, wenn nicht gefunden wird neues Konto angelegt.
	 */
	private void copyKonto() {
		KontoDataFrom kontoDataOld = (KontoDataFrom) DataBeanContext.getContext().getDataBean(KontoDataFrom.class);
		Iterator<Konto> iterOld = kontoDataOld.getIterator();
		KontoData kontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);
		Konto lKonto = new Konto();
		while (iterOld.hasNext()) {
			Konto lKontoOld = iterOld.next();
			try {
				lKonto = kontoData.read(lKontoOld.getKontoNr());
			} catch (KontoNotFoundException ex) {
				// wenn nicht gefunden, neue Anlegen
				lKonto.setKontoNr(lKontoOld.getKontoNr());
				lKonto.setText(lKontoOld.getText());
				lKonto.setIstSollKonto(lKontoOld.isSollKonto());
			}
			if (lKontoOld.getKontoNr() < Config.sERStart) {
				lKonto.setStartSaldo(lKontoOld.getSaldo());
				lKonto.setSaldo(lKontoOld.getSaldo());
			} else {
				lKonto.setStartSaldo(0);
				lKonto.setSaldo(1);
			}
			try {
				kontoData.add(lKonto);
			} catch (KontoNotFoundException ex) {
				// do nothing
			}
		}
	}

	/**
	 * Kopiert die Company daten für CSV.
	 */
	private void copyCsvCompany() {
		CsvCompanyDataFrom mPdfCompanyDataOld = (CsvCompanyDataFrom) DataBeanContext.getContext()
				.getDataBean(CsvCompanyDataFrom.class);
		Iterator<CsvCompany> iterOld = mPdfCompanyDataOld.getIterator();
		CsvCompanyData mPdfCompanyData = (CsvCompanyData) DataBeanContext.getContext()
				.getDataBean(CsvCompanyData.class);
		CsvCompany lPdfCompany = new CsvCompany();
		while (iterOld.hasNext()) {
			CsvCompany lPdfCompanyOld = iterOld.next();
			lPdfCompany.setCompanyID(lPdfCompanyOld.getCompanyID());
			lPdfCompany.setCompanyName(lPdfCompanyOld.getCompanyName());
			lPdfCompany.setDirPath(lPdfCompanyOld.getDirPath());
			lPdfCompany.setKontoNrDefault(lPdfCompanyOld.getKontoNrDefault());
			try {
				mPdfCompanyData.addData(lPdfCompany);
			} catch (FibuException ex) {
				message.setText("Fehler bei PdfCompany anlegen: " + ex.getMessage());
			}
		}
	}

	/**
	 * Kopiert die Keywords für CSV.
	 */
	private void copyCvsKeywords() {
		CsvKeywordDataFrom mPdfKeywordDataFrom = (CsvKeywordDataFrom) DataBeanContext.getContext()
				.getDataBean(CsvKeywordDataFrom.class);
		Iterator<CsvKeyKonto> iterFrom = mPdfKeywordDataFrom.getIterator();
		CsvKeyKontoData mCsvKeywordData = (CsvKeyKontoData) DataBeanContext.getContext()
				.getDataBean(CsvKeyKontoData.class);
		CsvKeyKonto lCvsKeyword = new CsvKeyKonto();
		while (iterFrom.hasNext()) {
			CsvKeyKonto lCvsKeywordFrom = iterFrom.next();
			lCvsKeyword.setCompanyId(lCvsKeywordFrom.getCompanyId());
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
			} catch (FibuException ex) {
				// do nothing
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
