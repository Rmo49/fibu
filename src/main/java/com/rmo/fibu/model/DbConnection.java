package com.rmo.fibu.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

/**
 * Connection zur MySql-DB.
 */
public class DbConnection {
	private static Connection sConnection = null; // Verbindung zu DB
	// --- connection to Access-DB.
	// public static final String sJdbcDriver = "sun.jdbc.odbc.JdbcOdbcDriver";
	// --- connection to MySql
//	public static final String sJdbcDriver = "com.mysql.jdbc.Driver";
	public static final String sJdbcDriver = "com.mysql.cj.jdbc.Driver";
	//public static final String url = "jdbc:mysql://localhost:3306/";

	public DbConnection() {
	}

	/**
	 * Opens the Connection to MySql which is set in Config.
	 *
	 * @param dbName
	 *            the name of database (schema), if null no database is opend
	 * @return Connection or null if not opened.
	 */
	public static Connection open(String dbName) throws FibuRuntimeException {
		// Connection aufbauen
		Trace.println(0, "Connection.open(dbName:" + dbName + ")");
		try {
			if (sConnection == null || sConnection.isClosed()) {
				Class.forName(sJdbcDriver).getDeclaredConstructor().newInstance();
				if (dbName == null) {
					// wenn kein dbName, dann wird eine neue DB angelegt
					sConnection = DriverManager.getConnection(Config.dbUrl, Config.userName, Config.password);
					Config.setDbName(dbName);
				} else {
					sConnection = DriverManager.getConnection(Config.dbUrl + dbName,
							Config.userName, Config.password);
				}
				Trace.println(1, "Connected to the database");
				sConnection.setAutoCommit(true);
			}
		} catch (Exception ex) {
			throw new FibuRuntimeException(ex + ex.getMessage() +
					"\n Solution: Check config file, Is MySql running? Check Verwaltung > Dienste > MySql57");
		}
		return sConnection;
	}

	/**
	 * Verbindung zur DB noch offen?
	 * @return
	 */
	public static boolean isConnected() {
		try {
			if (sConnection == null || sConnection.isClosed()) {
				return false;
			}
			return true;
		}
		catch (SQLException e) {
			// wenn Exception, dann Problem
			return false;
		}
	}

	/**
	 * Close the Connection.
	 */
	public static void close() throws FibuException {
		try {
			// Connection schliessen
			if (sConnection != null) {
				// sConnection.commit();
				sConnection.close();
				sConnection = null;
				Config.setDbName(null);
			}
		} catch (Exception e) {
			throw new FibuException("Schliessen der DB '" + Config.getDbName()
					+ "' Fehlermeldung: \n" + e.getMessage());
		}
	}

	/**
	 * Returns the Connection to database. If not open, it will setup
	 * a connection to mySQL, or a specific schema.
	 * @return Connection to mySql or a schema
	 */
	public static Connection getConnection() throws FibuRuntimeException {
		try {
			if (sConnection == null || sConnection.isClosed()) {
				open(null);
			}
			return sConnection;
		} catch (SQLException ex) {
			throw new FibuRuntimeException(ex.getMessage());
		}
	}

	/**
	 * @return true Wenn connection zu einer Fibu-DB gesetzt ist
	 */
	public static boolean isFibuOpen() {
		if (isConnected()) {
			return Config.getDbName() != null;
		}
		else {
			Config.setDbName("");
			return false;
		}
	}

}