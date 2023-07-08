package com.rmo.fibu.view;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.rmo.fibu.exception.BuchungValueException;
import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Datum;
import com.rmo.fibu.util.Trace;
import com.rmo.fibu.view.util.JFormattedTextFieldExt;
import com.rmo.fibu.view.util.JTextFiledExt;

public class BuchungEingabe extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 2924698558789708812L;
	/** Das Objekt wo die Eingabe eingebettet ist */
	private BuchungInterface mParent;

	private KontoListDialog mKontoListDialog;

	/** KontoListe für die Anzeige aller Konti */
//	private KontoListScrollPane		mKontoListe;

	// ---- die TextFelder für die Eingabe
	private JTextField mTfDatum;
	private JTextField mTfBeleg;
	private JTextField mTfText;
	private JTextField mTfSoll;
	private JTextField mTfHaben;
	private JFormattedTextField mTfBetrag;
	// Die ID der Buchung, die bearbeitet wird, ist -1 wenn neu.
	private long mId = -1;

	// ----- Temporäre Buchung fuer die naechste Eingabe
	private Buchung mTempBuchung = new Buchung();
	private boolean mDatumSame = false;
	private boolean mBelegSame = false;

	/**
	 * Das Feld, das zuletzt den Focus verloren hat, wird verwendet, wenn etwas in
	 * der KontoListe selektiert wurde
	 */
	private JTextComponent mFieldToFill = null;
	/**
	 * Zur Steuerung, damit die Selektion in der Kontoliste übernommen werden kann
	 */
	private boolean mHasKontoLostFocus = false;

	/**
	 * Create the panel.
	 */
	public BuchungEingabe(BuchungInterface parent) {
		Trace.println(3, "BuchungEingabe.Konstruktor");
		mParent = parent;
	}

	/**
	 * Initialisiertung der View
	 *
	 * @return
	 */
	public Container initView() {
		Trace.println(3, "BuchungEingabe.initEingabe()");
		// die letzte Buchung in den Temporären Speicher
		mTempBuchung = mParent.getLastBuchung();

		JPanel lPanel = new JPanel(new GridLayout(0, 1));
		lPanel.add(initEnterFields());
		mKontoListDialog = new KontoListDialog(this);
		mKontoListDialog.init();

		initListenersDatum();
		initListenersBeleg();
		initListenersText();
		initListenersSoll();
		initListenersHaben();
		initListenersBetrag();

		return lPanel;
	}

	/**
	 * Initialisierung der Eingabefelder
	 */
	private Container initEnterFields() {
		Trace.println(4, "BuchungEingabe.initEnterFields()");
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
		// --- Datum mit parser
		mTfDatum = new JTextFiledExt();
		lLayout.setConstraints(mTfDatum, lConstraints);
		lPanel.add(mTfDatum);

		// --- Beleg ----------------------------------
		lConstraints.weightx = 10.0;
		mTfBeleg = new JTextFiledExt();
		lLayout.setConstraints(mTfBeleg, lConstraints);
		lPanel.add(mTfBeleg);

		// --- Text -----------------------------------
		lConstraints.weightx = 100.0;
		mTfText = new JTextFiledExt();
		lLayout.setConstraints(mTfText, lConstraints);
		lPanel.add(mTfText);

		// --- Soll ----------------------------
		lConstraints.weightx = 5.0;
		mTfSoll = new JTextFiledExt();
		mTfSoll.setColumns(4);
		lLayout.setConstraints(mTfSoll, lConstraints);
		lPanel.add(mTfSoll);

		// --- Haben ------------------------
		lConstraints.gridwidth = GridBagConstraints.RELATIVE;
		mTfHaben = new JTextFiledExt();
		mTfHaben.setColumns(4);
		lLayout.setConstraints(mTfHaben, lConstraints);
		lPanel.add(mTfHaben);

		// --- Betrag ------------------------------------
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
		mTfDatum.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				focusGainedEnterField(mTfDatum, mTempBuchung.getDatumAsString());
				hideKontoListe();
				if (mTfDatum.getText().length() > 2) {
					mTfDatum.setCaretPosition(2);
				}
				// TODO evt. implementieren
				// gehe zum letzten Eintrag in der Buchungsliste
//				mBuchungListe.scrollToLastEntry();
			}

			@Override
			public void focusLost(FocusEvent e) {
				datumFocusLost();
			}
		});
		// wenn Enter-Taste gedrückt
		mTfDatum.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mTfBeleg.requestFocus();
			}
		});
	}

	/** Listeners für das Eingabefeld Beleg */
	private void initListenersBeleg() {
		// wenn cursor in Feld und wenn verlassen wird, Eingabe prüfen
		mTfBeleg.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				belegFocusGained();
			}

			@Override
			public void focusLost(FocusEvent e) {
				belegFocusLost();
			}
		});
		// wenn Enter-Taste gedrückt
		mTfBeleg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mTfText.requestFocus();
			}
		});
	}

	/** Listeners für das Eingabefeld Text */
	private void initListenersText() {
		mTfText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				Trace.println(6, "BuchungEingabe Text gain Focus");
				focusGainedEnterField(mTfText, mTempBuchung.getBuchungText());
				hideKontoListe();
			}

			@Override
			public void focusLost(FocusEvent e) {
				Trace.println(6, "BuchungEingabe Text lost Focus");
				focusLostEnterField(mTfText);
				// nix
			}
		});

		// wenn Enter-Taste gedrückt
		mTfText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mTfSoll.requestFocus();
			}
		});
	}

	/** Listeners für das Eingabefeld Soll */
	private void initListenersSoll() {
		mTfSoll.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				Trace.println(6, "BuchungEingabe SollKonto gain Focus");
				if (!e.isTemporary()) {
					focusGainedEnterField(mTfSoll, mTempBuchung.getSollAsString());
					showKontoListe();
					mHasKontoLostFocus = false;
					mFieldToFill = mTfSoll;
					try {
						mKontoListDialog.selectRow(mTfSoll.getText());
					} catch (KontoNotFoundException ex) {
					}
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				Trace.println(6, "BuchungEingabe SollKonto lost Focus");
				if (!e.isTemporary()) {
					focusLostEnterField(mTfSoll);
					mHasKontoLostFocus = true;
				}
			}
		});
		// überwachung der Tastatur-Eingabe
		mTfSoll.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				kontoKeyTyped(e, mTfSoll, mTfHaben);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// nix tun
			}

			@Override
			public void keyPressed(KeyEvent e) {
				kontoKeyPressed(e);
			}
		});
		// Bewegungen der Mouse kontrollieren
		mTfSoll.addMouseListener(new KontoMouseAdapter(mTfSoll));
		// wenn Enter-Taste gedrückt
		mTfSoll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mTfHaben.requestFocus();
			}
		});
	}

	/** Listeners für das Eingabefeld Haben */
	private void initListenersHaben() {
		mTfHaben.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				Trace.println(6, "BuchungEingabe HabenKonto gain Focus");
				if (!e.isTemporary()) {
					showKontoListe();
					mHasKontoLostFocus = false;
					mFieldToFill = mTfHaben;
					focusGainedEnterField(mTfHaben, mTempBuchung.getHabenAsString());
					try {
						mKontoListDialog.selectRow(mTfHaben.getText());
					} catch (KontoNotFoundException ex) {
					}
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				Trace.println(6, "BuchungEingabe HabenKonto lost Focus");
				if (!e.isTemporary()) {
					focusLostEnterField(mTfHaben);
					mHasKontoLostFocus = true;
				}
			}
		});
		// überwachung der Tastatur-Eingabe
		mTfHaben.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				kontoKeyTyped(e, mTfHaben, mTfBetrag);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// nix tun
			}

			@Override
			public void keyPressed(KeyEvent e) {
				kontoKeyPressed(e);
			}
		});
		// Bewegungen der Mouse kontrollieren
		mTfHaben.addMouseListener(new KontoMouseAdapter(mTfHaben));
		// wenn Enter-Taste gedrückt
		mTfHaben.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mTfBetrag.requestFocus();
			}
		});
	}

	/** Listeners für das Eingabefeld Betrag */
	private void initListenersBetrag() {
		mTfBetrag.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (!e.isTemporary()) {
					focusGainedEnterField(mTfBetrag, mTempBuchung.getBetragAsString());
					hideKontoListe();
					mTfBetrag.selectAll();
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (!e.isTemporary()) {
					focusLostEnterField(mTfBetrag);
				}
			}
		});
		// wenn Enter-Taste gedrückt
		mTfBetrag.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO mButtonOk evt. impl.
