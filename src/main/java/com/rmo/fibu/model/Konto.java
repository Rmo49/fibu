package com.rmo.fibu.model;

/** Ein Konto, wird für den Datenaustausch zwischen View und Model verwendet
 */
public class Konto
{
	// Attribute (Felder) eines Kontos
	private int			mKontoNr = 0;
	private String		mText = null;
	private double		mStartSaldo = 0;
	private double		mSaldo = 0;
	private boolean		mIstSollKonto = true;

public Konto() {
}
/**
 * Konstruktor mit allen Attributen
 * @param pKontoNr int
 * @param pText java.lang.String
 * @param pStartSaldo double
 * @param pSaldo double
 * @param pIstSollKonto boolean
 */
public Konto (int pKontoNr, String pText, double pStartSaldo, double pSaldo, boolean pIstSollKonto) {
	mKontoNr = pKontoNr;
	mText = pText;
	mStartSaldo = pStartSaldo;
	mSaldo = pSaldo;
	mIstSollKonto = pIstSollKonto;
}

/** Vergleicht 2 Konto
 */
@Override
public boolean equals(Object anObject) {
	if (anObject instanceof Konto) {
		Konto otherKonto = (Konto) anObject;
		if ((mKontoNr != otherKonto.getKontoNr()) || !mText.equals(otherKonto.getText()) || (mStartSaldo != otherKonto.getStartSaldo()) || (mSaldo != otherKonto.getSaldo())) {
			return false;
		}
		if (mIstSollKonto != otherKonto.isSollKonto()) {
			return false;
		}
	} else {
		return false;
	}
	return true;
}
public int getKontoNr() {
    return mKontoNr;
}

public String getKontoNrAsString() {
    return Integer.toString(mKontoNr);
}

public double getSaldo () {
    return mSaldo;
}

public double getStartSaldo () {
    return mStartSaldo;
}

public String getText() {
    return mText;
}

public boolean isSollKonto () {
    return mIstSollKonto;
}

public void setIstSollKonto (boolean pWert) {
    mIstSollKonto = pWert;
}

public void setKontoNr(int pWert) {
    mKontoNr = pWert;
}

public void setKontoNr(String pWert) {
	mKontoNr = Integer.valueOf(pWert).intValue();
}

public void setSaldo (double pWert) {
    mSaldo = pWert;
}

public void setSaldo(String pWert) {
	mSaldo = Double.valueOf(pWert).doubleValue();
}

public void setStartSaldo (double pWert) {
    mStartSaldo = pWert;
}

public void setStartSaldo(String pWert) {
	mStartSaldo = Double.valueOf(pWert).doubleValue();
}

public void setText(String pWert) {
    mText = pWert;
}
}
