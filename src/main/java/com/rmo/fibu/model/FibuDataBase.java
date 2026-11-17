package com.rmo.fibu.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Calendar;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.DatumFibu;
import com.rmo.fibu.util.Trace;

/**
 * Verwaltet die Datenbank. Kennt den namen der geöffneten Fibu
 *
 * @author Ruedi
 */
public class FibuDataBase {
	// Create Statements


	/** Verbindung zu Data bean */
	private static FibuData mFibuData = null;
	// --- Control-Vars

	/**
	 * Eine neue Fibu anlegen
	 *
	 * @param fibuName
	 * @return
	 * @throws FibuException
	 */
	public static void newFibu(String fibuName) throws FibuException {
		Trace.println(1, "newFibu()");
		// try to open fibu
		try {
			Statement statement = DbConnection.getConnection().createStatement();
			String schemaName = "CREATE SCHEMA " + fibuName + ";";
			statement.execute(schemaName);
			String useName = "USE " + fibuName + ";";
			statement.execute(useName);
			statement.execute(FibuData.CREATE_FIBUDATEN);
			statement.execute(KontoData.CREATE_KONTORAHMEN);
			statement.execute(BuchungData.CREATE_BUCHUNG);
			statement.execute(ParserBankData.CREATE_CSVBANK_V2);
			statement.execute(ParserKeywordData.CREATE_CSVKEYWORD_V2);
			statement.close();
			Config.sFibuTitel = "Fibu Name";
			vonBisDatumSetzen();
			getFibuData().writeFibuData();
			DbConnection.close();
			Config.addFibuToList(fibuName);
		} catch (java.sql.SQLException e) {
			throw new FibuException("FibuDataBase.newFibu \n SQLState: "
					+ e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	/** Die Fibu öffnen, die connection setzen */
	public static void openFibu(String dbName) throws FibuException {
		Trace.println(1, "FibuDataBase.openFibu(name: " + dbName + ")");
		// Connection öffnen
		DbConnection.open(dbName);
		Config.setDbName(dbName);
		// Stammdaten einlesen
		readFibuData();
		// ruft alle checks in den Data Klassen
		DataBeanContext.checkAllTableVersions();
		// alle Konti neu berechnen
		Trace.println(2, "FibuDataBase.openFibu, Konto calculate");
		KontoCalculator calculator = new KontoCalculator();
		calculator.calculateSaldo();
	}

	/**
	 * Wenn neue Fibu, dann das Datum auf das neue Jahr setzen.
	 * @throws FibuException
	 */
	private static void vonBisDatumSetzen() throws FibuException {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		try {
			String newDatum = "01.01." + year;
			DatumFibu datumVon = new DatumFibu();
			datumVon.setNewDatum(newDatum);
			Config.sDatumVon = datumVon;
			newDatum = "31.12." + year;
			DatumFibu datumBis = new DatumFibu();
			datumBis.setNewDatum(newDatum);
			Config.sDatumBis = datumBis;
		}
		catch (ParseException ex) {
			Trace.println(3, "FibuDataBase.vonBisDatumSetzen, " + ex.getMessage());
			throw new FibuException(ex.getMessage());
		}
	}


	/** Daten von der DB einlesen. */
	private static void readFibuData() throws FibuException {
		Trace.println(2, "FibuDataBase.readFibuData()");
		getFibuData().readFibuData();
	}

	/** Daten in die DB schreiben */
	public static void writeFibuData() throws FibuException {
		Trace.println(2, "FibuDataBase.writeFibuData()");
		getFibuData().writeFibuData();
	}

	/**
	 * Prüft, ob tabelle vorhanden
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static boolean tableExist(String tableName) throws SQLException {
	    boolean tExists = false;
	    Connection conn = DbConnection.getConnection();
	    try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
	        while (rs.next()) {
	            String tName = rs.getString("TABLE_NAME");
	            if (tName != null && tName.equals(tableName)) {
	                tExists = true;
	                break;
	            }
	        }
	    }
	    return tExists;
	}

	/**
	 * Bestehende Fibu löschen, Connection wird geschlossen.
	 * @param name der Fibu
	 */
	public static void deleteFibu(String fibuName) throws FibuException {
		Trace.println(1, "deleteFibu()");
		// try to open fibu
		try {
			Statement statement = DbConnection.getConnection()
					.createStatement();
			String schemaName = "DROP SCHEMA IF EXISTS " + fibuName + ";";
			// newFibu.setString(1, fibuName);
			statement.execute(schemaName);
			statement.close();
			DbConnection.close();
			Config.deleteFibuFromList(fibuName);
		} catch (java.sql.SQLException e) {
			throw new FibuException("FibuDB löschen \n SQLState: "
					+ e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	private static FibuData getFibuData() {
		if (mFibuData == null) {
			mFibuData = (FibuData) DataBeanContext.getDataBean(FibuData.class);
		}
		return mFibuData;
	}


}
