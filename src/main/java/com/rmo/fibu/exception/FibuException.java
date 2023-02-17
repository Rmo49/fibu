package com.rmo.fibu.exception;

/** Die allgemeine Fibu Exception,
 * wird geworfen wenn Error nicht näher spezifiziert,
 * Fehler ist in der Message anzuzeigen
 */
public class FibuException extends Exception
{
	private static final long serialVersionUID = -4260846904191081241L;
	public FibuException() {
	}

	public FibuException(String pMessage) {
		super (pMessage);
	}
}
