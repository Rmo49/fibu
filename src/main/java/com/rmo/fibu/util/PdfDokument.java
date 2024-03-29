package com.rmo.fibu.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Enthät alle Worte eines PDF-Domumentes.
 * Das PDF-Dokument wird im Konstruktor übergeben als Liste aller Worte,
 * mit x und y Koordinaten
 * @author ruedi
 *
 */
public class PdfDokument {

	// alle Worte des PDF-Dokuments
	private List<PdfWordLocation> allWords;
	// der Iterator über alle Worte des Dokumentes, wird von mehreren Methoden verwendet
	private Iterator<PdfWordLocation> iterAllWords = null;
	// die zuletzt gelesene Zeile
	private int lastY = 0;
	// das zuletzt gelesene Wort
	private PdfWordLocation pdfWordLast;
	// eine Zeile der "Tabelle" nach spalten
//	private static List <String> pdfZeile = new ArrayList<String>();

	public static List<List<String>> pdfZeilenListe = new ArrayList<>();


	/**
	 * Im Konstruktor wird das PDF-Dokument übergeben.
	 * @param pdfWords
	 */
	public PdfDokument(List<PdfWordLocation> pdfWords) {
		this.allWords = pdfWords;
	}


	/**
	 * Initialisiert den Iterator, setzt diesen auf die Position des Wortes
	 * @param iter
	 * @param suchWort
	 * @return yPos des Wortes, oder -1 wenn nicht gefunden
	 */
	public void gotoStart(String suchWort) {
		iterAllWords = allWords.iterator();
		while (iterAllWords.hasNext()) {
			pdfWordLast = iterAllWords.next();
			if (pdfWordLast.word.startsWith(suchWort)) {
				lastY = pdfWordLast.posY;
				return;
			}
		}
		lastY = -1;
	}

	/**
	 * Iterator weiter bis zur nächsten Zeile
	 * @param iter
	 * @return
	 */
	public void gotoNextLine() {
		while (iterAllWords.hasNext()) {
			pdfWordLast = iterAllWords.next();
			if (pdfWordLast.posY > lastY) {
				lastY = pdfWordLast.posY;
				return;
			}
		}
		lastY = -1;
	}


	/**
	 * Liest eine Zeile auf der Position (lastY), in pdfWordLast steht bereits das erste Wort
	 * @return Liste von Worten einer xPos (auch Wortgruppe möglich)
	 */
	public List<String> nextLine() {
		StringBuffer strBuffer = new StringBuffer();
		List <String> pdfZeile = new ArrayList<>();

		// pdfWordLast enthält das ersten Wort
		int lastX = pdfWordLast.posX;
		while (iterAllWords.hasNext()) {
			if ((pdfWordLast.posX != lastX) || (pdfWordLast.posY != lastY)) {
				// neues Wort oder neue Zeile
				// zuerst bestehendes Wort sichern, das im Buffer liegt
				if (strBuffer.length() > 0) {
					pdfZeile.add(strBuffer.toString());
					strBuffer.setLength(0);
				}
				if (pdfWordLast.posY != lastY) {
					// nächste Zeile
					lastY = pdfWordLast.posY;
					break;
				}
				// neues Wort in Buffer
				strBuffer.append(pdfWordLast.word);
				lastX = pdfWordLast.posX;
				lastY = pdfWordLast.posY;
			}
			else {
				if (strBuffer.length() > 0) {
					// Worte trennen, nur wenn schon was im Buffer
					strBuffer.append(" ");
				}
				strBuffer.append(pdfWordLast.word);
			}
			pdfWordLast = iterAllWords.next();
		}
		return pdfZeile;
	}




	/**
	 * Die Zeile nach diesem Wort lesen
	 * @param nachDiesemWort, nach diesem Wort lesen
	 */
	public List<String> getFirstRow(String nachDiesemWort) {
		gotoStart(nachDiesemWort);
		gotoNextLine();
		return nextLine();
	}

}
