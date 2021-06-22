package com.rmo.fibu.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

/** Suchen einer Buchung nach BelegeNr oder Text.
 * @author Ruedi
 */
public class BuchungSuchDialog extends JDialog {

	private static final long serialVersionUID = -5377839122390325204L;
	private static int SUCH_BREITE = 300;
	private static int SUCH_HOEHE = 150;
	
	/** Die Verbindung zur Buchung-View */
	private BuchungView			mBuchungView;
	// Eingabefelder
	private JTextField      	mSuchBeleg;
	private JTextField      	mSuchText;
	private JTextField      	mBetragVon;
	private JTextField      	mBetragBis;
	private JButton         	mButtonSearch;
	private JButton         	mButtonCancel;
	
	// Die Nummer der Row in der gesucht wird
	private int             	mSerachRow = 0;

	/**
	 * @param owner
	 * @param modal
	 * @throws java.awt.HeadlessException
	 */
	public BuchungSuchDialog(Frame owner, boolean modal)
		throws HeadlessException {
		super(owner, "Buchung suchen", modal);
		mBuchungView = (BuchungView)owner;
		this.setFont(Config.fontTextBold);
		init();
	}

	private void init() {
		Trace.println(3,"BuchungSuchPanel.init()");
		JPanel lPanel = new JPanel(new GridLayout(1, 2));
		this.getContentPane().add(lPanel);
		lPanel.add(initEingabe());
		lPanel.add(initButtons());
		
		//--- Border
		lPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		initListeners();
		this.setLocationRelativeTo(getOwner());
		// --- Grössen setzen abhängig von Font-Grösse
		double multiplikator = (double)Config.windowTextSize / (double)12;
		int breite = (int)(SUCH_BREITE * multiplikator);
		int hoehe = (int) (SUCH_HOEHE * multiplikator);
		this.setSize(new Dimension(breite, hoehe));

	}

	/** Eingabefelder initialisieren */
	private Component initEingabe() {
		JPanel lPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		int y = 0;
		JLabel lLabel;
		lLabel = new JLabel("Text");
		lLabel.setFont(Config.fontTextBold);
		lLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		c.gridx = 0;
		c.gridy = y;
		lPanel.add(lLabel, c);

		mSuchText = new JTextField();
		mSuchText.setFont(Config.fontTextBold);
		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = y;
		lPanel.add(mSuchText, c);

		y++;
		lLabel = new JLabel("Beleg");
		lLabel.setFont(Config.fontTextBold);
		lLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		c.weightx = 0.2;
		c.gridx = 0;
		c.gridy = y;
		lPanel.add(lLabel, c);		
		
		mSuchBeleg = new JTextField();
		mSuchBeleg.setFont(Config.fontTextBold);
		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = y;
		lPanel.add(mSuchBeleg, c);
		
		y++;
		lLabel = new JLabel("Betrag von");
		lLabel.setFont(Config.fontTextBold);
		lLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		c.weightx = 0.2;
		c.gridx = 0;
		c.gridy = y;
		lPanel.add(lLabel, c);		
		
		mBetragVon = new JTextField();
		mBetragVon.setFont(Config.fontTextBold);
		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = y;
		lPanel.add(mBetragVon, c);
		
		y++;
		lLabel = new JLabel("bis");
		lLabel.setFont(Config.fontTextBold);
		lLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		c.weightx = 0.2;
		c.gridx = 0;
		c.gridy = y;
		lPanel.add(lLabel, c);		
		
		mBetragBis = new JTextField();
		mBetragBis.setFont(Config.fontTextBold);
		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = y;
		lPanel.add(mBetragBis, c);
		
		return lPanel;
	}
	
	/** Buttons initialisieren */
	private Component initButtons() {
		JPanel lButtonPane = new JPanel();
		lButtonPane.setLayout(new GridLayout(2, 1, 10, 5));
		mButtonSearch = new JButton("Suchen");
		mButtonSearch.setFont(Config.fontTextBold);
		mButtonSearch.setAlignmentX(Component.CENTER_ALIGNMENT);
		lButtonPane.add(mButtonSearch);
		mButtonCancel = new JButton("Abbrechen");
		mButtonCancel.setFont(Config.fontTextBold);
		mButtonCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
		lButtonPane.add(mButtonCancel);
		lButtonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		return lButtonPane;
	}


