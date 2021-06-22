package com.rmo.fibu.view.util;

import javax.swing.JFormattedTextField;

import com.rmo.fibu.util.Config;

/**
 * überschriebene Basisklasse, damit Font gesetzt werden kann.
 * @author Ruedi
 *
 */
public final class JFormattedTextFieldExt extends JFormattedTextField {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -1166831038069002713L;

	public JFormattedTextFieldExt(java.text.Format format) {
		 super (format);
		setFont(Config.fontText);
	 }
}
