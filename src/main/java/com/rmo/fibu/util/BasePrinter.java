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

import javax.swing.table.TableModel;

/**
 * Druckt fortlaufende Seiten einer Tabelle. Die Daten müssen über das Interface
 * BasePrinterModel gesetzt werden. Das drucken wird mit der Methode print()
 * ausgelöst. Der BasePrinter kontrolliert die y-Position, wenn das Ende der
 * Seite erreicht ist, werden die Summen ausgegeben.
 */
public abstract class BasePrinter implements Printable {
	// Das Model zum BasePrinter
	protected BasePrinterModel printerModel;
	/** das Model der Tabelle, immer über getTabelModel lesen */
	protected TableModel mTableModel;
	/** Die Seiten-Nummer ab der gedruckt wird */
	private int mPageIndex;
	/** Die kontrolle der Seite */
	private int mPageLast = -1;
	/** Anzahl Zeilen, die bereits gedruckt wurden */
	protected int mPrintedRows;
	/** True, wenn alles gedruckt wurden */
	protected boolean mPrintedAll = false;

	/**
	 * Die Y-Positon der letzten gedruckten Zeile muss vor dem drucken einer Zeile
	 * gesetzt werden
	 */
	protected float yPos;
	/** Die Seiten-Masse */
	protected float pageWidth;
	protected double pageHeight;
	// --- Werte der Tabelle
	/** Der Array mit den Summen */
	protected double[] mColSumme;
	/** Die genaue Start-Position jeder Spalte (0..n) */
	private double[] colStartX;
	/** Format für Beträge */
	private DecimalFormat mDecimalFormat = new DecimalFormat("###,###,##0.00");

	/** Konstruktor mit TableModel für die Datenabfrage */
//	public BasePrinter(BasePrinterModel theModel) {
//		this.printerModel = theModel;
//	}

	/***********************************************************************
	 * Wird von der Klasse aufgerufen, die eine Tablle drucken will. JTable,
	 * Kopfzeile etc. werden über TabelPrinterModel gesetzt (siehe Konstruktor).
	 *
	 * @throws Exception, falls Fehler beim Drucken.
	 */
	public void doPrint() throws PrinterException {
		Trace.println(3, "BasePrinter.doPrint()");
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
	 * muss jede einzelne Seite unabhöngig berechnen, bzw. ausgeben können.
	 *
	 * @param g          Graphics umgebung
	 * @param mPageIndex Seite die gedruckt werden soll
	 * @return Printable.PAGE_EXISTS wenn gedruckt, sonst Printable.NO_SUCH_PAGE
	 */
	@Override
	public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
		Trace.println(3, "BasePrinter.print(page: " + pageIndex + ")");

		Graphics2D g2 = (Graphics2D) g;
		g2.translate(pf.getImageableX(), pf.getImageableY());
		this.mPageIndex = pageIndex;
		if (pageIndex == 0) {
			// fuer die schleppenden Summen
			mColSumme = new double[printerModel.getColCount()];
			// absulute Cols berechnen
			setColStartX(g2, pf);
//			int lPageIndex = 0;
			mPrintedRows = 0;
		}

		// wenn alles gedruckt
		if (mPrintedAll) {
			return Printable.NO_SUCH_PAGE;
		}

		if (pageIndex > mPageLast) {
			// wenn eine Seite das erstemal aufgerufen, dann nicht
			mPageLast = pageIndex;
			return Printable.PAGE_EXISTS;
		}

		// ----- die Seite drucken
		printPage(g2, pageIndex, true);
		return Printable.PAGE_EXISTS;
	}

	/**
	 * Eine Seite drucken. Die Methode muss selber wissen was wann noch fortgesetzt
	 * werden soll.
	 *
	 * @param g
	 * @param pageIndex
	 * @param printing
	 */
	abstract protected void printPage(Graphics2D g, int pageIndex, boolean printing);

