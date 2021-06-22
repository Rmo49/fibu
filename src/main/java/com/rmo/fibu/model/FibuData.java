package com.rmo.fibu.model;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

/**
 * Verwaltet die Steuerdaten der Fibu.
 */
public class FibuData extends DataModel implements Serializable {
	private static final long serialVersionUID = -8821885201937153074L;
	private static FibuData sFibuData = null;
	private String mFibuTitel = null;
	private String mFibuName = null;

	/**
	 * FibuData constructor comment.
	 * 
	 * @exception java.lang.Exception
	 *                The exception description.
	 */
	private FibuData() throws Exception {
		super();
	}

	/**
	 * getService
	 */
//	public Iterator<?> getCurrentServiceSelectors(BeanContextServices bcs,
//			Class<?> serviceClass) {
//		return null; // do nothing
//	}

	/** Lazy Instanzierung. */
	public static FibuData getFibuData() {
		if (sFibuData == null) {
			try {
				sFibuData = new FibuData();
			} catch (Exception e) {
				System.err.println("Fehler beim öffnen");
			}
		}
		return sFibuData;
	}

	/**
	 * Aufruf eines Services dieser Klasse. requestor: Ein Objekt des Contextes
	 * sein (BuchungM) serviceClass: Der verlangte service serviceSelector:
	 * Parameter des Services
	 */
//	public Object getService(BeanContextServices bcs, Object requestor,
//			Class<?> serviceClass, Object serviceSelector) {
//		return this;
//	}

	/**
	 * Liest die allgemeinen Fibu-Daten von der DB.
	 */
	public void readFibuData() throws FibuException {
		Trace.println(3, "FibuData.readFibuData()");
		try {
			Connection conn = getConnection();
			Statement stmt = conn.createStatement();
			String lQuery = "SELECT * FROM FibuDaten WHERE ID = 1";
			ResultSet lResult = stmt.executeQuery(lQuery);
			if (lResult.next()) {
				Config.sFibuTitel = lResult.getString("FibuTitel");
				java.sql.Date date = lResult.getDate("DatumVon");
				if (date != null) {
					Config.sDatumVon.setDatum(date);
				}
				date = lResult.getDate("DatumBis");
				if (date != null) {
					Config.sDatumBis.setDatum(date);
				}
				// TODO weitere Felder: DatumFormat (dd.mm.yy), NextBuchungID
			} else {
				Statement create = getConnection().createStatement();
				create.executeUpdate("INSERT INTO FibuDaten VALUES ('1', 'FibuName', 'FibuTitel', NULL, NULL, 'dd.mm.yy')");
			}
		} catch (java.sql.SQLException e) {
			throw new FibuException("FibuDaten readFibuData \n SQLState: "
					+ e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	/**
	 * Schreibt die allgemeinen Fibu-Daten in die DB.
	 */
	public void writeFibuData() throws FibuException {
		Trace.println(3, "FibuData.writeFibuData()");
		try {
			PreparedStatement updateFibu = getConnection().prepareStatement(
				"UPDATE FibuDaten SET FibuName = ?, FibuTitel = ?, DatumVon = ?, DatumBis = ?, DatumFormat = ? WHERE ID = 1");
			updateFibu.setString(1, "Fibu Name");
			updateFibu.setString(2, Config.sFibuTitel);
			updateFibu.setDate(3, Config.sDatumVon.asSqlDate());
			updateFibu.setDate(4, Config.sDatumBis.asSqlDate());
			updateFibu.setString(5, "dd.mm.yy");
			updateFibu.executeUpdate();
		} catch (java.sql.SQLException e) {
			throw new FibuException("FibuDaten schreiben \n SQLState: "
					+ e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	/**
	 * Schreibt die allgemeinen Fibu-Daten in die DB.
	 */
	public void writeFibuName(String fibuTitel) throws FibuException {
		Trace.println(3, "FibuData.writeFibuName()");
		try {
			PreparedStatement updateFibu = getConnection().prepareStatement(
				"UPDATE FibuDaten SET FibuTitel = ? WHERE ID = 1");
			updateFibu.setString(1, fibuTitel);
			updateFibu.executeUpdate();
		} catch (java.sql.SQLException e) {
			throw new FibuException("FibuDaten schreiben \n SQLState: "
					+ e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	
//	public void releaseService(BeanContextServices bcs, Object requestor,
//			Object service) {
//		// do nothing
//	}

	// --- getter und setter -------------------------------
	public String getFibuTitel() {
		return mFibuTitel;
	}
	
	public void setFibuName(String name) {
		mFibuName = name;
	}

	public String getFibuName() {
		return mFibuName;
	}


}
