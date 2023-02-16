package com.rmo.fibu.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
* Überschreibt die Methode writeString und speichert den Text in pdfWords.
* Zu jeden Wort (oder Wortgruppe) wird die X- und Y-Position gespreichert.
* Zusammenhängende Wörter haben immer die gleiche X-Position.
*/
public class PdfWordStripper extends PDFTextStripper {

	/**
	 * Worte werden in der Form:
	 * Kartenlimite 66 442 <= wenn gleiche Zahlen, dann ein zusammenhängender Eintrag
	 * CHF 66 442
	 * 7'500 66 442
	 * 03.01.23 66 457 <= das ist ein neues Wort auf einer neuen Zeile
	 */
    public List<PdfWordLocation> pdfWords = new ArrayList<>();

    public PdfWordStripper() throws IOException {
    }

    /**
     * Override the default functionality of PDFTextStripper.writeString()
     */
    @Override
    protected void writeString(String str, List<TextPosition> textPositions) throws IOException {
        String[] wordsInStream = str.split(getWordSeparator());
        if(wordsInStream!=null){
            for(String word :wordsInStream){
            	PdfWordLocation pwl = new PdfWordLocation();
            	pwl.word = word;
               	pwl.posX = (int) textPositions.get(0).getXDirAdj();
               	pwl.posY = (int) textPositions.get(0).getYDirAdj();
               	pdfWords.add(pwl);
            }
        }
    }

}
