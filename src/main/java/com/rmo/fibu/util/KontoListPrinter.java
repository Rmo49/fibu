package com.rmo.fibu.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.DecimalFormat;
import java.util.Date;

import com.rmo.fibu.model.KontoNrVector;

/**
 * Druckt fortlaufende Seiten einer Tabelle.
 * Die Daten müssen über das Interface TablePrinterModel gesetzt werden.
 * Das drucken wird mit der Methode print() ausgelöst.
 * Der TablePrinter kontrolliert die y-Position,
 * wenn das Ende der Seite erreicht ist, werden die Summen ausgegeben.
 */
public class KontoListPrinter implements Printable {
	// Das Model, (Tabelle)
	private KontoListPrinterModel   printerModel;
	/** Der Vecort mit den Kontonummern */
	private KontoNrVector	mKontoNrVector;
	/** Die nummer im Vector */
	private int				mVectorNr;
	/** Die aktuelle Kontonummer die gedruckt wird */
	private int				mKontoNr;
	/** True, wenn das Konto vollständig gedruckt wurde */
	private boolean			mKontoPrinted;
	/** Die Seite des Kontos */
	private int				mKontoPage;
	/** Zeigt an, ob die Seite vollständig gedruckt wurde */
	private boolean			mPageFull;
	/** True, wenn alle Konto gedruckt wurden */
	private boolean			mAllKontoPrinted;
	/** Anzahl Zeilen eines Kontos, die bereits gedruckt wurden */
	private int             mPrintedRows;
	/** Die Seiten-Nummer ab der gedruckt wird */
	private int             mPageIndex;
	/** Die Y-Positon der letzten gedruckten Zeile
	 *  muss vor dem drucken einer Zeile gesetzt werden */
	private float           yPos;
	/** Die Seiten-Masse */
	private float			pageWidth;
	private float			pageHeight;
	//--- Werte der Tabelle
	/** Der Array mit den Summen */
	private double[]        colSumme;
	/** Die genaue Start-Position jeder Spalte (0..n) */
	private double[]         colStartX;
	/** Format für Beträge */
    private DecimalFormat   mDecimalFormat = new DecimalFormat("###,###,##0.00");


/** Konstruktor mit TableModel für die Datenabfrage */
public KontoListPrinter(KontoListPrinterModel theModel)  {
	this.printerModel = theModel;
	mKontoNrVector = new KontoNrVector();
}

/***********************************************************************
 * Wird von der Klasse aufgerufen, die eine Tablle drucken will.
 * JTable, Kopfzeile etc. werden über TabelPrinterModel gesetzt
 * (siehe Konstruktor).
 * @throws Exception, falls Fehler beim Drucken.
 */
public void doPrint() throws PrinterException {
	Trace.println(3, "TablePrinter.doPrint()");
	PrinterJob printJob = PrinterJob.getPrinterJob();
	PageFormat pf = new PageFormat();
	// Seiteneinstellungen aus Config setzen
	Paper paper = new Paper();
	paper.setSize(595.3, 841.9); // A4
	paper.setImageableArea(Config.printerRandLinks, Config.printerRandOben,
		Config.printerPageWidth, Config.printerPageHeight);
	pf.setPaper(paper);
	//pf = printJob.pageDialog(pf);
	// die Eingabe validieren mit den Druckereinstellungen
	pf = printJob.validatePage(pf);
	printJob.setPrintable(this, pf);
	// Print-Dialog aufrufen und drucken
	if (printJob.printDialog()) {
		// drucken starten, ruft print() von dieser Klasse
		printJob.print();
	}
}

/** Wird vom PrintHandler aufgerufen solange bis NO_SUCH_PAGE zurückgegeben wird.
 *  JTable, Kopfzeile etc. werden über TabelPrinterModel gesetzt.
 *  Die Methode muss jede einzelne Seite unabhängig berechnen, bzw. ausgeben können.
 * @param g Graphics umgebung
 * @param pageIndex Seite die gedruckt werden soll
 * @return Printable.PAGE_EXISTS wenn gedruckt, sonst Printable.NO_SUCH_PAGE
 */
@Override
public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException
{
	Trace.println(3, "TablePrinter.print(page: " + pageIndex +")");
	Graphics2D g2 = (Graphics2D) g;
	g2.translate(pf.getImageableX(), pf.getImageableY());
	this.mPageIndex = pageIndex;
	// fuer die schleppenden Summen
	colSumme = new double [printerModel.getColCount()];
	// absulute Cols berechnen
	setColStartX(g2, pf);
	
	int lPageIndex = 0;
	mPrintedRows = 0;
	mVectorNr = 0;
	mKontoPrinted = true;
	mAllKontoPrinted = false;
	mPageFull = false;
	while (lPageIndex < mPageIndex) {
		//--- drucken ohen ausgaben (berechnen)
		printPage(null, false);
		lPageIndex++;
		mPageFull = false;
	}
	// bereits abbrechen?
	if (mAllKontoPrinted) {
		return Printable.NO_SUCH_PAGE;
	}
	// ----- drucken
	printPage(g2, true);
	return Printable.PAGE_EXISTS;
}

/** Eine Seite drucken, jede Zeile setzt seine eigene Position in yPos */
private void printPage(Graphics2D g, boolean printing) {
	String bool = "false";
	if (printing) bool = "true";
	Trace.println(4, "KontoListPrinter.printPage(printing: " + bool +")");
	
	// erste Position auf der Seite
	yPos = Config.printerTitelFont.getSize2D();
	// durchlaufen bis alle Konto einer Seite gedruckt sind
	while (!mAllKontoPrinted && !mPageFull) {
		// nächstes Konto lesen falls das letzte gedruckt wurde
		if (mKontoPrinted) {
			if (mVectorNr < mKontoNrVector.size()) {
				mKontoNr = mKontoNrVector.getAsInt(mVectorNr);
				mVectorNr++;
				mKontoPage = 1;
				mKontoPrinted = false;
			}
			else {
				mAllKontoPrinted = true;
			}
		}
		//ein Konto durcken, oder fortsetzen falls noch nicht fertig gedruckt
		printKonto(g, printing);
		printFooter(g, printing);
	}
}

/** Ein Konto drucken. Wenn nicht auf einer Seite möglich,
 * dann wird mKontoPrinted auf false gesetzt.
 */
private void printKonto (Graphics2D g, boolean printing) {
	//--- Kopfzeile drucken
	printHeader(g, printing);
	//--- Konto-Eintraege drucken
	printTable(g, printing);
	if (mKontoPrinted) {
		mPrintedRows = 0; // für nachstes Konto
		// noch genuegend Platz für ein weiteres Konto?
		yPos += spaceBetweenKonto();
		if ((yPos + spaceMinimumForKonto()) > pageHeight) {
			mPageFull = true;
		}
	}
	else {
		//--- Zwischensumme am Ende der Seite drucken
		printSumme(g, printing);
		mKontoPage++;
		mPageFull = true;
	}
}

/** Kopfzeile drucken, bei der ersten Kopfzeile rechts die Seitenzahl
 * Wenn not printing, wird nur die Position von
 *  yPos verändert (für die Berechnung). */
private void printHeader(Graphics2D g, boolean printing) {
	for (int i = 0; i < printerModel.getHeaderCount(); i++) {
		if (i > 0) {
			yPos += Config.printerTitelFont.getSize2D();
		}
		if (printing) {
			// Kopfzeile Text
			g.setFont( Config.printerTitelFont );
			g.drawString(printerModel.getHeader(mKontoNr, i), 0, yPos);
			// PageNummer auf der ersten Zeile
			if (i == 0) {
				String pageNumber = "Kontoblatt: " + (mKontoPage);
				g.setFont( Config.printerNormalFont );
				int width = g.getFontMetrics().stringWidth(pageNumber);
				g.drawString(pageNumber, (int)pageWidth-width, yPos);
			}
		}
	}
	// doppelter Zeile und Zwischenabstand
	yPos += spaceForOneRow() + Config.printerRowAbstand;
	if (printing) {
		g.setFont( Config.printerNormalFont );
		for (int i = 0; i <  printerModel.getColCount(); i++) {
			//--- Die Spalten-Bezeichnungen
			printCelleText(g, i, printerModel.getColName(i), printerModel.getColRight(i) );
		}
		//--- Linie
		g.drawLine(0, (int)yPos+4, (int)pageWidth, (int)yPos+4);
	}
	yPos += Config.printerHeaderAbstand;
}

/** Die Tabelle drucken, muss selber merken, wann fertig ist.
 *  Berechnet die schleppende Summen und führt */
private void printTable(Graphics2D g, boolean printing) {
	yPos += spaceForOneRow();
	while (yPos < (pageHeight - spaceSummeFooter() )) {
		if ( mPrintedRows == printerModel.getRowCount(mKontoNr)-1) {
			// wenn letzte Zeile (Summe), dann Linie
			if (printing) {
				printSummeLine(g, (int) (yPos - spaceForOneRow() + 2) );
			}
			yPos += 2;
		}
		
		if ( mPrintedRows >= printerModel.getRowCount(mKontoNr)) {
			mKontoPrinted = true;
			break;
		}
		//--- eine Zeile drucken
		if (printing) printRow(g);
		addSumme();
		mPrintedRows++;
		yPos += spaceForOneRow();
	}
	// den letzten increment korrigieren, Zeile wurde nicht gedruckt
	yPos -= spaceForOneRow();
}

/** Eine Zeile drucken. */
private void printRow(Graphics2D g) {
	for (int i = 0; i < printerModel.getColCount(); i++) {
		printCelle(g, i);
	}
}

/** Eine Zelle drucken, Values in Text umwandlen */
private void printCelle(Graphics2D g, int colNr) {
	Object value = printerModel.getValueAt(mKontoNr, mPrintedRows, colNr);
    String text = null;
	boolean printRight = false;
    //--- Werte formatieren
	if (value instanceof Double) {
		// wenn -1 dann nix anzeigen
		if ( ((Double)value).doubleValue() == -1) {
			text = "";
		}
		else {
            text = mDecimalFormat.format(value);
		}
		printRight = true;
	}
	else if (value instanceof Integer) {
		// wenn 0 dann nix anzeigen
		if ( ((Integer)value).intValue() == 0) {
			text = "";
		}
		else {
		    text = value.toString();
		}
		printRight = true;
	}
	else if (value instanceof Date) {
		text = DatumFormat.getDatumInstance().format(value);
	}
    else {
		printRight = printerModel.getColRight(colNr);
        text = value.toString();
    }
	if (text == null || text.length() < 1) return;
	//--- Zelle ausgeben
	if ( text.charAt(0) == '_' ) {
		printCelleLine(g, colNr);
		return;
	}
	if ( text.charAt(0) == '=' ) {
		printCelleDoubleLine(g, colNr);
		return;
	}
	printCelleText(g, colNr, text, printRight);
}

/** Den Textinhalt einer Zelle drucken */
private void printCelleText(Graphics2D g, int colNr, String text, boolean printRight) {
	String fillStr = "..";
	// wenn String zu lang, diesen schneiden
	double allowedWidth = (colStartX[colNr+1] - colStartX[colNr]);
	// Breite berechnen
	allowedWidth -= Config.printerColAbstand;
	if (g.getFontMetrics().getStringBounds(text, g).getWidth() > allowedWidth) {
		// wenn zu breit, Text verkürzen
		allowedWidth -= g.getFontMetrics().getStringBounds(fillStr,g).getWidth();
		while (g.getFontMetrics().getStringBounds(text, g).getWidth() > allowedWidth) {
			if (printRight) text = text.substring(1);
			else text = text.substring(0,text.length()-1);
		}
		if (printRight)  text = fillStr + text;
		else    text = text + fillStr;
	}
	// X-Position berechnen
	double xPos = colStartX[colNr] + Config.printerColAbstand;
	// X-Position für rechts berechnen
	if (printRight) {
		Rectangle2D rect = g.getFontMetrics().getStringBounds(text, g);
		xPos = colStartX[colNr+1] - rect.getWidth() - Config.printerColAbstand;
	}
	g.drawString(text, (float)xPos, yPos);
}

/** Eine Line in der Zelle drucken */
private void printCelleLine( Graphics2D g, int colNr) {
	// Linie zeichnen -5 punkte über Text
	int yLine = (int)yPos - 5;
	g.drawLine((int)(colStartX[colNr] + Config.printerColAbstand), yLine,
		(int)(colStartX[colNr+1] - Config.printerColAbstand), yLine);
}

/** Eine Doppel-Line in der Zelle drucken */
private void printCelleDoubleLine( Graphics2D g, int colNr) {
	// Linie zeichnen -6 punkte über Text
	int yLine = (int)yPos - 6;
	g.drawLine((int)(colStartX[colNr] + Config.printerColAbstand), yLine,
		(int)(colStartX[colNr+1] - Config.printerColAbstand), yLine);
	yLine += 2;
	g.drawLine((int)(colStartX[colNr] + Config.printerColAbstand), yLine,
		(int)(colStartX[colNr+1] - Config.printerColAbstand), yLine);
}

/** Die Summe am Ende der Seite */
private void printSumme(Graphics2D g, boolean printing) {
	yPos += spaceSummeFooter();
	if ( printing && isInFooter(yPos) ) {
		//--- Linie zeichnen -2 punkte über Text
		printSummeLine(g, (int)yPos - (int)Config.printerNormalFont.getSize2D() - 2);
		for( int colNr = 0; colNr < printerModel.getColCount(); colNr++ ) {
			if ( printerModel.getColSumme(colNr) ) {
				// Summe ausgeben
				String text = mDecimalFormat.format(colSumme[colNr]);
				printCelleText(g, colNr, text, true);
			}
		}
	}
}

/** Strich, über der Spalten mit Summe 
 * @param yLine die y-Position der Linie */
private void printSummeLine(Graphics2D g, int yLine) {
	for( int colNr = 0; colNr < printerModel.getColCount(); colNr++ ) {
		if ( printerModel.getColSumme(colNr) ) {
			// Linie zeichnen -2 punkte über Text
			g.drawLine((int)(colStartX[colNr] + Config.printerColAbstand), yLine,
				(int)(colStartX[colNr+1] - Config.printerColAbstand), yLine);
		}
	}
}
/** Die Seitennummer am Ende der Seite */
private void printFooter(Graphics2D g, boolean printing) {
	if (!printing) return;
	String seiteNumber = "Seite: " + (mPageIndex+1);
	g.setFont( Config.printerNormalFont );
	int width = g.getFontMetrics().stringWidth(seiteNumber);
	g.drawString(seiteNumber, pageWidth-width, pageHeight-2);
}

/** Die Summe am Ende der Seite */
private void addSumme() {
	for (int i = 0; i < printerModel.getColCount(); i++) {
		if (printerModel.getColSumme(i)) {
			try {
				double lBetrag = ((Double)printerModel.getValueAt(mKontoNr, mPrintedRows, i)).doubleValue();
				if (lBetrag > 0) {
					// negativer Betrag nicht erlaubt, nicht gesetzter Betrag wird mit -1 initialisiert
					colSumme[i] += lBetrag;
				}
			}
			catch (ClassCastException e) {
				// kann sein, dass keine Zahlen enthält
				Trace.println(4, "TablePrinter.addSumme() ClassCastException: "
					+ printerModel.getValueAt(mKontoNr, mPrintedRows, i).toString() );
			}
		}
	}
}

/** berechnet die Startposition einer Spalte
 *  For Convenience: es wird eine Spalte mehr als vorhanden berechnet */
private void setColStartX(Graphics2D g, PageFormat pf) {
	// Den Printbereich setzten
	pageWidth = (float) pf.getImageableWidth();
	pageHeight = (float) pf.getImageableHeight();
	// Die Summe der relativen Spaltenbreiten
	int summe = 0;
	for (int i = 0; i < printerModel.getColCount(); i++) {
		summe += printerModel.getColSize(i);
	}
	float faktor = pageWidth / summe;
	colStartX = new double[printerModel.getColCount()+1];
	colStartX[0] = 0;
	for (int i = 1; i <  printerModel.getColCount(); i++) {
		colStartX[i] = colStartX[i-1] + (printerModel.getColSize(i-1) * faktor);
	}
	// die letzte Position, für einfacher Iteration
	colStartX[ printerModel.getColCount()] = pageWidth;
}


//------- helper Methoden ------------------------------------------------

/** Bereich in Pixels für eine Zeile */
private float spaceForOneRow() {
	return (Config.printerNormalFont.getSize2D() + Config.printerRowAbstand);
}

/** Bereich in Pixels für Summe und Footer */
private float spaceSummeFooter() {
	return (Config.printerNormalFont.getSize2D() + Config.printerSummeAbstand);
}

/** Bereich zwischen 2 Kontos */
private float spaceBetweenKonto() {
	return spaceForOneRow() * 5;
}

/** Bereich zwischen 2 Kontos 
 * Titel + Anzahl Zeilen */
private float spaceMinimumForKonto() {
	return (2*Config.printerTitelFont.getSize2D()) + (4*spaceForOneRow());
}


/** Berechnet, ob die yPos im Footerbereich ist.
 *  @param float yPos die position
 *  @return true wenn im footerbereich, sonst false
 */
private boolean isInFooter(float yPos) {
	return ( yPos > (pageHeight - spaceSummeFooter()) );
}

}//endOfClass