package com.rmo.fibu.view.util;

import javax.swing.JTextField;

import com.rmo.fibu.util.Config;


/**
 * überschriebene Basisklasse, damit Font gesetzt werden kann.
 * @author Ruedi
 *
 */
public class JTextFiledExt extends JTextField {

	/**
	 *
	 */
	private static final long serialVersionUID = -278586863536877235L;

	public JTextFiledExt() {
		setFont(Config.fontText);
	}

}
