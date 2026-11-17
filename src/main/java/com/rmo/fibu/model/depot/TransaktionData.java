package com.rmo.fibu.model.depot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.rmo.fibu.exception.BuchungValueException;
import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.DataObject;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.util.Trace;


/**
 * Alle Buchungen zum den Positions
 */
public class TransaktionData extends DataObject {

	/**
	 * Für die DB zur Erstellung der Tabellen
	 */
	public final static String CREATE_TRANSAKTION = "CREATE TABLE Transaktion ("
			+ "Datum date NOT NULL, StockNr varchar(10) NOT NULL, "
			+ "Text varchar(30) DEFAULT NULL, " 
			+ "Betrag decimal(12,2) DEFAULT NULL, "
			+ "INDEX fk_datum (Datum ASC), " 
			+ "CONSTRAINT fk_stock_depot FOREIGN KEY (StockNr) " 
			+ "REFERENCES Position (StockNr) "
			+ "ON DELETE RESTRICT ON UPDATE NO ACTION);";

	private static TransaktionData transaktionData;
	
	private TransaktionData() {};
	
	/**
	 * Singelton
	 */
	public static TransaktionData getInstance() {
		if (transaktionData == null) {
			transaktionData = new TransaktionData();
		}
		return transaktionData;
	}

	
	/**
	 * Enthält Connection zur DB. Wird in setupResultset gesetzt, bleibt während
	 * ganzer Sitzung erhalten.
	 */
	private Statement mReadStmt;
	
	/**
	 * Der Set mit allen Zeilen von dem gelesen wird. Ist ein scrollable Set
	 * der von allen Methoden verwendet wird.
	 */
	private ResultSet mResultSet;
	
	/** Eine Liste min Transaktionen */
	private ArrayList<Transaktion> mListBuchung;


	/**
	 * Implementieren, wenn verschiedene Versionen der Tabelle vorhanden sind. Diese
	 * Methode wird nach dem Start der Fibu aufgerufen.
	 */
	@Override
	public void checkTableVersion() {
		// nix zum tun
	}

	public void add(Transaktion pPositionBuchung) throws SQLException {
			addRow(pPositionBuchung);
	}
	