	private void initListeners() {
		mSuchBeleg.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				// do nothing
			}
			/** Es wurde etwas im Feld eingegeben */
			@Override
			public void focusLost(FocusEvent e) {
				mSerachRow = -1;
			}
		});
		mSuchText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				// do nothing
			}
			/** Es wurde etwas im Feld eingegeben */
			@Override
			public void focusLost(FocusEvent e) {
				mSerachRow = -1;
			}
		});
		mBetragVon.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				// do nothing
			}
			/** Es wurde etwas im Feld eingegeben */
			@Override
			public void focusLost(FocusEvent e) {
				mSerachRow = -1;
			}
		});
		mBetragBis.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				// do nothing
			}
			/** Es wurde etwas im Feld eingegeben */
			@Override
			public void focusLost(FocusEvent e) {
				mSerachRow = -1;
			}
		});

		mButtonSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchActionPerformed();
			}
		});
		mButtonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}

	public void setXY (int x, int y) {
		this.setLocation(x, y);
	}
	
	/** Search-Button wurde gedrückt.
	 *  Herausfinden welche Felder gesetzt sind.
	 *  Nach Buchungen mit diesem Inhalt suchen, wenn das 2. mal gedrückt weitersuchen.
	 */
	private void searchActionPerformed () {
		Trace.println(3, "SearchButton->actionPerformed()");
		mSerachRow++;
		BuchungListFrame lBuchungListe = mBuchungView.getBuchungListe();
		
		// Nach Text suchen, falls eingegeben
		if ( mSuchText.getText() != null && mSuchText.getText().length() > 0) {
			while (mSerachRow < lBuchungListe.getRowCount() ) {
				String text = ((String)lBuchungListe.getValueAt(mSerachRow, 2)).toUpperCase();
				if (text.indexOf(mSuchText.getText().toUpperCase()) >= 0) {
					// gefunden
					lBuchungListe.setRowSelectionInterval(mSerachRow, mSerachRow);
					// @todo wie die selektierte Zeile sichtbar machen?
					lBuchungListe.showRow(mSerachRow);
					return;
				}
				mSerachRow++;
			}
			JOptionPane.showMessageDialog(this, "Keine Buchung gefunden",
					"Suchen", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		// Nach Beleg suchen, falls eingegeben
		if (mSuchBeleg.getText() != null && mSuchBeleg.getText().length() > 0) {
			while (mSerachRow < lBuchungListe.getRowCount() ) {
				String text = ((String)lBuchungListe.getValueAt(mSerachRow, 1)).toUpperCase();
				if (text.indexOf(mSuchBeleg.getText().toUpperCase()) >= 0) {
					// gefunden
					lBuchungListe.setRowSelectionInterval(mSerachRow, mSerachRow);
					// die selektierte Zeile sichtbar machen
					lBuchungListe.showRow(mSerachRow);
					return;
				}
				mSerachRow++;
			}
			JOptionPane.showMessageDialog(this, "Keine Buchung gefunden",
					"Suchen", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
	
		// Nach Betrag suchen, falls eingegeben
		double betragVon = -1;
		double betragBis = -1;	
		if (mBetragVon.getText() != null && mBetragVon.getText().length() > 0) {
			betragVon = parseBetrag(mBetragVon.getText());
		}
		if	(mBetragBis.getText() != null && mBetragBis.getText().length() > 0){
			betragBis = parseBetrag(mBetragBis.getText());
		}
		if (betragVon >= 0 || betragBis >= 0) {
			if (betragBis < 0) {
				betragBis = 999999999;
			}
			while (mSerachRow < lBuchungListe.getRowCount() ) {	
				Double betrag = (Double) lBuchungListe.getValueAt(mSerachRow, 5);
				// gefunden
				if ( (betrag >= betragVon) && (betrag <= betragBis) ) {
					lBuchungListe.setRowSelectionInterval(mSerachRow, mSerachRow);
					// die selektierte Zeile sichtbar machen
					lBuchungListe.showRow(mSerachRow);
					return;					
				}
				mSerachRow++;
			}
			JOptionPane.showMessageDialog(this, "Keine Buchung gefunden",
					"Suchen", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
	}


	/**
	 *  String in double, wenn Fehler, dann
	 * @param text
	 * @return Betrag, wenn Fehler -1;
	 */
	private double parseBetrag(String text) {
		try {
			return Double.parseDouble(text); 
		}
		catch (NumberFormatException ex) {
			return -1;
		}
	}
}
