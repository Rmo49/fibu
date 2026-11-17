package com.rmo.fibu.model.depot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.DataObject;

public class PositionData extends DataObject {

	/**
	 * Für die DB zur Erstellung der Tabellen
	 */
	public final static String CREATE_POSITION = "CREATE TABLE Position ("
			+ "StockNr varchar(10) NOT NULL, "
			+ "Produkt varchar(30) NOT NULL, "
			+ "Bezeichung varchar(30) DEFAULT NULL, "
			+ "Einstand decimal(12,2) NOT NULL, Kurs decimal(12,2) DEFAULT NULL, "
			+ "Kaufdatum date NOT NULL, Verkaufdatum date DEFAULT NULL,"
			+ "PRIMARY KEY (StockNr), "
			+ "INDEX fk_stock (StockNr ASC));";

	private static PositionData positionData;
	
	private PositionData() {};
	
	/**
	 * Singelton
	 */
	public static PositionData getInstance() {
		if (positionData == null) {
			positionData = new PositionData();
		}
		return positionData;
	}
	
	
	/**
	 * Enthält Connection zur DB. Wird in setupResultset gesetzt, bleibt während
	 * ganzer Sitzung erhalten.
	 */
	private Statement mReadStmt;
	
	/**
	 * Der Set mit allen Konto-Daten von dem gelesen wird. Ist ein scrollable Set
	 * der von allen Methoden verwendet wird. KontoNr: LongInteger <br>
	 * KontoText: String <br>
	 * StartSaldo: Double (Währung) <br>
	 * Saldo: Double (Währung) <br>
	 * IstSollKto: boolean
	 */
	private ResultSet mReadSet;


	/**
	 * Implementieren, wenn verschiedene Versionen der Tabelle vorhanden sind. Diese
	 * Methode wird nach dem Start der Fibu aufgerufen.
	 */
	@Override
	public void checkTableVersion() {
		// nix zum tun
	}

	public void add(Position pPosition) throws SQLException {
			addRow(pPosition);
	}
	
	/**
	 * Das Position mit der StockNr wird zurückgegeben. Wenn nicht gefunden wird
	 * FibuException geworfen.
	 */
	public Position read(String pStockNr) throws FibuException {
		try {
			if (findRow(pStockNr)) {
				Position lPosition = new Position();
				lPosition.setStockNr(pStockNr);
				mReadSet.refreshRow();
				copyToPosition(lPosition);
				return lPosition;
			} else {
				throw new FibuException("Position: " + pStockNr);
			}
		} catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new KontoNotFoundException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	
	//---- interne Methoden -------------------------------
	
	/**
	 * Eine neue Zeile (Row) in die Tabelle eintragen. Kopiert die Attribute vom
	 * ResultSet in das Objekt Konto. Der SQL-String wird zusammengestellt.
	 */
	private void addRow(Position pPosition) throws SQLException {
		Statement stmt = getConnectionDepot().createStatement();
		StringBuffer lQuery = new StringBuffer("INSERT INTO Position VALUES ('");
		lQuery.append(pPosition.getStockNr());
		lQuery.append("', '");
		lQuery.append(pPosition.getProdukt());
		lQuery.append("', '");
		lQuery.append(pPosition.getBezeichnung());
		lQuery.append("', ");
		lQuery.append(pPosition.getEinstand());
		lQuery.append(", ");
		lQuery.append(pPosition.getKurs());
		lQuery.append(", '");
		java.sql.Date lDate = pPosition.getKaufDatum().asSqlDate();
		lQuery.append(lDate.toString());
		lQuery.append("', ");
		if (pPosition.getVerkaufDatum() != null) {
			lDate = pPosition.getVerkaufDatum().asSqlDate();
			lQuery.append("'");
			lQuery.append(lDate.toString());
			lQuery.append("'");			
		}
		else {
			lQuery.append("NULL");
		}
		lQuery.append(");");
		// String lQ = lQuery.toString();
		stmt.executeUpdate(lQuery.toString());
		stmt.close();
	}

	/**
	 * Sucht die Row mit der Stocknummer. Wenn true, steht mReadSet auf dieser
	 * Zeile.
	 */
	private boolean findRow(String pStockNr) throws SQLException {
		setupReadSet();
		mReadSet.beforeFirst();
		while (mReadSet.next()) {
			if (mReadSet.getString(1).compareTo(pStockNr)== 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Kopiert die Attribute vom ResultSet in das Objekt Konto
	 */
	private void copyToPosition(Position pPosition) throws SQLException {
		pPosition.setProdukt(mReadSet.getString(2));
		pPosition.setBezeichnung(mReadSet.getString(3));
		pPosition.setEinstand(mReadSet.getDouble(4));
		pPosition.setKurs(mReadSet.getDouble(5));
		pPosition.setKaufDatum(mReadSet.getDate(6));
		pPosition.setVerkaufDatum(mReadSet.getDate(7));
	}

	
	/**
	 * Setzt das Statement (Connection zur DB) und den Scroll-Set, der für Insert
	 * oder update verwendet werden kann.
	 */
	private synchronized void setupReadSet() throws SQLException {
		if (mReadStmt == null) {
			mReadStmt = getConnectionDepot().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
		if (mReadSet == null) {
			mReadSet = mReadStmt.executeQuery("SELECT * FROM Position ORDER BY StockNr");
		}
	}

	
}
