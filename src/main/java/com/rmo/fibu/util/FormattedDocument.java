package com.rmo.fibu.util;

import java.awt.Toolkit;
import java.text.Format;
import java.text.ParseException;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/** Erlaubt nur, dass Wert mit einem bestimmten Format eingegeben werden.
 */
public class FormattedDocument extends PlainDocument {
	private static final long serialVersionUID = 8683120417854475257L;
	private Format format;

	/** Konstruktor mit dem format das angewendet werden soll */
	public FormattedDocument(Format f) {
		format = f;
	}

	public Format getFormat() {
		return format;
	}

	/** Ueberschriebene Methode, das erwartete Resultat wird zuerst geparst
	 *  durch das beim Konstruktor angegebene Format-Objekt
	 */
	@Override
	public void insertString(int offs, String str, AttributeSet a)
		throws BadLocationException {

		String currentText = getText(0, getLength());
		String beforeOffset = currentText.substring(0, offs);
		String afterOffset = currentText.substring(offs, currentText.length());
		String proposedResult = beforeOffset + str + afterOffset;

		try {
			format.parseObject(proposedResult);
			super.insertString(offs, str, a);
		} catch (ParseException e) {
			//Toolkit.getDefaultToolkit().beep();
			//System.err.println("insertString: could not parse: " + proposedResult);
		}
	}

	/** Ueberschriebene Methode
	 *  @see insertString()
	 */
	@Override
	public void remove(int offs, int len) throws BadLocationException {
		String currentText = getText(0, getLength());
		String beforeOffset = currentText.substring(0, offs);
		String afterOffset = currentText.substring(len + offs,
												   currentText.length());
		String proposedResult = beforeOffset + afterOffset;

		try {
			if (proposedResult.length() != 0)
				format.parseObject(proposedResult);
			super.remove(offs, len);
		} catch (ParseException e) {
			Toolkit.getDefaultToolkit().beep();
			System.err.println("remove: could not parse: " + proposedResult);
		}
	}
}