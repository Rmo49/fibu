package com.rmo.fibu.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Standard-Format des Datums für die Fibu
*/

public class DatumFormat extends SimpleDateFormat {

	private static final long serialVersionUID = 2271319664865682830L;
	private static DatumFormat instance = null;

    /** Konstruktor mit Standard-Einstellungen */
    private DatumFormat() {
        super("dd.MM.yy");
        Calendar start2Year = Calendar.getInstance();
        start2Year.set(1950,1,1);
        set2DigitYearStart(start2Year.getTime());
    }

    public static DatumFormat getDatumInstance() {
        if (instance == null) {
            instance = new DatumFormat();
        }
        return instance;
    }

    /** parsed den text (das Datum)
     *   Wenn ok, gibt den neu formatierten String zurück (mit 4-Stelliger Jahreszahl) */
    public String parseDatum(String text) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = parse(text, pos);
        if (date == null) {
            throw new ParseException("ungültiges Datum", 0);
        }
        return getDatumInstance().format(date);
    }

	/** Gibt das Datum in String-Format zurück */
	public String toString (Date date) {
		StringBuffer sb = new StringBuffer(12);
		sb = format(date, sb, new FieldPosition(DateFormat.SHORT));
		return sb.toString();
	}

    /** Ueberschreibt parse von SimpleDateFormat */
    /*
    public Date parse(String text) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        return parse(text, pos);
    }
    */
    }