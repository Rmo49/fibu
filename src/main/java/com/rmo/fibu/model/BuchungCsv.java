package com.rmo.fibu.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Datenstruktur eines Tupels eingelesen von CSV.
 * @author Ruedi
 *
 */
public class BuchungCsv {

	private String datum;
	private String beleg;
	private String text;
	private String betrag;
	private String soll;
	private String haben;

	public BuchungCsv() {
	}

	public BuchungCsv(String datum, String beleg, String text, String betrag, String soll, String haben) {
		this.datum = datum;
		this.beleg = beleg;
		this.text = text;
		this.betrag = betrag;
		this.soll = soll;
		this.haben = haben;
	}

	// ---- setter und getter

	public String getDatum() {
		return datum;
	}

	public void setDatum(String datum) {
		this.datum = datum;
	}

	public void setDatum(Date datum) {
		DateFormat df = new SimpleDateFormat("dd.MM.yy");
		this.datum = df.format(datum);
	}

	public String getBeleg() {
		return beleg;
	}

	public void setBeleg(String beleg) {
		this.beleg = beleg;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getBetrag() {
		return betrag;
	}

	public void setBetrag(String betrag) {
		this.betrag = betrag;
	}

	public String getSoll() {
		return soll;
	}

	public void setSoll(String soll) {
		this.soll = soll;
	}

	public String getHaben() {
		return haben;
	}

	public void setHaben(String haben) {
		this.haben = haben;
	}

}
