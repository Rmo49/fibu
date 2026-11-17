package com.rmo.fibu.model.depot.test;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.depot.DbConnectionDepot;
import com.rmo.fibu.model.depot.DepotDataBase;

/**
 * Zum Verwalten der Datenbank, wird von anderen Test aufgerufen
 */
public class DbHandlingDepot {
	
	public static final String dbName = "DepotTest";



	/**
	 * Die DB löschen
	 * return 1, wenn ok
	 */
	public static int deleteDb(String dbName) {
		if (DbConnectionDepot.isDbOpen()) {
			try {
				DbConnectionDepot.close();
			} catch (FibuException ex) {
				return -1;
			}
		}
		try {
			DepotDataBase.deleteDb(dbName);
		} catch (FibuException ex) {
			return -1;
		}
		return 1;
	}

	/**
	 * DB anlegen
	 * @param dbName
	 * @return 1, wenn ok,
	 */
	public static int makeDb(String dbName) {
		try {
			DepotDataBase.createDb(dbName);
		} catch (FibuException ex) {
			return -1;
		}
		return 1;
	}

}
