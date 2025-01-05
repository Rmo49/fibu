package com.rmo.fibu.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Date;

import com.rmo.fibu.model.KontoNrVector;

/**
 * Druckt fortlaufende Seiten einer Tabelle. Die Daten müssen über das Interface
 * TablePrinterModel gesetzt werden. Das drucken wird mit der Methode print()
 * ausgelöst. Der TablePrinter kontrolliert die y-Position, wenn das Ende der
 * Seite erreicht ist, werden die Summen ausgegeben.
 */
public class KontoListPrinter implements Printable {
	// Das Model, (Tabelle)
	private KontoListPrinterInterface printerModel;
	/** Der Vecort mit den Kontonummern */
	private KontoNrVector mKontoNrList;
	/** Die nummer im Vector */
	private int mListNr;
	/** Die aktuelle Kontonummer die gedruckt wird */
	private int mKontoNr;
	/** True, wenn das Konto vollständig gedruckt wurde */
	private boolean mKtoAllesGedrucked;
	/** Die Seite des Kontos */
	private int mKontoPage;
	/** Zeigt an, ob die Seite vollständig gedruckt wurde */
	private boolean mPageFull;
	/** True, wenn alle Konto gedruckt wurden */
	private boolean mAllKontoPrinted;
	/** Anzahl Zeilen eines Kontos, die bereits gedruckt wurden */
	private int mPrintedRows;
	/** Die Seiten-Nummer ab der gedruckt wird */
	private int mPageIndex;
	/** Die kontrolle der Seite */
	private int mPageLast = -1;
	/**
	 * Die Y-Positon der zu druckenden Zeile muss vor dem drucken einer Zeile
	 * gesetzt werden
	 */
	private float yPos;
	/** Die Seiten-Masse */
	private float pageWidth;
	private float pageHeight;
	// --- Werte der Tabelle
	/** Der Array mit den Summen */
	private double[] mColSumme;
	/** Die genaue Start-Position jeder Spalte (0..n) */
	private double[] colStartX;

	/** Konstruktor mit TableModel für die Datenabfrage */
	public KontoListPrinter(KontoListPrinterInterface theModel) {
		this.printerModel = theModel;
		mKontoNrList = new KontoNrVector();
	}

	/***********************************************************************
	 * Wird von der Klasse aufgerufen, die eine Tablle drucken will. JTable,
	 * Kopfzeile etc. werden über TabelPrinterModel gesetzt (siehe Konstruktor).
	 * 
	 * @throws Exception, falls Fehler beim Drucken.
	 */
	public void doPrint() throws PrinterException {
		Trace.println(3, "KontoListPrinter.doPrint()");
		PrinterJob printJob = PrinterJob.getPrinterJob();
		PageFormat pf = new PageFormat();
		// Seiteneinstellungen aus Config setzen
		Paper paper = new Paper();
		paper.setSize(Config.printerPaperSizeWidth, Config.printerPaperSizeHeigth); // A4
		paper.setImageableArea(Config.printerRandLinks, Config.printerRandOben, Config.printerPageWidth,
				Config.printerPageHeight);
		pf.setPaper(paper);
		// pf = printJob.pageDialog(pf);
		// die Eingabe validieren mit den Druckereinstellungen
		pf = printJob.validatePage(pf);
		printJob.setPrintable(this, pf);
		// Print-Dialog aufrufen und drucken
		if (printJob.printDialog()) {
			// drucken starten, ruft print() von dieser Klasse
			printJob.print();
		}
	}

	/**
	 * Wird vom PrintHandler aufgerufen solange bis NO_SUCH_PAGE zurückgegeben wird.
	 * JTable, Kopfzeile etc. werden über TabelPrinterModel gesetzt. Die Methode
	 * muss jede einzelne Seite unabhängig berechnen, bzw. ausgeben können.
	 * 
	 * @param g         Graphics umgebung
	 * @param pageIndex Seite die gedruckt werden soll
	 * @return Printable.PAGE_EXISTS wenn gedruckt, sonst Printable.NO_SUCH_PAGE
	 */
	@Override
	public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
		Trace.println(3, "KontoListPrinter.print(page: " + pageIndex + ")");

