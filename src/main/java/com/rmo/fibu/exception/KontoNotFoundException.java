package com.rmo.fibu.exception;

/** Wird geworfen, wenn das Konto nicht gefunden
 */
public class KontoNotFoundException extends FibuException
{
	private static final long serialVersionUID = 1L;
	public KontoNotFoundException()
	{
	}
	public KontoNotFoundException(String pMessage)
	{
		super (pMessage);
	}
}
