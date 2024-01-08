package com.rmo.fibu.model;

import java.text.ParseException;
import java.util.Date;

import com.rmo.fibu.exception.BuchungValueException;
import com.rmo.fibu.util.Datum;
import com.rmo.fibu.util.Trace;

/** Eine Buchung, wird für den Datenaustausch zwischen View und Model verwendet
 */
public class Buchung {
	// Attribute
	private long		mID = -1;			// die unique ID der Buchung
	private	Datum		mDatum = null;
	private String		mBeleg = null;
	private String		mBuchungText = null;
	private double		mBetrag = 0;
	private int			mSoll = 0;
	private int			mHaben = 0;
    private boolean     mFehler = false;

    public Buchung() {
        mBetrag = 0;
        mID = -1;
    }

	/** Konstruktor mit allen Datentypen */
    public Buchung(long ID, Datum datum, String beleg, String buchungsText, int soll, int haben, double betrag) {
        mID = ID; // die ID von Access
		mDatum = datum;
		mBeleg = beleg;
        mBuchungText = buchungsText;
        mSoll = soll;
        mHaben = haben;
        mBetrag = betrag;
    }

	/** Konstruktor Datum als String */
    public Buchung(long ID, String datum, String beleg, String buchungsText, int soll, int haben, double betrag) {
        mID = ID; // die ID von Access
        try {
            setDatum(datum);
        }
        catch (ParseException e) {
        	Trace.println(1, "error: falsches Datum: " + datum);
        }
        mBeleg = beleg;
        mBuchungText = buchungsText;
        mSoll = soll;
        mHaben = haben;
        mBetrag = betrag;
    }

    /** Vergleicht 2 Buchungen, sind gleich, wenn ID gleich ist,
     *  oder alle Felder gleich sind.  */
    @Override
	public boolean equals(Object pObject) {
        if (pObject instanceof Buchung) {
            // wenn beide ID's gesetzt, dann entscheidet diese
            if (mID >= 0 && ((Buchung) pObject).getID() >= 0) {
                return mID == ((Buchung) pObject).getID();
            }
            if (!mDatum.equals(((Buchung) pObject).getDatum()))
                return false;
            String beleg = ((Buchung) pObject).getBeleg();
            if ((beleg != null && !mBeleg.equals(((Buchung) pObject).getBeleg())) || (mSoll != ((Buchung) pObject).getSoll()) || (mHaben != ((Buchung) pObject).getHaben()))
                return false;
            if (Math.round(mBetrag) != Math.round(((Buchung) pObject).getBetrag()))
                return false;
        } else {
            return false;
        }
        return true;
    }

    /** Vergleicht 2 Buchungen, sind gleich, wenn Datum, Soll, Haben, Betrag gleich sind.
     *  oder alle Felder gleich sind.  */
    public boolean sameAs(Object pObject) {
        if (pObject instanceof Buchung) {
            // wenn beide ID's gesetzt, dann entscheidet diese
            if (mID >= 0 && ((Buchung) pObject).getID() >= 0) {
                return mID == ((Buchung) pObject).getID();
            }
            if (!mDatum.equals(((Buchung) pObject).getDatum()) || (mSoll != ((Buchung) pObject).getSoll()) || (mHaben != ((Buchung) pObject).getHaben()) || (Math.round(mBetrag) != Math.round(((Buchung) pObject).getBetrag())))
                return false;
        } else {
            return false;
        }
        return true;
    }


    //----- getter mehtoden ------------------------------
	public String getBeleg() {
		if (mBeleg == null) {
			return "";
		}
		return mBeleg;
	}
	public double getBetrag() {
		return mBetrag;
	}
	public String getBetragAsString() {
		return String.valueOf(mBetrag);
	}
	public String getBuchungText() {
		return mBuchungText;
	}
	/** auslesen für SQL ' => \' (@todo welcher Escape-Char?)
	 *  Notlösung: alle ' entfernen */
	public String getBuchungTextSql() {
		if (mBuchungText.indexOf('\'') >= 0) {
		    StringBuffer strBuf = new StringBuffer(mBuchungText);
			int i = 0;
			while (i < strBuf.length()) {
			    if (strBuf.charAt(i) == '\'') {
				    strBuf.deleteCharAt(i);
				}
				i++;
			}
			return strBuf.toString();
		}
		return mBuchungText;
	}

	public Datum getDatum() {
		if (mDatum == null) {
			return new Datum();
		}
 		return mDatum;
	}
	public String getDatumAsString() {
		if (mDatum == null) {
			return "";
		}
        return mDatum.toString();
	}

	public int getSoll() {
		return mSoll;
	}
	public String getSollAsString() {
		return String.valueOf(mSoll);
	}

	public int getHaben() {
		return mHaben;
	}
	public String getHabenAsString() {
		return String.valueOf(mHaben);
	}

	public long getID() {
		return mID;
	}
	public boolean isFehler() {
		return mFehler;
	}

    //----- Setter Methoden -----------------------------------
	public void setBeleg(String pValue) {
		mBeleg = pValue;
	}
	public void setBetrag(double pValue) {
		mBetrag = pValue;
	}
	public void setBetrag(String pValue) {
		mBetrag = Double.parseDouble(pValue);
	}

	public void setBuchungText(String pValue) {
		mBuchungText = pValue;
	}
    /** setMethod with parsing */
    public void setDatum(String pValue) throws ParseException {
        if (mDatum == null) {
			mDatum = new Datum();
        }
		mDatum.setDatum(pValue);
    }

	public void setDatum(Date pValue) {
        if (mDatum == null) {
			mDatum = new Datum();
        }
		mDatum.setDatum(pValue);
	}

	public void setSoll(String pValue) throws BuchungValueException {
		setSoll(convertToInt(pValue));
	}

	public void setSoll(int pValue) throws BuchungValueException {
		mSoll = pValue;
	}

	public void setHaben(String pValue) throws BuchungValueException {
		setHaben( convertToInt(pValue) );
	}

	public void setHaben(int pValue) throws BuchungValueException {
        if (mSoll > 0 && mSoll == pValue) {
            throw new BuchungValueException("Soll und Haben gleiches Konto: " +pValue);
        }
		mHaben = pValue;
	}

	private int convertToInt(String pValue) throws BuchungValueException {
		if ((pValue == null) || (pValue.length() < 4)) {
			throw new BuchungValueException("KontoNr zu klein: " +pValue);
		}
		int intValue = 0;
		try {
			intValue = Integer.valueOf(pValue).intValue();
		}
		catch (NumberFormatException ex) {
			throw new BuchungValueException("KontoNr: " +pValue + " " + ex.getMessage());
		}
		return intValue;
	}

	public void setID(long pValue) {
		mID = pValue;
	}

    public void setFehler(boolean pValue) {
		mFehler = pValue;
	}

	/** gibt die Buchung in Stringform aus
	 */
	@Override
	public String toString() {
		StringBuffer lStr = new StringBuffer();
		lStr.append("ID:");
        lStr.append(getID());
		lStr.append(" ");
		lStr.append(getDatum().toString());
		lStr.append(" ");
		lStr.append(getBeleg());
		lStr.append(" ");
		lStr.append(getBuchungText());
		lStr.append(" ");
		lStr.append(getSoll());
		lStr.append(" ");
		lStr.append(getHaben());
		lStr.append(" ");
		lStr.append(String.valueOf(getBetrag()));
		return lStr.toString();
	}
}