		Graphics2D g2 = (Graphics2D) g;
		g2.translate(pf.getImageableX(), pf.getImageableY());
		this.mPageIndex = pageIndex;
		mPageFull = false;
		if (pageIndex == 0) {
			// absulute Cols berechnen
			calculateColStart(pf);
//			mPageLast = -1;
			mListNr = 0;
			mPrintedRows = 0;
			mKtoAllesGedrucked = true;
			mAllKontoPrinted = false;
		}

		// wenn alles gedruckt
		if (mAllKontoPrinted) {
			return Printable.NO_SUCH_PAGE;
		}

		if (pageIndex > mPageLast) {
			// wenn eine Seite das erstemal aufgerufen, dann nicht
			mPageLast = pageIndex;
			return Printable.PAGE_EXISTS;
		}

		// ----- drucken
		printPage(g2, true);
		return Printable.PAGE_EXISTS;
	}

	/** Eine Seite drucken, jede Zeile setzt seine eigene Position in yPos */
	private void printPage(Graphics2D g2, boolean printing) {
		Trace.println(4, "KontoListPrinter.printPage(" + mPageIndex + " printing: " + printing + " )");

		// erste Position auf der Seite
		yPos = Config.printerNormalFont.getSize2D();
		printPageHeader(g2, printing);
		yPos += spaceForOneRow() * 3;

		// durchlaufen bis eine Seite gedruckt ist
		while (!mPageFull && !mAllKontoPrinted) {
			// nächstes Konto lesen falls das letzte gedruckt wurde
			if (mKtoAllesGedrucked) {
				if (mListNr < mKontoNrList.size()) {
					mKontoNr = mKontoNrList.getAsInt(mListNr);
					mListNr++;
					mKontoPage = 1;
					mKtoAllesGedrucked = false;
					// die Summen für die Summe am ende der Page
					initSummen();
				} else {
					// abbrechen, wenn Liste durchlaufen
					mAllKontoPrinted = true;
				}
			}
			if (!mAllKontoPrinted) {
				// ein Konto durcken, oder fortsetzen falls noch nicht fertig gedruckt
				printKonto(g2, printing);
				// printFooter(g2, printing);
			}
		}
	}

	/** Die Seitennummer am Anfang der Seite */
	private void printPageHeader(Graphics2D g, boolean printing) {
		if (!printing)
			return;
		// Name der Fibu ausgeben
		g.setFont(Config.printerNormalFont);
		g.drawString(Config.sFibuTitel, 0, yPos);
		// Die Seitennummer
		String seiteNumber = "Seite: " + (mPageIndex + 1);
		int width = g.getFontMetrics().stringWidth(seiteNumber);
		g.drawString(seiteNumber, pageWidth - width, yPos);
		// --- Linie
		g.drawLine(0, (int) yPos + 4, (int) pageWidth, (int) yPos + 4);
	}

	/**
	 * Ein Konto drucken. Wenn nicht auf einer Seite möglich, dann wird
	 * mKtoAllesGedrucked auf false gesetzt.
	 */
	private void printKonto(Graphics2D g2, boolean printing) {
		// --- Kopfzeile drucken
		printKontoName(g2, printing);
		// Konto-Eintraege drucken, setzt mPageFull wenn am Ende der Seite
		printBuchungen(g2, printing);
		if (mKtoAllesGedrucked) {
			mPrintedRows = 0; // für nachstes Konto
			// noch genuegend Platz für ein weiteres Konto?
			yPos += spaceBetweenKonto();
			if ((yPos + spaceMinimumForKonto()) > pageHeight) {
				mPageFull = true;
			}
		} else {
			mKontoPage++;
//			mPageFull = true;
		}
	}

	/**
	 * Kopfzeile drucken, bei der ersten Kopfzeile rechts die Seitenzahl Wenn not
	 * printing, wird nur die Position von yPos verändert (für die Berechnung).
	 */
	private void printKontoName(Graphics2D g, boolean printing) {
		for (int i = 0; i < printerModel.getHeaderCount(); i++) {
			if (i > 0) {
				yPos += Config.printerTitelFont.getSize2D();
			}
			if (printing) {
				// Kopfzeile Text
				g.setFont(Config.printerTitelFont);
				g.drawString(printerModel.getKontoName(mKontoNr, i), 0, yPos);
				// PageNummer auf der ersten Zeile
				if (i == 0) {
					String pageNumber = "Kontoblatt: " + (mKontoPage);
					g.setFont(Config.printerNormalFont);
					int width = g.getFontMetrics().stringWidth(pageNumber);
					g.drawString(pageNumber, (int) pageWidth - width, yPos);
				}
			}
		}
		// doppelter Zeile und Zwischenabstand
		yPos += spaceForOneRow() + Config.printerRowAbstand;
		if (printing) {
			g.setFont(Config.printerNormalFont);
			for (int i = 0; i < printerModel.getColCount(); i++) {
				// --- Die Spalten-Bezeichnungen
				printCelleText(g, i, printerModel.getColName(i), printerModel.getColRight(i));
			}
			// --- Linie
			g.drawLine(0, (int) yPos + 4, (int) pageWidth, (int) yPos + 4);
		}
		yPos += Config.printerHeaderAbstand;
	}

	/**
	 * Die Buchungen eines Kontos drucken inkl. Summen am Schluss. Berechnet die
	 * schleppende Summe
	 * 
	 * @param g
	 * @param printing false wenn nur berechnet werden soll
	 */
	private void printBuchungen(Graphics2D g, boolean printing) {
		while (yPos < (pageHeight - (spaceSummeFooter() * 2))) {
			if (mKtoAllesGedrucked) {
				break;
			}
			if (mPrintedRows == printerModel.getRowCount(mKontoNr) - 1) {
				// die letzte Zeile, Summe ausgeben
				printSumme(g, printing);
			} else {
				// --- eine Zeile drucken
				yPos += spaceForOneRow();
				if (printing) {
					printRow(g);
				}
				addSumme();
				mPrintedRows++;
			}
		}
		if (isInFooter(yPos)) {
			if (!mKtoAllesGedrucked) {
				printSumme(g, printing);
			}
			mPageFull = true;
		}
		// den letzten increment korrigieren, Zeile wurde nicht gedruckt
//		yPos -= spaceForOneRow();
	}

	/** Die Summe am Ende der Seite */
	private void printSumme(Graphics2D g, boolean printing) {
		printSummeLine(g, printing);
		if (printing) {
			if (isInFooter(yPos)) {
				// wenn am Ende einer Seite, aber nicht Ende des Kontos
//			printSummeLine(g, (int) yPos - (int) Config.printerNormalFont.getSize2D() - 2);
				yPos += spaceSummeFooter();
				for (int colNr = 0; colNr < printerModel.getColCount(); colNr++) {
					if (colNr == 2) {
						printCelleText(g, colNr, "Total", false);
					}
					if (printerModel.isColToAdd(colNr)) {
						// Die schleppende Summe ausgeben
						String text = Config.sDecimalFormat.format(mColSumme[colNr]);
						printCelleText(g, colNr, text, true);
					}
				}
			} else {
				// wenn die letzte Zeile eines Kontos (Summen) gedruckt
//				if (mPrintedRows >= printerModel.getRowCount(mKontoNr)) {
				yPos += spaceSummeFooter();
				printRow(g);
				// Saldo drucken
				double soll = (Double) printerModel.getValueAt(mKontoNr, mPrintedRows, 4);
				double haben = (Double) printerModel.getValueAt(mKontoNr, mPrintedRows, 5);
				if ((soll == 0) || (haben == 0)) {
					// wenn ein Betrag 0 ist, dann keine Saldo ausgeben
				} else {
					yPos += spaceForOneRow();
					printCelleText(g, 2, "Saldo", false);
					if (soll > haben) {
						String text = Config.sDecimalFormat.format(soll - haben);
						printCelleText(g, 5, text, true);
					} else {
						String text = Config.sDecimalFormat.format(haben - soll);
						printCelleText(g, 4, text, true);
					}
				}

				// für das nächste Konto
				mKtoAllesGedrucked = true;
				mPrintedRows = 0;
			}
		} else {
			yPos += spaceSummeFooter();
		}
	}

	/**
	 * Linie, über der Spalten mit Summen
	 * 
	 * @param printing wenn gedrucked wird
	 */
	private void printSummeLine(Graphics2D g, boolean printing) {
		yPos += 2;
		if (printing) {
			int yLine = (int) yPos;
			for (int colNr = 0; colNr < printerModel.getColCount(); colNr++) {
				if (printerModel.isColToAdd(colNr)) {
					// Linie zeichnen -2 punkte über Text
					g.drawLine((int) (colStartX[colNr] + Config.printerColAbstand), yLine,
							(int) (colStartX[colNr + 1] - Config.printerColAbstand), yLine);
				}
			}
		}
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
		// --- Werte formatieren
		if (value instanceof Double) {
			// wenn -1 dann nix anzeigen
			if (((Double) value).doubleValue() == -1) {
				text = "";
			} else {
				text = Config.sDecimalFormat.format(value);
			}
			printRight = true;
		} else if (value instanceof Integer) {
			// wenn 0 dann nix anzeigen
			if (((Integer) value).intValue() == 0) {
				text = "";
			} else {
				text = value.toString();
			}
			printRight = true;
		} else if (value instanceof Date) {
			text = DatumFormat.getDatumInstance().format(value);
		} else {
			printRight = printerModel.getColRight(colNr);
			text = value.toString();
		}
		if (text == null || text.length() < 1)
			return;
		// --- Zelle ausgeben
		if (text.charAt(0) == '_') {
			printCelleLine(g, colNr);
			return;
		}
		if (text.charAt(0) == '=') {
			printCelleDoubleLine(g, colNr);
			return;
		}
		printCelleText(g, colNr, text, printRight);
	}

	/** Den Textinhalt einer Zelle drucken */
	private void printCelleText(Graphics2D g, int colNr, String text, boolean printRight) {
		String fillStr = "..";
		// wenn String zu lang, diesen schneiden
		double allowedWidth = (colStartX[colNr + 1] - colStartX[colNr]);
		// Breite berechnen
		allowedWidth -= Config.printerColAbstand;
		if (g.getFontMetrics().getStringBounds(text, g).getWidth() > allowedWidth) {
			// wenn zu breit, Text verkürzen
			allowedWidth -= g.getFontMetrics().getStringBounds(fillStr, g).getWidth();
			while (g.getFontMetrics().getStringBounds(text, g).getWidth() > allowedWidth) {
				if (printRight)
					text = text.substring(1);
				else
					text = text.substring(0, text.length() - 1);
			}
			if (printRight)
				text = fillStr + text;
			else
				text = text + fillStr;
		}
		// X-Position berechnen
		double xPos = colStartX[colNr] + Config.printerColAbstand;
		// X-Position für rechts berechnen
		if (printRight) {
			Rectangle2D rect = g.getFontMetrics().getStringBounds(text, g);
			xPos = colStartX[colNr + 1] - rect.getWidth() - Config.printerColAbstand;
		}
		g.drawString(text, (float) xPos, yPos);
	}

	/** Eine Line in der Zelle drucken */
	private void printCelleLine(Graphics2D g, int colNr) {
		// Linie zeichnen -5 punkte über Text
		int yLine = (int) yPos - 5;
		g.drawLine((int) (colStartX[colNr] + Config.printerColAbstand), yLine,
				(int) (colStartX[colNr + 1] - Config.printerColAbstand), yLine);
	}

	/** Eine Doppel-Line in der Zelle drucken */
	private void printCelleDoubleLine(Graphics2D g, int colNr) {
		// Linie zeichnen -6 punkte über Text
		int yLine = (int) yPos - 6;
		g.drawLine((int) (colStartX[colNr] + Config.printerColAbstand), yLine,
				(int) (colStartX[colNr + 1] - Config.printerColAbstand), yLine);
		yLine += 2;
		g.drawLine((int) (colStartX[colNr] + Config.printerColAbstand), yLine,
				(int) (colStartX[colNr + 1] - Config.printerColAbstand), yLine);
	}

	/**
	 * Die Seitennummer am Ende der Seite private void printFooter(Graphics2D g,
	 * boolean printing) { if (!printing) return; String seiteNumber = "Seite: " +
	 * (mPageIndex+1); g.setFont( Config.printerNormalFont ); int width =
	 * g.getFontMetrics().stringWidth(seiteNumber); g.drawString(seiteNumber,
	 * pageWidth-width, pageHeight-2); }
	 */

	/**
	 * Die schleppende Summe, die evt. angezeigt werden muss.
	 * 
	 */
	private void addSumme() {
		for (int i = 0; i < printerModel.getColCount(); i++) {
			if (printerModel.isColToAdd(i)) {
				try {
					double lBetrag = ((Double) printerModel.getValueAt(mKontoNr, mPrintedRows, i)).doubleValue();
					if (lBetrag > 0) {
						// negativer Betrag nicht erlaubt, nicht gesetzter Betrag wird mit -1
						// initialisiert
						mColSumme[i] += lBetrag;
					}
				} catch (ClassCastException e) {
					// kann sein, dass keine Zahlen enthält
					Trace.println(4, "TablePrinter.addSumme() ClassCastException: "
							+ printerModel.getValueAt(mKontoNr, mPrintedRows, i).toString());
				}
			}
		}
	}

	/**
	 * Für die schleppende Summe
	 */
	private void initSummen() {
		if (mColSumme != null) {
			mColSumme = null;
		}
		mColSumme = new double[printerModel.getColCount()];
	}

	/**
	 * Berechnet die Startposition einer Spalte. For Convenience: es wird eine
	 * Spalte mehr als vorhanden berechnet
	 */
	private void calculateColStart(PageFormat pf) {
		// Den Printbereich setzten
		pageWidth = (float) pf.getImageableWidth();
		pageHeight = (float) pf.getImageableHeight();
		// Die Summe der relativen Spaltenbreiten
		int summe = 0;
		for (int i = 0; i < printerModel.getColCount(); i++) {
			summe += printerModel.getColSize(i);
		}
		float faktor = pageWidth / summe;
		colStartX = new double[printerModel.getColCount() + 1];
		// die erste Spalte auf pos 0
		colStartX[0] = 0;
		for (int i = 1; i < printerModel.getColCount(); i++) {
			colStartX[i] = colStartX[i - 1] + (printerModel.getColSize(i - 1) * faktor);
		}
		// die letzte Position, für einfacher Iteration
		colStartX[printerModel.getColCount()] = pageWidth;
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

	/**
	 * Bereich zwischen 2 Kontos Titel + Anzahl Zeilen
	 */
	private float spaceMinimumForKonto() {
		return (2 * Config.printerTitelFont.getSize2D()) + (4 * spaceForOneRow());
	}

	/**
	 * Berechnet, ob die yPos im Footerbereich ist.
	 * 
	 * @param float yPos die position
	 * @return true wenn im footerbereich, sonst false
	 */
	private boolean isInFooter(float yPos) {
		return (yPos > (pageHeight - (spaceSummeFooter() * 2)));
	}

}// endOfClass