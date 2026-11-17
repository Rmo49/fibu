package com.rmo.fibu.model.depot;

import java.text.ParseException;
import java.util.Date;

import com.rmo.fibu.util.Datum;

/** 
 * Ein Eintrag im Depot 
 */
public class Position {

	// Attribute, Felder einer Position
	private String 	stockNr;
	private String	produkt;
	private String	bezeichnung;
	private double	einstand;
	private Double	kurs;
	private	Datum	kaufDatum;
	private Datum	verkaufDatum;
	
	/** 
	 * für leere Position anlegen
	 */
	public Position() {
		
	}

	/** Konstruktor mit allen Attributen
	 * 
	 * @param stockNr
	 * @param produkt
	 * @param bezeichnung
	 * @param einstand
	 * @param kurs
	 * @param kaufDatum
	 * @param verkaufDatum
	 */
	public Position(String stockNr, String produkt, String bezeichnung, double einstand, Double kurs, Datum kaufDatum,
			Datum verkaufDatum) {
		super();
		setStockNr(stockNr);
		setProdukt(produkt);
		setBezeichnung(bezeichnung);
		setEinstand(einstand);
		setKurs(kurs);
		setKaufDatum(kaufDatum);
		setVerkaufDatum(verkaufDatum);
	}

	/** Konstruktor mit allen Attributen
	 * 
	 * @param stockNr
	 * @param produkt
	 * @param bezeichnung
	 * @param einstand
	 * @param kurs
	 * @param kaufDatum
	 * @param verkaufDatum
	 */
	public Position(String stockNr, String produkt, String bezeichnung, double einstand, Double kurs, String kaufDatum,
			String verkaufDatum) {
		super();
		setStockNr(stockNr);
		setProdukt(produkt);
		setBezeichnung(bezeichnung);
		setEinstand(einstand);
		setKurs(kurs);
		setKaufDatum(kaufDatum);
		setVerkaufDatum(verkaufDatum);
	}

	public String getStockNr() {
		return stockNr;
	}

	public void setStockNr(String stockNr) {
		this.stockNr = stockNr;
	}

	public String getProdukt() {
		return produkt;
	}

	public void setProdukt(String produkt) {
		this.produkt = produkt;
	}

	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	public double getEinstand() {
		return einstand;
	}

	public void setEinstand(double einstand) {
		this.einstand = einstand;
	}

	public Double getKurs() {
		return kurs;
	}

	public void setKurs(Double kurs) {
		this.kurs = kurs;
	}

	public Datum getKaufDatum() {
		return kaufDatum;
	}

	public void setKaufDatum(Datum kaufDatum) {
		this.kaufDatum = kaufDatum;
	}
	
	public void setKaufDatum(String pValue) {
        if (kaufDatum == null) {
        	kaufDatum = new Datum();
        }
        try {
        	kaufDatum.setDatum(pValue);
        }
        catch (ParseException ex) {
        	// nix
        }
	}

	public void setKaufDatum(Date pValue) {
        if (kaufDatum == null) {
        	kaufDatum = new Datum();
        }
        kaufDatum.setDatum(pValue);
	}


	public Datum getVerkaufDatum() {
		return verkaufDatum;
	}

	public void setVerkaufDatum(Datum verkaufDatum) {
		this.verkaufDatum = verkaufDatum;
	}
	
	public void setVerkaufDatum(Date pValue) {
		if (pValue == null) {
			verkaufDatum = null;
		}
		else {
	        if (verkaufDatum == null) {
	        	verkaufDatum = new Datum();
	        }
	        verkaufDatum.setDatum(pValue);
		}
	}

	public void setVerkaufDatum(String pValue) {
        if (verkaufDatum == null) {
        	verkaufDatum = new Datum();
        }
        try {
        	verkaufDatum.setDatum(pValue);
        }
        catch (ParseException ex) {
        	verkaufDatum = null;
        }
	}
	
	
}
