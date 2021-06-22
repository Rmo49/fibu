package com.rmo.fibu.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

/**
 * Das Datum mit der richtigen Darstellung.
 * Mit div. Umwandlungen zu java.util.Date und java.sql.Date
 */
public class Datum extends Date {

	private static final long serialVersionUID = -5981032883781686216L;

	public Datum() {
		super();
	}

	/** Standard-Konstruktor, prüft den Range des Datums nicht.
	 *  Sollte nur zum setzen in der Config verwendet werden.  */
    public Datum(long time) {
		super(time);
    }

	/** Datum kann in der Form 25.7.01 übergeben werden.
	 *  prüft das Format und ob das Datum im Buchhaltungs-Range liegt */
	public Datum (String datum) throws ParseException {
		setDatum(datum);
	}

    /** Setzt das Datum, falls richtig eingegeben in der Form 25.7.01
     *  Wenn ok, wird das Datum gesetzt.
	 *  @exception  ParseException falls nicht richtig formatiert
	 *  oder nicht im Bereich der Buchhaltung liegt */
    public void setDatum(String datum) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
		Date date = DatumFormat.getDatumInstance().parse(datum, pos);
        if (date == null) {
            throw new ParseException("ungültiges Datum", 0);
        }
 		if ( date.compareTo(Config.sDatumVon) < 0
			|| date.compareTo(Config.sDatumBis) > 0 ) {
            throw new ParseException("Datum nicht innerhalb "
				+Config.sDatumVon.toString() + "-"
				+Config.sDatumBis.toString() , 0);
		}
       setTime(date.getTime());
    }

    /** Setzt das Datum, falls richtig eingegeben.
     *   Wenn ok, wird das Datum gesetzt
	 *   @exception  ParseException falls nicht richtig formatiert ist. */
    public void setNewDatum(String text) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
		Date date = DatumFormat.getDatumInstance().parse(text, pos);
        if (date == null) {
            throw new ParseException("ungültiges Datum", 0);
        }
       setTime(date.getTime());
    }

	/** Setzt das neue Datum  */
    public void setDatum(java.util.Date date)  {
		this.setTime(date.getTime());
    }

	/** Setzt das neue Datum  */
    public void setDatum(java.sql.Date date)  {
		this.setTime(date.getTime());
    }

	/** Umwandlung in eine anderen Form */
	public java.sql.Date asSqlDate() {
		return new java.sql.Date(getTime());
	}

	/** Gibt das Datum in String-Format zurück */
	@Override
	public String toString () {
		StringBuffer sb = new StringBuffer(12);
		sb = DatumFormat.getDatumInstance().format(this, sb, new FieldPosition(DateFormat.SHORT));
		return sb.toString();
	}
}