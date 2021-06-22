package com.rmo.fibu.exception;

/** Wird geworfen, wenn das Konto nicht gefunden
 */
public class BuchungNotFoundException extends FibuException
{
	private static final long serialVersionUID = -7814544678385854469L;
	/**
	 * Creation date: (26.12.00 15:25:34)
	 */
	public BuchungNotFoundException() {}
	public BuchungNotFoundException(String message) {
		super(message);
	}
}
