package com.rmo.fibu.model.from;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.FibuData;
import com.rmo.fibu.util.Trace;

/**
 * Verwaltet eine alte Datenbank, von der die Kontodaten gelesen werden.
 * Kennt den namen der alten Fibu
 * 
 * @author Ruedi
 */
public class FibuDataBaseFrom {
	// Create Statements

	/** Verbindung zu Data bean */
	private static FibuData mFibuData;
	// --- Control-Vars


	/** Eine Fibu öffnen, die connection setzen */
	public static void openFibu(String dbName) throws FibuException {
		Trace.println(1, "FibuDataBaseFrom.openFibu(name: " + dbName + ")");
		// Connection öffnen
		DbConnectionFrom.open(dbName);
		//Config.setDbName(dbName);
		// Stammdaten einlesen
		readFibuData();
		// alle Konti neu berechnen
		Trace.println(1, "FibuView.openFibu, Konto calculate");
	}

	/** Daten von der DB einlesen. */
	private static void readFibuData() throws FibuException {
		Trace.println(2, "FibuDataBaseFrom.readFibuData()");
		mFibuData = (FibuData) DataBeanContext.getContext().getDataBean(
				FibuData.class);
		mFibuData.readFibuData();
	}

	/** Daten in die DB schreiben */
	public static void writeFibuData() throws FibuException {
		Trace.println(2, "FibuDataBaseFrom.writeFibuData()");
		mFibuData.writeFibuData();
	}

}
