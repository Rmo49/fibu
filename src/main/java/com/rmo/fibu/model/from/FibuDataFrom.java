package com.rmo.fibu.model.from;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.util.Trace;

/**
 * Verwaltet die Steuerdaten der Fibu.
 */
public class FibuDataFrom extends DataModelFrom implements Serializable {
	private static final long serialVersionUID = -8821885201937153074L;

	/**
	 * FibuData constructor comment.
	 * 
	 * @exception java.lang.Exception
	 *                The exception description.
	 */
	public FibuDataFrom() throws Exception {
		super();
	}

	/**
	 * Liest die allgemeinen Fibu-Daten von der DB.
	 */
	public String readFibuTitel() throws FibuException {
		Trace.println(3, "FibuDataFrom.readFibuTitel()");
		try {
			Connection conn = getConnection();
			Statement stmt = conn.createStatement();
			String lQuery = "SELECT * FROM FibuDaten WHERE ID = 1";
			ResultSet lResult = stmt.executeQuery(lQuery);
			if (lResult.next()) {
				return lResult.getString("FibuTitel");
			} 
		} catch (java.sql.SQLException e) {
			throw new FibuException("FibuDaten readFibuData \n SQLState: "
					+ e.getSQLState() + " Message: " + e.getMessage());
		}
		return "";
	}

}
