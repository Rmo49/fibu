package com.rmo.fibu.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

/** Selektieren von Buchungen nach Soll- Habenkonto
 * @author Ruedi
 * TODO: model noch implementieren
 */
public class BuchungSelectDialog extends JDialog {

	private static final long serialVersionUID = -5377839122390325300L;
	/** Die Verbindung zur Buchung-View */
	private BuchungView			mBuchungView;
	// Eingabefelder
	private JTextField      	mSollKonto;
	private JTextField      	mHabenKonto;
	private JButton         	mButtonSelect;
	private JButton         	mButtonCancel;
	
	// Die Nummer der Row in der gesucht wird
	private int             	mSerachRow = 0;

	/**
	 * @param owner
	 * @param modal
	 * @throws java.awt.HeadlessException
	 */
	public BuchungSelectDialog(Frame owner, boolean modal)
		throws HeadlessException {
		super(owner, "Buchungen selektieren", modal);
		mBuchungView = (BuchungView)owner;
		this.setFont(Config.fontTextBold);
		init();
	}

	private void init() {
		Trace.println(3,"BuchungSelectPanel.init()");
		JPanel lPanel = new JPanel(new BorderLayout());
		this.getContentPane().add(lPanel);
		lPanel.add(initEingabe(), BorderLayout.CENTER);
		lPanel.add(initButtons(), BorderLayout.LINE_END);
		
		//--- Border
		lPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		initListeners();
		this.setLocationRelativeTo(getOwner());
		// --- Grössen setzen abhängig von Font-Grösse
		double multiplikator = (double)Config.windowTextSize / (double)12;
		int breite = (int)(300 * multiplikator);
		int hoehe = (int) (120 * multiplikator);
		this.setSize(new Dimension(breite, hoehe));

	}

	/** Eingabefelder initialisieren */
	private Component initEingabe() {
		JPanel lPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		JLabel lLabel;
		lLabel = new JLabel("Soll Konto");
		lLabel.setFont(Config.fontTextBold);
		lLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		c.weightx = 0.2;
		c.gridx = 0;
		c.gridy = 0;
		lPanel.add(lLabel, c);		
		lLabel = new JLabel("Haben Konto");
		lLabel.setFont(Config.fontTextBold);
		lLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		c.gridx = 0;
		c.gridy = 1;
		lPanel.add(lLabel, c);

		mSollKonto = new JTextField();
		mSollKonto.setFont(Config.fontTextBold);
		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = 0;
		lPanel.add(mSollKonto, c);
		mHabenKonto = new JTextField();
		mHabenKonto.setFont(Config.fontTextBold);
		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = 1;
		lPanel.add(mHabenKonto, c);
		
		return lPanel;
	}
	
	/** Buttons initialisieren */
	private Component initButtons() {
		JPanel lButtonPane = new JPanel();
		lButtonPane.setLayout(new BoxLayout(lButtonPane, BoxLayout.PAGE_AXIS));
		mButtonSelect = new JButton("Selektieren");
		mButtonSelect.setFont(Config.fontTextBold);
		mButtonSelect.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lButtonPane.add(mButtonSelect);
		mButtonCancel = new JButton("Abbrechen");
		mButtonCancel.setFont(Config.fontTextBold);
		mButtonCancel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lButtonPane.add(mButtonCancel);
		lButtonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		return lButtonPane;
	}


	private void initListeners() {
		mSollKonto.addFocusListener(new FocusListener() {
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
		mHabenKonto.addFocusListener(new FocusListener() {
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
		mButtonSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectActionPerformed();
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
	private void selectActionPerformed () {
		Trace.println(3, "SelectButton->actionPerformed()");
		if ( (mSollKonto.getText() == null || mSollKonto.getText().length() == 0) &&
			(mHabenKonto.getText() == null || mHabenKonto.getText().length() == 0) )
		{
			JOptionPane.showMessageDialog(this, "kein Select-Argumente eingegeben",
				"Selektieren von Buchnungen", JOptionPane.ERROR_MESSAGE);
			return;
		}
		mSerachRow++;
		BuchungListFrame lBuchungListe = mBuchungView.getBuchungListe();
		// Nach Beleg suchen, falls eingegeben
		if (mSollKonto.getText() != null && mSollKonto.getText().length() > 0) {
			while (mSerachRow < lBuchungListe.getRowCount() ) {
				String text = ((String)lBuchungListe.getValueAt(mSerachRow, 1)).toUpperCase();
				if (text.indexOf(mSollKonto.getText().toUpperCase()) >= 0) {
					// gefunden
					lBuchungListe.setRowSelectionInterval(mSerachRow, mSerachRow);
					// die selektierte Zeile sichtbar machen
					lBuchungListe.showRow(mSerachRow);
					return;
				}
				mSerachRow++;
			}
			return;
		}
		// Nach Text suchen, falls eingegeben
		if ( mHabenKonto.getText() != null && mHabenKonto.getText().length() >= 0) {
			while (mSerachRow < lBuchungListe.getRowCount() ) {
				String text = ((String)lBuchungListe.getValueAt(mSerachRow, 2)).toUpperCase();
				if (text.indexOf(mHabenKonto.getText().toUpperCase()) >= 0) {
					// gefunden
					lBuchungListe.setRowSelectionInterval(mSerachRow, mSerachRow);
					// @todo wie die selektierte Zeile sichtbar machen?
					lBuchungListe.showRow(mSerachRow);
					return;
				}
				mSerachRow++;
			}
		}
	}
}
