package com.rmo.fibu.exception;

/**
 * Diese Exception wird geworfen, für System-Fehler.
 * Diese Exception wird im main-Programm gefangen und der Fehler angezeigt.
 * @author Ruedi
 *
 */
public class FibuRuntimeException extends RuntimeException {

	/**
	 * Serie-Number
	 */
	private static final long serialVersionUID = -5537187529573613195L;

	/**
	 * Konstruktur mit message.
	 * @param pMessage
	 */
	public FibuRuntimeException(String pMessage) {
		super (pMessage);
	}

}
