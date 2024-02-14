package com.rmo.fibu.model.from;

import java.sql.Connection;

import com.rmo.fibu.exception.FibuRuntimeException;

/**
Erweiterung des Data-Models auf die Connection2
 */
public abstract class DataBaseFrom { //extends DataBase {

	/**
	 * Model constructor comment.
	 */
	public DataBaseFrom() throws Exception {
		super();
	}

	/** Die Connection zu der DB */
	protected Connection getConnection() throws FibuRuntimeException {
		return DbConnectionFrom.getConnection();
	}

	/**
	 * Jede Klasse muss den check implementieren
	 */
	public abstract void checkTableVersion();

}