	/**
	 * Die Buchungen mit der StockNr werden zurückgegeben. Wenn nicht gefunden wird
	 * FibuException geworfen.
	 */
	public List<Transaktion> readAll(String pStockNr) throws FibuException {
		try {			
			List<Transaktion> buchungList = findAllRow(pStockNr);
			if (buchungList.size() > 0) {
				return buchungList;
			}
			else {
				throw new FibuException("Nicht gefunden in Depot: " + pStockNr);
			}
		} catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new FibuException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	
	//---- interne Methoden -------------------------------
	
	/**
	 * Eine neue Zeile (Row) in die Tabelle eintragen. Kopiert die Attribute vom
	 * ResultSet in das Objekt Konto. Der SQL-String wird zusammengestellt.
	 */
	private void addRow(Transaktion pPositionBuchung) throws SQLException {
		Statement stmt = getConnectionDepot().createStatement();
		StringBuffer lQuery = new StringBuffer("INSERT INTO transaktion VALUES ('");
		java.sql.Date lDate = pPositionBuchung.getDatum().asSqlDate();
		lQuery.append(lDate.toString());
		lQuery.append("', '");
		lQuery.append(pPositionBuchung.getStockNr());
		lQuery.append("', '");
		lQuery.append(pPositionBuchung.getText());
		lQuery.append("', ");
		lQuery.append(pPositionBuchung.getBetrag());
		lQuery.append(");");
		// String lQ = lQuery.toString();
		stmt.executeUpdate(lQuery.toString());
		stmt.close();
	}

	/**
	 * Sucht die Row mit der Stocknummer. Wenn true, steht mReadSet auf dieser
	 * Zeile.
	 */
	private ArrayList<Transaktion> findAllRow(String pStockNr) throws SQLException {
		String query = "SELECT * FROM transaktion WHERE StockNr = '" + pStockNr + "';";
		setupReadStmt();
		mResultSet = mReadStmt.executeQuery(query);
//		mResultSet.beforeFirst();
		ArrayList<Transaktion> listBuchung = new ArrayList<Transaktion>();
		while (mResultSet.next()) {
			Transaktion pPositionBuchung = new Transaktion();
			copyToBuchung(pPositionBuchung);
			listBuchung.add(pPositionBuchung);
		}
		return listBuchung;
	}


	/**
	 * Kopiert die Attribute vom ResultSet in das Objekt Konto
	 */
	private Transaktion copyToBuchung(Transaktion pPositionBuchung) throws SQLException {
		Trace.println(7, "PositionBuchung.copyToBuchung(PositionBuchung)");
		pPositionBuchung.setDatum(mResultSet.getDate(1));
		pPositionBuchung.setStockNr(mResultSet.getString(2));
		pPositionBuchung.setText(mResultSet.getString(3));
		pPositionBuchung.setBetrag(mResultSet.getDouble(4));
		return pPositionBuchung;
	}

	
	/**
	 * Kopiert die Attribute vom ResultSet in das Objekt Buchung
	 */
	private Transaktion copyToBuchung(ResultSet pResult) throws SQLException {
		Trace.println(7, "PositionBuchung.copyToBuchung(ResultSet, Buchung)");
		Transaktion buchung = new Transaktion();
		buchung.setDatum(pResult.getDate(1));
		buchung.setStockNr(pResult.getString(2));
		buchung.setText(pResult.getString(3));
		buchung.setBetrag(pResult.getDouble(4));
		return buchung;
	}

	/**
	 * Setzt das Statement (Connection zur DB) und den Scroll-Set, der für Insert
	 * oder update verwendet werden kann.
	 */
	private synchronized void setupReadStmt() throws SQLException {
		if (mReadStmt == null) {
			mReadStmt = getConnectionDepot().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
	}

	
	/**
	 * Setzt das Statement (Connection zur DB) und den Scroll-Set, der für Insert
	 * oder update verwendet werden kann.
	 */
//	private synchronized void setupResultSet() throws SQLException {
//		setupReadStmt();
//		if (mResultSet == null) {
//			mResultSet = mReadStmt.executeQuery("SELECT * FROM Transaktion ORDER BY Datum");
//		}
//	}
	
	// ----- Iterator ---------------------------------------------
	/** Gibt einen Iterator zurück */
	public Iterator<Transaktion> getIterator() {
		return new BuchungIterator();
	}

	/** Iterator über alle gespeicherten PositionBuchungen */
	private class BuchungIterator implements Iterator<Transaktion> {
		private Statement mReadStmt;
		private ResultSet mResultSet;

		/** Konstruktor, setzt den Readset auf */
		BuchungIterator() {
			try {
				mReadStmt = getConnectionDepot().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
				mResultSet = mReadStmt.executeQuery("SELECT * FROM transaktion ORDER BY Datum");
				mResultSet.beforeFirst();
			} catch (SQLException ex) {
				// nix tun
			}
		}

		@Override
		public boolean hasNext() {
			try {
				if (mResultSet.next()) {
					return true;
				} else {
					mResultSet.close();
					mReadStmt.close();
					return false;
				}
			} catch (SQLException ex) {
				return false;
			}
		}

		@Override
		public Transaktion next() throws NoSuchElementException {
			try {
				Transaktion lBuchung = copyToBuchung(mResultSet);
				return lBuchung;
			} catch (SQLException ex) {
				throw new NoSuchElementException(ex.getMessage());
			}
		}

		@Override
		public void remove() {
			try {
				mResultSet.deleteRow();
			} catch (Exception ex) {
				// do nothing
			}

		}
	}


}

