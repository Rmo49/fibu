package com.rmo.fibu.view;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;

import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.TablePrinter;
import com.rmo.fibu.util.TablePrinterModel;

/** Druckt das Journal, zeigt den Fortschritt an.
 */
public class JournalPrintDialog extends JDialog
{
	private static final long serialVersionUID = 4851610485407935411L;
	/** Anzeige des Fortschrittes */
    JProgressBar jProgressBar = new JProgressBar();
    JButton btnCancel = new JButton();
    JButton btnPrint = new JButton();
	JournalPrinterModel printerModel;

/** Konstruktor */
public JournalPrintDialog(Frame owner, String title, boolean modal) {
	super (owner, title, modal);
	init();
}

/** Initialisierung */
private void init() {
    try {
        jbInit();
		this.setSize(250,120);
    }
    catch(Exception e) {
        e.printStackTrace();
    }
}

/** Initialisierung des Frames */
private void jbInit() throws Exception {
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

/** Handler, wenn drucken gewählt wurde */
void btnPrint_actionPerformed(ActionEvent e) {
	// Printer aufrufen, Daten siehe Interface: TablePrinterModel
	TablePrinter lPrinter = new TablePrinter( new JournalPrinterModel() );
	try {
		/*
		progressMonitor = new ProgressMonitor(this, "Test", "", 0, 100);
		progressMonitor.setProgress(0);
		progressMonitor.setMillisToDecideToPopup(2 * ONE_SECOND);
		//Create a timer.
		timer = new Timer(ONE_SECOND, new TimerListener());
		timer.start();
		task.go();
		jProgressBar = ???
		*/
		lPrinter.doPrint();
		this.setVisible(false);
	}
	catch (PrinterException ex) {
		JOptionPane.showMessageDialog(this, ex.getMessage(),
			"Drucken", JOptionPane.ERROR_MESSAGE);
	}
}

/** aussteigen */
void btnCancel_actionPerformed(ActionEvent e) {
	this.dispose();
}

//-------- innere Klasse für drucken ------------------------------------
/** Stellt die Verbindung zu den Daten her und steuert die Darstellung.
 */
private class JournalPrinterModel implements TablePrinterModel
{
	/** Das Model zur Tabelle */
	private JTable mBuchungTable;

	public JournalPrinterModel() {
		BuchungData mBuchungData =
			(BuchungData) DataBeanContext.getContext().getDataBean(BuchungData.class);
		mBuchungTable = new JTable(new BuchungModel(mBuchungData));
	}

	/** Die Tabelle, die gedruckt werden soll */
	@Override
	public JTable getTableToPrint() {
		return mBuchungTable;
	}

	/** Die Anzahl Kopfzeilen */
	@Override
	public int getHeaderCount() {
		return 1;
	}

	/** Die Kopfzeile, wir linksbündig angezeigt.
	 *  Die Seitenzahl wird automatisch rechts generiert */
	@Override
	public String getHeader(int number) {
		return "Journal " + Config.sFibuTitel;
	}

	/** Die Anzahl Spalten */
	@Override
	public int getColCount() {
		return 6;
	}

	/** Die relative Spaltenbreiten, diese werden der Seitenbreite angepasst. */
	@Override
	public int getColSize(int columnIndex) {
		switch (columnIndex) {
			case 0: return 16;
			case 1: return 8;
			case 2: return 50;
			case 5: return 20;
			default: return 12;
		}
	}

	/** Die Spalten-Nummern, die eine Summen enthalten sollen. */
	@Override
	public boolean getColSumme(int columnIndex) {
		if (columnIndex == 5) return true;
		else return false;
	}

	/** Die Spalten, die rechtsbündig gedruckt werden.
	 *  Zahlen werden automatisch rechtsbündig gedruckt,
	 *  hier angeben, wenn Ueberschrift auch rechtsbündig sein soll */
	@Override
	public boolean getColRight(int columnIndex) {
		if ((columnIndex == 1) || (columnIndex >= 3)) return true;
		return false;
	}

	/** Die überschrift einer Spalte der Liste. */
	@Override
	public String getColName(int columnIndex) {
		return mBuchungTable.getModel().getColumnName(columnIndex);
	}

}///endOfJournalPrinterModel

}//endOfJournalPrinter