package com.rmo.fibu.view;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.KontoListPrinter;
import com.rmo.fibu.util.KontoListPrinterModel;

/**
 * Druckt die gesamte Liste aller Konti, zeigt den Fortschritt an.
 */
public class KontoListPrintDialog extends JDialog {
	private static final long serialVersionUID = -7874240576377106790L;
	/** Anzeige des Fortschrittes */
	JCheckBox checkBox = new JCheckBox("mit schleppendem Saldo");
	JProgressBar jProgressBar = new JProgressBar();
	JButton btnCancel = new JButton();
	JButton btnPrint = new JButton();
	KontoListPrinterModel printerModel;

	/** Konstruktor */
	public KontoListPrintDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		init();
	}

	/** Initialisierung */
	private void init() {
		try {
			jbInit();
			this.setSize(250, 150);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Initialisierung der Elemente */
	private void jbInit() throws Exception {
		this.getContentPane().setLayout(null);
		
		checkBox.setFont(Config.fontTextBold);
		checkBox.setSelected(false);
		checkBox.setVisible(true);
		checkBox.setBounds(new Rectangle(14, 10, 200,40)); 


		btnPrint.setFont(Config.fontTextBold);
		btnPrint.setText("drucken");
		btnPrint.setBounds(new Rectangle(10, 50, 98, 34));
		btnPrint.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnPrint_action(e);
			}
		});

		btnCancel.setFont(Config.fontTextBold);
		btnCancel.setText("Abbrechen");
		btnCancel.setBounds(new Rectangle(113, 50, 98, 34));
		btnCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnCancel_action(e);
			}
		});
		
		jProgressBar.setBounds(new Rectangle(15, 10, 199, 19));
		this.getContentPane().add(checkBox, null);
		this.getContentPane().add(btnPrint, null);
		this.getContentPane().add(btnCancel, null);
//		this.getContentPane().add(jProgressBar, null);

	}

	/** Handler, wenn drucken gewählt wurde */
	void btnPrint_action(ActionEvent e) {
		// Printer aufrufen, Daten siehe Interface: TablePrinterModel
		KontoListPrinterModel lModel = new KontoListPrinterModel();
		lModel.setMitSaldo(checkBox.isSelected());
		KontoListPrinter lPrinter = new KontoListPrinter(lModel);
		try {
			/*
			 * progressMonitor = new ProgressMonitor(this, "Test", "", 0, 100);
			 * progressMonitor.setProgress(0); progressMonitor.setMillisToDecideToPopup(2 *
			 * ONE_SECOND); //Create a timer. timer = new Timer(ONE_SECOND, new
			 * TimerListener()); timer.start(); task.go(); jProgressBar = ???
			 */
			lPrinter.doPrint();
			this.setVisible(false);
		} catch (PrinterException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Drucken", JOptionPane.ERROR_MESSAGE);
		}
	}

	/** aussteigen */
	void btnCancel_action(ActionEvent e) {
		this.dispose();
	}

}