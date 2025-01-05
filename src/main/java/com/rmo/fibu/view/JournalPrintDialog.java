package com.rmo.fibu.view;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.JournalPrinterModel;
import com.rmo.fibu.util.TablePrinter;

/**
 * Druckt das Journal, zeigt den Fortschritt an.
 */
public class JournalPrintDialog extends JDialog {
	private static final long serialVersionUID = 4851610485407935411L;
	/** Anzeige des Fortschrittes */
	JProgressBar jProgressBar = new JProgressBar();
	JButton btnCancel = new JButton();
	JButton btnPrint = new JButton();
	JournalPrinterModel printerModel;

	/** Konstruktor */
	public JournalPrintDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		init();
	}

	/** Initialisierung */
	private void init() {
		try {
			jbInit();
			this.setSize(250, 120);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Initialisierung des Frames */
	private void jbInit() throws Exception {
		// 32.12.24
		// sofort auf drucker-Einstellungen, da auch dort abbrechen möglich
//    btnPrint_actionPerformed();

		this.getContentPane().setLayout(null);
		btnCancel.setFont(Config.fontTextBold);
		btnCancel.setText("Abbrechen");
		btnCancel.setBounds(new Rectangle(113, 43, 98, 34));
		btnCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnCancel_actionPerformed(e);
			}
		});
		btnPrint.setFont(Config.fontTextBold);
		btnPrint.setText("drucken");
		btnPrint.setBounds(new Rectangle(10, 43, 98, 34));
		btnPrint.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnPrint_actionPerformed(e);
			}
		});
		jProgressBar.setBounds(new Rectangle(15, 10, 199, 19));
		this.getContentPane().add(btnPrint, null);
		this.getContentPane().add(btnCancel, null);
		this.getContentPane().add(jProgressBar, null);
	}

	/** Handler, direkt drucken */
	void btnPrint_actionPerformed() {
		// Printer aufrufen, Daten siehe Interface: TablePrinterModel
		TablePrinter lPrinter = new TablePrinter(new JournalPrinterModel());
		try {
			lPrinter.doPrint();
			this.setVisible(false);
		} catch (PrinterException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Drucken", JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Handler, wenn drucken gewählt wurde */
	void btnPrint_actionPerformed(ActionEvent e) {
		// Printer aufrufen, Daten siehe Interface: TablePrinterModel
		TablePrinter lPrinter = new TablePrinter(new JournalPrinterModel());
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
	void btnCancel_actionPerformed(ActionEvent e) {
		this.dispose();
	}

}// endOfJournalPrinter