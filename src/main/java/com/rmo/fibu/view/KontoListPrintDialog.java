package com.rmo.fibu.view;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.BuchungOfKontoModel;
import com.rmo.fibu.model.BuchungOfKontoModelNormal;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.KontoListPrinter;
import com.rmo.fibu.util.KontoListPrinterModel;
import com.rmo.fibu.util.Trace;

/** Druckt die gesamte Liste aller Konti, zeigt den Fortschritt an.
 */
public class KontoListPrintDialog extends JDialog
{
	private static final long serialVersionUID = -7874240576377106790L;
	/** Anzeige des Fortschrittes */
    JProgressBar jProgressBar = new JProgressBar();
    JButton btnCancel = new JButton();
    JButton btnPrint = new JButton();
	KontoPrinterModel printerModel;

/** Konstruktor */
public KontoListPrintDialog(Frame owner, String title, boolean modal) {
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

/** Initialisierung der Elemente */
private void jbInit() throws Exception {
    this.getContentPane().setLayout(null);

    btnPrint.setFont(Config.fontTextBold);
    btnPrint.setText("drucken");
    btnPrint.setBounds(new Rectangle(10, 43, 98, 34));
    btnPrint.addActionListener(new java.awt.event.ActionListener() {
        @Override
		public void actionPerformed(ActionEvent e) {
            btnPrint_actionPerformed(e);
        }
    });

    btnCancel.setFont(Config.fontTextBold);
        btnCancel.setText("Abbrechen");
    btnCancel.setBounds(new Rectangle(113, 43, 98, 34));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                btnCancel_actionPerformed(e);
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
	KontoListPrinter lPrinter = new KontoListPrinter(new KontoPrinterModel() );
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
private class KontoPrinterModel implements KontoListPrinterModel
{
	/** Das Model zur Tabelle */
	private BuchungOfKontoModel mBuchungTable;
	private KontoData mKontoData;
	/** Das aktuelle Konto in der Tabelle */
	private	int					mActualKontoNr = 0;

	public KontoPrinterModel() {
		mBuchungTable = new BuchungOfKontoModelNormal();
		mKontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);
	}

	/** Der Iterator, der verwendet werden muss
	 * um über alle Konti zu Iterieren*/
//	public Iterator<Konto> getIterator() {
//		return mKontoData.getIterator();
//	}


	/** Die Anzahl Kopfzeilen */
	@Override
	public int getHeaderCount() {
		return 1;
	}

	/** Die Kopfzeile, wir linksbündig angezeigt.
	 * @param kontoNr die gewählte Kontonummer
	 *  @param number die Zeilennummer
	 * */
	@Override
	public String getKontoName(int kontoNr, int number) {
		Konto lKonto = null;
		try {
			lKonto = mKontoData.read(kontoNr);
		}
		catch (KontoNotFoundException ex) {
			Trace.println(0, "Error in KontoListPrintDialog:" + ex.getMessage());
		}
		return lKonto.getKontoNrAsString() + "  " + lKonto.getText();
	}

	/** Die Anzahl Spalten */
	@Override
	public int getColCount() {
		return 7;
	}

	/** Die relative Spaltenbreiten, diese werden der Seitenbreite angepasst. */
	@Override
	public int getColSize(int columnIndex) {
		switch (columnIndex) {
			case 0: return 16;
			case 1: return 10;
			case 2: return 50;
			case 3: return 10;
			default: return 20;
		}
	}

	/** Die Spalten-Nummern, die eine Summen enthalten sollen. */
	@Override
	public boolean isColToAdd(int columnIndex) {
		if (columnIndex == 4 || columnIndex == 5) return true;
		else return false;
	}

	/** Die Spalten, die rechtsbündig gedruckt werden.
	 *  Zahlen werden automatisch rechtsbündig gedruckt,
	 *  hier angeben, wenn Ueberschrift auch rechtsbündig sein soll */
	@Override
	public boolean getColRight(int columnIndex) {
		if ((columnIndex == 1) || (columnIndex >= 4)) return true;
		return false;
	}

	/** Die überschrift einer Spalte der Liste. */
	@Override
	public String getColName(int columnIndex) {
		return mBuchungTable.getColumnName(columnIndex);
	}

	/** Die Anzahl Zeilen eines Kontos
	 * @param kontoNr die gewählte Kontonummer
	*/
	@Override
	public int getRowCount(int kontoNr) {
		return getBuchungTable(kontoNr).getRowCount();
	}

	/** Der Wert einer Zelle.
	 * @param row Zeile
	 * @param col Spalte
	 * @return Wert der Zelle */
	@Override
	public Object getValueAt(int kontoNr, int row, int col) {
		return getBuchungTable(kontoNr).getValueAt(row, col);
	}

	/** Setzt das BuchungsModel auf das geforderte Konto
	 * falls nicht identisch mit dem Vorgänger.
	 * @param kontoNr
	 * @return
	 */
	private BuchungOfKontoModel getBuchungTable(int kontoNr) {
		if (kontoNr != mActualKontoNr) {
			mBuchungTable.setup(kontoNr, Config.sDatumVon);
			mActualKontoNr = kontoNr;
		}
		return mBuchungTable;
	}

}///endOfJournalPrinterModel

}//endOfJournalPrinter