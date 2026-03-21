package com.rmo.fibu.view.util;

import javax.swing.JLabel;

import com.rmo.fibu.util.Config;

/**
 * Alle JLabels in fetter Schrift
 */
public class JLabelBold extends JLabel {

	private static final long serialVersionUID = 1000087005106638127L;

	public JLabelBold() {
		setFont(Config.fontTextBold);
	}


	public JLabelBold(String text) {
		setFont(Config.fontTextBold);
		setText(text);
	}
}
