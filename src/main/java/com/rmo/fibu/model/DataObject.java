package com.rmo.fibu.model;

import java.sql.Connection;

import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.model.depot.DbConnectionDepot;

/**
Basis-Klasse aller DB-Model-Klassen, die Zugriff auf DB implementieren,
Ist zuständig für den Bean-Support aller Model-Klassen.
 */
public abstract class DataObject {

	/**
	 * DataObject constructor.
	 */
	public DataObject() {
	}


	/** Die Connection zu der DB */
	protected Connection getConnection() throws FibuRuntimeException {
		return DbConnection.getConnection();
	}

	/** Die Connection zu der DB */
	protected Connection getConnectionDepot() throws FibuRuntimeException {
		return DbConnectionDepot.getConnection();
	}


	/**
	 * Jede Klasse muss den check implementieren
	 */
	public abstract void checkTableVersion();
}
