package com.rmo.fibu.model.depot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.depot.PositionData;
import com.rmo.fibu.util.Trace;

/**
 * Verwaltet die Datenbank. Kennt den namen der geöffneten Fibu
 *
 * @author Ruedi
 */
public class DepotDataBase {
	// Create Statements

	/** Verbindung zu Data bean */
	private static PositionData mPositionData = null;
	// --- Control-Vars

	/**
	 * Eine neue Depot DB anlegen
	 *
	 * @param depotName
	 * @return
	 * @throws FibuException
	 */
	public static void createDb(String depotName) throws FibuException {
		Trace.println(1, "newDepotDb()");
		// try to open fibu
		try {
			Statement statement = DbConnectionDepot.getConnection().createStatement();
			// DB anlegen
			String schemaName = "CREATE SCHEMA " + depotName + ";";
			statement.execute(schemaName);
			String useName = "USE " + depotName + ";";
			statement.execute(useName);
			statement.execute(PositionData.CREATE_POSITION);
			statement.execute(TransaktionData.CREATE_TRANSAKTION);
			statement.close();
			DbConnectionDepot.close();
		} catch (java.sql.SQLException e) {
			throw new FibuException(
					"DepotDataBase.newDepotDb \n SQLState: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	/** Die Fibu öffnen, die connection setzen */
	public static void openDepot(String dbName) throws FibuException {
		Trace.println(1, "DepotDataBase.openDepot(name: " + dbName + ")");
		// Connection öffnen
		DbConnectionDepot.open(dbName);
	}

	/**
	 * Bestehende Fibu löschen, Connection wird geschlossen.
	 * 
	 * @param name der Fibu
	 */
	public static boolean dbExists(String dbName) throws FibuException {
		Trace.println(1, "dbExists()");
		// try to open DB
		String sql = """
				SELECT EXISTS (
				    SELECT 1
				    FROM INFORMATION_SCHEMA.SCHEMATA
				    WHERE SCHEMA_NAME = ?
				)
				""";
		try {
			PreparedStatement ps = DbConnectionDepot.getConnection().prepareStatement(sql);
			ps.setString(1, dbName);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next() && rs.getBoolean(1)) {
					return true;
				} else {
					return false;
				}
			} catch (java.sql.SQLException e) {
				return false;
			}

		} catch (java.sql.SQLException e) {
			return false;
		}
	}

	/**
	 * Prüft, ob tabelle vorhanden
	 * 
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static boolean tableExist(String tableName) throws SQLException {
		boolean tExists = false;
		Connection conn = DbConnectionDepot.getConnection();
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
	 * Bestehende DB löschen, Connection wird geschlossen.
	 * 
	 * @param name der Fibu
	 */
	public static void deleteDb(String dbName) throws FibuException {
		Trace.println(1, "deleteDepot()");
		// try to open fibu
		try {
			Statement statement = DbConnectionDepot.getConnection().createStatement();
			String schemaName = "DROP SCHEMA IF EXISTS " + dbName + ";";
			// newFibu.setString(1, fibuName);
			statement.execute(schemaName);
			statement.close();
			DbConnectionDepot.close();
		} catch (java.sql.SQLException e) {
			throw new FibuException("DepotDB löschen \n SQLState: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}

}
