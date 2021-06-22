package com.rmo.fibu.exception;

/** Wenn ein Wert fehlerhaft ist.
 */

public class BuchungValueException extends FibuException {

	private static final long serialVersionUID = -1904960586722707471L;

	public BuchungValueException() {
    }

	public BuchungValueException(String pMessage) {
		super (pMessage);
	}
}