	/**
	 * Kopfzeile drucken, bei der ersten Kopfzeile rechts die Seitenzahl Wenn not
	 * printing, wird nur die Position von yPos verändert (für die Berechnung).
	 */
	protected void printHeader(Graphics2D g, boolean printing) {
		if (!printing) {
			return;
		}
		// erste Position auf der Seite
		yPos = Config.printerNormalFont.getSize2D();

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
	 * Den Titel ausgeben, wie "Bilanz per ..."
	 *
	 * @param g
	 * @param printing
	 */
	protected void printTitle(Graphics2D g, boolean printing) {
		// doppelter Zeilenabstand
		yPos += spaceForOneRow() * 3;

		if (printing) {
			// Titel Text
			g.setFont(Config.printerTitelFont);
			g.drawString(printerModel.getTitle(), 0, yPos);
		}
		yPos += spaceForOneRow()*2;

	}

	/**
	 * Die überschriften über die Tabelle
	 *
	 * @param g
	 * @param printing
	 */
	protected void printTableHeader(Graphics2D g, boolean printing) {
		// doppelter Zeilenabstand
		if (printing) {
			// die Header der Columns
			g.setFont(Config.printerNormalFont);
			for (int i = 0; i < printerModel.getColCount(); i++) {
				printCelleText(g, i, printerModel.getColName(i), printerModel.getColRight(i));
			}
			// Linie
			g.drawLine(0, (int) yPos + 4, (int) pageWidth, (int) yPos + 4);
		}
		yPos += Config.printerHeaderAbstand;
	}

	/** Eine Zeile drucken. */
	protected void printTableRow(Graphics2D g, int rowNr) {
		for (int i = 0; i < getTableModel().getColumnCount(); i++) {
			printCelle(g, rowNr, i);
		}
	}

	/**
	 * Eine Zelle drucken, der Inhalt wird vom Tabelmodell gelesen. Wenn _ dann
	 * Linie, wenn = dann DoppelLinie
	 */
	/** Eine Zelle drucken, Values in Text umwandlen */
	private void printCelle(Graphics2D g, int rowNr, int colNr) {
		Object value = getTableModel().getValueAt(rowNr, colNr);
		String text = null;
		boolean printRight = printerModel.getColRight(colNr);

		// --- Werte formatieren
		if (value instanceof Double) {
			// wenn -1 dann nix anzeigen
			if (((Double) value).doubleValue() == -1) {
				text = "";
			} else {
				text = mDecimalFormat.format(value);
			}
//		printRight = true;
		} else if (value instanceof Integer) {
			// wenn 0 dann nix anzeigen
			if (((Integer) value).intValue() == 0) {
				text = "";
			} else {
				text = value.toString();
			}
//		printRight = true;
		} else if (value instanceof Date) {
			text = DatumFormat.getDatumInstance().format(value);
		} else {
//		printRight = printerModel.getColRight(colNr);
			if (value != null) {
				text = value.toString();
			}
		}
		if (text == null || text.length() < 1) {
			return;
		}
		// --- Eine Linie drucken
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

	/**
	 * Den Inhalt der Zelle ausgeben
	 */

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
				if (printRight) {
					text = text.substring(1);
				} else {
					text = text.substring(0, text.length() - 1);
				}
			}
			if (printRight) {
				text = fillStr + text;
			} else {
				text = text + fillStr;
			}
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
	 * Die Summe am Ende der Seite
	 */
	protected void printZwischenSumme(Graphics2D g, boolean printing) {
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
						g.setFont(Config.printerFontBold);
						String text = Config.sDecimalFormat.format(mColSumme[colNr]);
						printCelleText(g, colNr, text, true);
					}
				}
			}
		}
	}


	/** Die Summe am Ende der Seite */
	protected void printSummeFooter(Graphics2D g, boolean printing) {
		yPos += spaceSummeFooter();
		if (printing && isInFooter(yPos)) {
			for (int colNr = 0; colNr < printerModel.getColCount(); colNr++) {
				if (printerModel.getColSumme(colNr)) {
					// Linie zeichnen -2 punkte über Text
					int yLine = (int) yPos - (int) Config.printerNormalFont.getSize2D() - 2;
					g.drawLine((int) (colStartX[colNr] + Config.printerColAbstand), yLine,
							(int) (colStartX[colNr + 1] - Config.printerColAbstand), yLine);
					// Summe ausgeben
					String text = mDecimalFormat.format(mColSumme[colNr]);
					printCelleText(g, colNr, text, true);
				}
			}
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


	/**
	 * berechnet die Startposition einer Spalte For Convenience: es wird eine Spalte
	 * mehr als vorhanden berechnet
	 */
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
		colStartX = new double[printerModel.getColCount() + 1];
		colStartX[0] = 0;
		for (int i = 1; i < printerModel.getColCount(); i++) {
			colStartX[i] = colStartX[i - 1] + (printerModel.getColSize(i - 1) * faktor);
		}
		// die letzte Position, für einfacher Iteration
		colStartX[printerModel.getColCount()] = pageWidth;
	}

	/**
	 * Die Summe am Ende der Seite
	 */
	protected void addSumme() {
		for (int i = 0; i < printerModel.getColCount(); i++) {
			if (printerModel.getColSumme(i)) {
				try {
					double lBetrag = ((Double) getTableModel().getValueAt(mPrintedRows, i)).doubleValue();
					if (lBetrag > 0) {
						// negativer Betrag nicht erlaubt, nicht gesetzter Betrag wird mit -1
						// initialisiert
						mColSumme[i] += lBetrag;
					}
				} catch (ClassCastException e) {
					// kann sein, dass keine Zahlen enthält
					Trace.println(4, "BasePrinter.addSumme() ClassCastException: "
							+ getTableModel().getValueAt(mPrintedRows, i).toString());
				}
			}
		}
	}

//------- helper Methoden ------------------------------------------------
	/** Lasy read des Models der Tabelle */
	protected TableModel getTableModel() {
		if (mTableModel == null) {
			mTableModel = printerModel.getTableToPrint().getModel();
		}
		return mTableModel;
	}

	/** Bereich in Pixels für eine Zeile */
	protected float spaceForOneRow() {
		return (Config.printerNormalFont.getSize2D() + Config.printerRowAbstand);
	}

	/** Bereich in Pixels für Summe und Footer */
	protected float spaceSummeFooter() {
		return (Config.printerNormalFont.getSize2D() + Config.printerSummeAbstand);
	}

	/**
	 * Berechnet, ob die yPos im Footerbereich ist.
	 *
	 * @param float yPos die position
	 * @return true wenn im footerbereich, sonst false
	 */
	protected boolean isInFooter(float yPos) {
		return (yPos > (pageHeight - spaceSummeFooter()));
	}

}// endOfClass