//				mButtonOk.requestFocus();
			}
		});
	}

	// ----- Enter-Field Event Handling -------------------------

	/**
	 * FocusGained Behandlung für alle Standard-Enter Fields. In ein leeres Feld
	 * wird der letzt Wert eingetragen. Wenn das Feld leer ist, nichts
	 */
	private void focusGainedEnterField(JTextComponent field, String defaultText) {
		Trace.println(6, "BuchungEingabe.focusGainedEnterField()");
		// nichts eintragen, wenn schon etwas drin.
		if (field.getText() == null || field.getText().length() > 0)
			return;
		field.setText(defaultText);
		field.selectAll();
	}

	/** FocusLost Behandlung für alle Standard-Enter Fields */
	private void focusLostEnterField(JTextComponent field) {
		Trace.println(6, "BuchungView.focusLostEnterField()");
		isTfEmpty(field, true);
		mParent.enableButtons();
		mFieldToFill = field;
	}

	/** Parsed das Datum */
	private void datumFocusLost() {
		Trace.println(5, "BuchungView.datumFocusLost()");
		try {
			Datum datum = new Datum(mTfDatum.getText());
			mTfDatum.setText(datum.toString());
			mTfDatum.setBackground(Color.white);
			// Testen, ob es dem letzten Datum entspricht
			if (mTfDatum.getText().equals(mTempBuchung.getDatumAsString()))
				mDatumSame = true;
			else
				mDatumSame = false;
			isTfEmpty(mTfDatum, true);
			// TODOdeleteMessage
//			deleteMessage();
			mParent.enableButtons();
		} catch (ParseException pEx) {
			mTfDatum.setBackground(Color.yellow);
			mParent.setMessage("Fehler: " + pEx.getMessage());
		}
	}

	/** Werte der letzten Buchung, bzw. selektierten Buchung eintragen */
	private void belegFocusGained() {
		Trace.println(5, "BuchungView.belegFocusGained()");
		hideKontoListe();
		// nichts tun, wenn bereits etwas eingegeben
		if (mTfBeleg.getText() == null || mTfBeleg.getText().length() > 0)
			return;
		if (mDatumSame && mBelegSame) {
			mTfBeleg.setText(mTempBuchung.getBeleg());
		} else {
			mTfBeleg.setText(Config.addOne(mTempBuchung.getBeleg()));
		}
		mParent.enableButtons();
	}

	/** Beleg Eingaben prüfen, ob etwas eingegeben, Feld markieren */
	private void belegFocusLost() {
		Trace.println(5, "BuchungView.belegFocusLost()");
		if (mTfBeleg.getText() == null || mTfBeleg.getText().length() > 0)
			return;
		isTfEmpty(mTfBeleg, true);
		mParent.enableButtons();
	}

	/**
	 * prüft ob das Textfeld leer ist
	 *
	 * @param mark true wenn das Feld markiert werden soll
	 * @return true wenn leer ist.
	 */
	private boolean isTfEmpty(JTextComponent textField, boolean mark) {
		// Document doc = textField.getDocument();
		if ((textField.getText() == null) || (textField.getText().length() < 1)) {
			if (mark)
				textField.setBackground(Color.yellow);
			return true;
		}
		textField.setBackground(Color.white);
		return false;
	}

	/**
	 * In KonotFelder (TfSoll oder TfHaben) wurde eine Taste gedrückt nur Backspace
	 */
	private void kontoKeyPressed(KeyEvent e) {
		Trace.println(5, "BuchungView.kontoKeyPressed (KeyCode:" + e.getKeyCode() + ")");
		if (e.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
			// nur BackSpace erlaubt (nicht Pfeil links / rechts, Delete etc)
			e.consume();
		}
	}

	/**
	 * Konto-Felder prüfen, nur Zahlen und Backspace erlaubt. Die Eingabe
	 * zusammenstellen und an die Kontoliste übergeben. Wenn eine Erweiterung der
	 * Kontonummer zurückkommt, diese setzen
	 */
	private void kontoKeyTyped(KeyEvent event, JTextField kontoField, JComponent nextField) {
		Trace.println(5, "BuchungView.kontoKeyTyped(" + event.getKeyChar() + " " + event.getKeyCode() + ")");
		// BackSpace gedrückt?
		boolean lBackSpace = (event.getKeyChar() == KeyEvent.VK_BACK_SPACE); // '\b');
		boolean lEnter = (event.getKeyChar() == KeyEvent.VK_ENTER); // '\n');
		// nur BackSpace, Enter und Zahlen erlaubt, sonst konsumieren
		if (lBackSpace || lEnter || (event.getKeyChar() >= '0' && event.getKeyChar() <= '9')) {
		} else {
			event.consume();
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
			} else {
				lEingabe = lEingabe.substring(0, lEingabe.length() - 1);
			}
		} else {
			lEingabe += event.getKeyChar();
		}
		try {

			String lKontoNr = mKontoListDialog.selectRow(lEingabe);
			// wenn KontoNummer vervollständigt, dann diese setzen
			if (!lBackSpace && (lKontoNr.length() > lEingabe.length())) {
				event.consume();
				kontoField.setText(lKontoNr);
			}
		} catch (KontoNotFoundException ex) {
			event.consume();
		}
	}

	/**
	 * Wenn eine Kontonummer selektiert wurde, vom KontoListDialog.
	 *
	 * @param kontoNr
	 */
	public void kontoSelected(int kontoNr) {
		mFieldToFill.setText(Integer.toString(kontoNr));
	}

	/**
	 * prüft alle Eingabefelder.
	 *
	 * @param mark true wenn die Felder markiert werden sollen, die leer sind
	 */
	public int hasEnterFieldsEmpty(boolean mark) {
		int nrEmpty = 0;
		if (isTfEmpty(mTfBeleg, mark))
			nrEmpty++;
		if (isTfEmpty(mTfText, mark))
			nrEmpty++;
		if (isTfEmpty(mTfSoll, mark))
			nrEmpty++;
		if (isTfEmpty(mTfHaben, mark))
			nrEmpty++;
		if (isTfEmpty(mTfBetrag, mark))
			nrEmpty++;
		return nrEmpty;
	}

	/** Löscht den Inhalt der Eingabefelder */
	public void clearEingabe() {
		// --- Datum
		mTfDatum.setText("");
		mTfDatum.setBackground(Color.white);
		// --- Beleg
		mTfBeleg.setText("");
		mTfBeleg.setBackground(Color.white);
		// --- Text
		mTfText.setText("");
		mTfText.setBackground(Color.white);
		// --- Soll
		mTfSoll.setText("");
		mTfSoll.setBackground(Color.white);
		// --- Text
		mTfHaben.setText("");
		mTfHaben.setBackground(Color.white);

		// --- Betrag
		mTfBetrag.setText("");
		mTfBetrag.setBackground(Color.white);
		mId = -1;
	}

	/** Kopiert den Inhalt der Eingabefelder in den temporären Speicher */
	private void copyToTemp() {
		try {
			// --- Datum
			if (mTempBuchung.getDatum() != null && mTempBuchung.getDatumAsString().equals(mTfDatum.getText()))
				mDatumSame = true;
			else
				mDatumSame = false;
			mTempBuchung.setDatum(mTfDatum.getText());
			// --- Beleg
			if (mTempBuchung.getBeleg() != null && mTempBuchung.getBeleg().equals(mTfBeleg.getText()))
				mBelegSame = true;
			else
				mBelegSame = false;
			mTempBuchung.setBeleg(mTfBeleg.getText());
			// --- Text
			mTempBuchung.setBuchungText(mTfText.getText());
			// --- Soll
			mTempBuchung.setSoll(mTfSoll.getText());
			// --- Haben
			mTempBuchung.setHaben(mTfHaben.getText());
			// --- Betrag
			mTempBuchung.setBetrag(((Number) mTfBetrag.getValue()).doubleValue());
		} catch (ParseException e) {
		} catch (BuchungValueException e) {
		}
		// nix machen
	}

	/**
	 * prüft die Eingabefelder und kopiert deren Inhalt in Buchung.
	 *
	 * @return Buchung falls alle Felder richtige Werte enthalten, sonst null
	 */
	public Buchung copyToBuchung() throws BuchungValueException {
		Trace.println(4, "BuchungView.copyToBuchung()");
		// zuerst prüfen, ob ein Feld leer ist.
		if (hasEnterFieldsEmpty(true) > 0) {
			throw new BuchungValueException("Eingabe fehlt");
		}
		Buchung lBuchung = new Buchung();
		String lErrorFeld = ""; // Angabe welches Feld
		try {
			lErrorFeld = "Datum";
			lBuchung.setDatum(mTfDatum.getText());
			lErrorFeld = "Beleg";
			lBuchung.setBeleg(mTfBeleg.getText());
			lErrorFeld = "Buchungstext";
			lBuchung.setBuchungText(mTfText.getText());
			lErrorFeld = "Soll-Konto";
			lBuchung.setSoll(Integer.valueOf(mTfSoll.getText()).intValue());
			lErrorFeld = "Haben-Konto";
			lBuchung.setHaben(Integer.valueOf(mTfHaben.getText()).intValue());
			lErrorFeld = "Betrag";
			mTfBetrag.commitEdit();
			double betrag = ((Number) mTfBetrag.getValue()).doubleValue();
			if (betrag <= 0) {
				throw new Exception("Betrag muss Grösser 0 sein");
			}
			lBuchung.setBetrag(betrag);
			lBuchung.setID(mId);
		} catch (Exception pEx) {
			pEx.printStackTrace(Trace.getPrintWriter());
			throw new BuchungValueException("Fehler in " + lErrorFeld + ":" + pEx.getMessage());
		}
		return lBuchung;
	}

	/**
	 * prüft die Eingabefelder und kopiert deren Inhalt in Buchung.
	 */
	public void copyToGui(Buchung pBuchung) {
		Trace.println(4, "BuchungView.copyToGui()");
		mTfDatum.setText(pBuchung.getDatumAsString());
		mTfBeleg.setText(pBuchung.getBeleg());
		mTfText.setText(pBuchung.getBuchungText());
		mTfSoll.setText(pBuchung.getSollAsString());
		mTfHaben.setText(pBuchung.getHabenAsString());
		mTfBetrag.setText(pBuchung.getBetragAsString());
		mId = pBuchung.getID();
	}

	/**
	 * Die Kontoliste in den Vordergrund
	 */
	private void showKontoListe() {
		mKontoListDialog.getDialog().setLocationRelativeTo(mTfDatum);
		mKontoListDialog.getDialog().setVisible(true);
		mKontoListDialog.getDialog().setAlwaysOnTop(true);
	}

	/**
	 * KontoListe in den Hintergrund
	 */
	private void hideKontoListe() {
		mKontoListDialog.getDialog().setVisible(false);
		// TODO mHasKontoLostFocus wird das noch gebraucht?
//		mHasKontoLostFocus = false;
	}

}
