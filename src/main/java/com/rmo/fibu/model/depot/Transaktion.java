package com.rmo.fibu.model.depot;

import java.text.ParseException;
import java.util.Date;

import com.rmo.fibu.util.Datum;
import com.rmo.fibu.util.DatumFibu;

/**
 * Eine Buchung zu einer Position Eintrag
 */
public class Transaktion {

	// Attribute / Felder eines TRansaktion
	private	Datum	datum;
	private String 	stockNr;
	private String	text;
	private Double	betrag;

	
	/**
	 * leere Buchung Objekt anlegen
	 */
	public Transaktion() {
	}

	/**
	 * Buchung mit allen Attributen
	 */
	public Transaktion(String datum, String stockNr, String text, Double betrag) {
		super();
		setDatum(datum);
		setStockNr(stockNr);
		setText(text);
		setBetrag(betrag);
	}

	public Datum getDatum() {
		return datum;
	}

	public void setDatum(DatumFibu datum) {
		this.datum = datum;
	}

	public void setDatum(Date pValue) {
        if (datum == null) {
        	datum = new Datum();
        }
        datum.setDatum(pValue);
	}

	public void setDatum(java.sql.Date pValue) {
        if (datum == null) {
        	datum = new Datum();
        }
        datum.setDatum(pValue);		
	}
	
	public void setDatum(String pValue) {
        if (datum == null) {
        	datum = new Datum();
        }
        try {
        	datum.setDatum(pValue);
        }
        catch (ParseException ex) {
        	datum = null;
        }
	}


	public String getStockNr() {
		return stockNr;
	}

	public void setStockNr(String stockNr) {
		this.stockNr = stockNr;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Double getBetrag() {
		return betrag;
	}

	public void setBetrag(Double betrag) {
		this.betrag = betrag;
	}


	
	
	
}
