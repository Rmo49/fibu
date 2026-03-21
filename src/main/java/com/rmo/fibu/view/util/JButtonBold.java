package com.rmo.fibu.view.util;

import javax.swing.JButton;

import com.rmo.fibu.util.Config;

/**
 * Mit Fetter Schrift
 */
public class JButtonBold extends JButton {

	private static final long serialVersionUID = 1708406941812285905L;


	public JButtonBold() {
		setFont(Config.fontTextBold);
	}


	public JButtonBold(String text) {
		setFont(Config.fontTextBold);
		setText(text);
	}


}
