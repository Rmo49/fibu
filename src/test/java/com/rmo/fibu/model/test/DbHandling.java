package com.rmo.fibu.model.test;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.FibuDataBase;

public class DbHandling {
	
	/**
	 * Die DB löschen
	 * return 1, wenn ok
	 */
	public static int deleteDb(String dbName) {	
		if (DbConnection.isFibuOpen()) {
			try {
				DbConnection.close();
			} catch (FibuException ex) {
				return -1;
			}
		}
		try {
			FibuDataBase.deleteFibu(dbName);
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
			FibuDataBase.newFibu(dbName);
		} catch (FibuException ex) {
			return -1;
		}	
		return 1;
	}
	
	
}
