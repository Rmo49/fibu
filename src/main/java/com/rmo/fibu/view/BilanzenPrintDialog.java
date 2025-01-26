package com.rmo.fibu.view;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.rmo.fibu.util.Config;

public class BilanzenPrintDialog extends JDialog {
	private static final long serialVersionUID = 5511906371328147981L;

	private String[] reihenfolge = {"0", "1", "2", "3"};

	private JLabel datum;
	private TextField datumBilanz;
	private JLabel info1;
	private JLabel info2;
	private JComboBox<String> startBilanzCombo = new JComboBox<>(reihenfolge);
	private JLabel startBilanzText = new JLabel("Start Bilanz");
	private JComboBox<String> bilanzCombo = new JComboBox<>(reihenfolge);
	private JLabel bilanzText = new JLabel("Bilanz");
	private JComboBox<String> erCombo = new JComboBox<>(reihenfolge);
	private JLabel erText = new JLabel("Erfolgsrechnung");

	JButton btnCancel = new JButton();
	JButton btnPrint = new JButton();
	private boolean isCancelSelected = false;

	/** Konstruktor alt */
	public BilanzenPrintDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		init();
	}

	/** Konstruktor alt */
	public BilanzenPrintDialog(Frame owner, boolean modal) {
		super(owner, modal);
		init();
	}



	/** Initialisierung */
	private void init() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Initialisierung der Elemente */
	private void jbInit() throws Exception {
		this.getContentPane().setLayout(null);
		int comboWidth = 40;
		int comboHight = 25;
		int yPos = 10;
		int yLineSize = comboHight + 5;

		// Datum auf Setzen
		DateFormat dateFormat = new SimpleDateFormat("d.M.yyyy");
		Calendar cal = Calendar.getInstance();
		Calendar calBis = Calendar.getInstance();
		calBis.setTime(Config.sDatumBis);
		if (cal.compareTo(calBis) > 0) {
			cal = calBis;
		}
		else {
			// letzte des Monats setzen
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.add(Calendar.DATE, -1);
		}

		datum = new JLabel("Abschluss Datum");
		datum.setFont(Config.fontTextBold);
		datum.setBounds(new Rectangle(10, yPos, 120, comboHight));

		datumBilanz = new TextField();
		datumBilanz.setFont(Config.fontText);
		datumBilanz.setText(dateFormat.format(cal.getTime()));
		datumBilanz.setBounds(new Rectangle(130, yPos, 70, comboHight));

		yPos += yLineSize;
		info1 = new JLabel("0: nicht drucken");
		info1.setFont(Config.fontText);
		info1.setBounds(new Rectangle(10, yPos, 200, comboHight));

		yPos += 20;
		info2 = new JLabel("1..3: Reichenfolge auf der Seite");
		info2.setFont(Config.fontText);
		info2.setBounds(new Rectangle(10, yPos, 200, comboHight));

		yPos += yLineSize;
		startBilanzCombo.setSelectedIndex(0);
		startBilanzCombo.setBounds(new Rectangle(10, yPos, comboWidth, comboHight));
		startBilanzText.setFont(Config.fontTextBold);
		startBilanzText.setBounds(new Rectangle(60, yPos, 100, comboHight));

		yPos += yLineSize;
		bilanzCombo.setSelectedIndex(0);
		bilanzCombo.setBounds(new Rectangle(10, yPos, comboWidth, comboHight));
		bilanzText.setFont(Config.fontTextBold);
		bilanzText.setBounds(new Rectangle(60, yPos, 100, comboHight));

		yPos += yLineSize;
		erCombo.setSelectedIndex(0);
		erCombo.setBounds(new Rectangle(10, yPos, comboWidth, comboHight));
		erText.setFont(Config.fontTextBold);
		erText.setBounds(new Rectangle(60, yPos, 100, comboHight));

		yPos += yLineSize;
		btnPrint.setFont(Config.fontTextBold);
		btnPrint.setText("drucken");
		btnPrint.setBounds(new Rectangle(10, yPos, 98, 34));
		btnPrint.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnPrint_action(e);
			}
		});

		btnCancel.setFont(Config.fontTextBold);
		btnCancel.setText("Abbrechen");
		btnCancel.setBounds(new Rectangle(113, yPos, 98, 34));
		btnCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnCancel_action(e);
			}
		});

		this.getContentPane().add(datum, null);
		this.getContentPane().add(datumBilanz, null);
		this.getContentPane().add(info1, null);
		this.getContentPane().add(info2, null);
		this.getContentPane().add(startBilanzCombo, null);
		this.getContentPane().add(startBilanzText, null);
		this.getContentPane().add(bilanzCombo, null);
		this.getContentPane().add(bilanzText, null);
		this.getContentPane().add(erCombo, null);
		this.getContentPane().add(erText, null);
		this.getContentPane().add(btnPrint, null);
		this.getContentPane().add(btnCancel, null);

		this.setSize(250, yPos + (yLineSize*3));
	}

	/** Handler, wenn drucken gewählt wurde */
	void btnPrint_action(ActionEvent e) {
		isCancelSelected = false;
		dispose();
	}


	public int getPosStartBilanz() {
		return startBilanzCombo.getSelectedIndex();
	}

	public int getPosBilanz() {
		return bilanzCombo.getSelectedIndex();
	}

	public int getPosER() {
		return erCombo.getSelectedIndex();
	}

	public String getDatum() {
		return datumBilanz.getText();
	}

	/** aussteigen */
	void btnCancel_action(ActionEvent e) {
		isCancelSelected = true;
		dispose();
	}

	public boolean isCancelSelected() {
		return isCancelSelected;

	}
}
