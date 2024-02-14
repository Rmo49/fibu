package com.rmo.fibu.model.from;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.util.Trace;

/**
 * Verwaltet eine alte Datenbank, von der die Kontodaten gelesen werden.
 * Kennt den namen der alten Fibu
 *
 * @author Ruedi
 */
public class FibuDataBaseFrom {

	/** Eine Fibu öffnen, die connection setzen */
	public static void openFibu(String dbName) throws FibuException {
		Trace.println(1, "FibuDataBaseFrom.openFibu(name: " + dbName + ")");
		// Connection öffnen
		DbConnectionFrom.open(dbName);
		// Stammdaten einlesen
		// ruft alle checks in den Data Klassen
		DataBeanContextFrom.getContext().checkAllTableVersions();
	}

	/**
	 * Prüft, ob tabelle vorhanden
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static boolean tableExist(String tableName) throws SQLException {
	    boolean tExists = false;
	    Connection conn = DbConnectionFrom.getConnection();
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


}
