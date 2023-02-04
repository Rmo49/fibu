package com.rmo.fibu.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.util.Trace;

/**
 * Model der gespeicherten company von denen CSV gelesen werden.
 * Auslesen als Iterator
 * @author Ruedi
 *
 */
public class CsvCompanyData extends DataBase {
	
	private final String TABLE_NAME = "pdfcompany";
	
	/** 
	 * Das SQL Statement für die Erstellung
	 */
	public final static String CREATE_CSVCOMPANY_V1 = "CREATE TABLE `pdfcompany` ("
			 + "`CompanyID` int AUTO_INCREMENT PRIMARY KEY, "
			 + "`CompanyName` varchar(20) NOT NULL, "
			 + "`KontoNrDefault` varchar(6) DEFAULT NULL, "
			 + "`DirPath` varchar(100) DEFAULT NULL );";
	
	private final static String ADD_COL_CSVCOMPANY = "ALTER TABLE `pdfcompany`"
			+ " ADD COLUMN `TypeOfDoc` SMALLINT NULL DEFAULT NULL AFTER `DirPath`;";
	
	private final static String SET_INIT_VALUE = "UPDATE `pdfcompany` SET `TypeOfDoc`=1 WHERE `TypeOfDoc` is NULL;";

	public final static String CREATE_CSVCOMPANY_V2 = "CREATE TABLE `pdfcompany` ("
			 + "`CompanyID` int AUTO_INCREMENT PRIMARY KEY, "
			 + "`CompanyName` varchar(20) NOT NULL, "
			 + "`KontoNrDefault` varchar(6) DEFAULT NULL, "
			 + "`DirPath` varchar(100) DEFAULT NULL, "
			 + "`TypeOfDoc` SMALLINT(6) NULL DEFAULT NULL );";
			 
	
	private final int COLS_V1 = 4;		// Anzahl Cols in der Version 1
	private final int COLS_V2 = 5;		// Anzahl Cols in der Version 2
	
			 
	/**
	 * Die Anzahl Rows in der Tabelle. Wird beim Start berechnet, dann immer
	 * updated, da Probleme bei vielen Zugriffen
	 */
	private int mMaxRows;


	/**
	 * Enthält Connection zur DB. Wird in setupResultset gesetzt, bleibt während
	 * ganzer Sitzung erhalten.
	 */
	private Statement mReadStmt;

	/**
	 * Der Set mit den Company-Namen. Ist ein scrollable Set
	 */
	private ResultSet mReadSetName;

	/**
	 * Der Set mit den Company-ID. Ist ein scrollable Set
	 */
	private ResultSet mReadSetId;

	/**
	 * Der Set mit allen Company-Daten von dem gelesen wird. Ist ein scrollable
	 * Set der von allen Methoden verwendet wird.
	 */
	private ResultSet mReadSetAll;
	
	/**
	 * Model constructor comment.
	 */
	public CsvCompanyData() throws Exception {
		super();
	}

	
	/**
	 * Implementieren, wenn verschiedene Versionen der Tabelle vorhanden sind.
	 * Diese Methode wird nach dem Start der Fibu aufgerufen.
	 */
	public void checkTableVersion() {
		try {
			int colsAnzahl = getNumberOfCols();
			if (colsAnzahl < COLS_V2) {
				addColumnV1();
			}			
		} catch (SQLException e) {
			System.err.println("CsvCompanyData.checkTableVersion: " + e.getMessage());
		}
	}


	/**
	 * Max. Anzahl Zeilen in der Tabelle berechnen Werden im mMaxRows
	 * gespeichert.
	 */
	public int getRowCount() {
		try {
			Statement stmt = getConnection().createStatement();
			String lQuery = "SELECT Count(*) FROM " + TABLE_NAME +";";
			ResultSet lResult = stmt.executeQuery(lQuery);
			if (lResult.next()) {
				mMaxRows = lResult.getInt(1);
				lResult.close();
			}
		} catch (SQLException e) {
			System.err.println("CsvCompanyData.getMaxRows: " + e.getMessage());
			mMaxRows = 0;
		}
		return mMaxRows;
	}
	

	/**
	 * Ein Eintrag für eine Company speichern, falls nicht vorhanden ist, wird
	 * ein neues Tupel angelegt.
	 */
	public void addData(CsvCompany pCompany) throws FibuException {
		try {
			if (findRow(pCompany.getCompanyName())) {
				updateRow(pCompany);
			} else {
				// wenn nicht gefunden, neues anlegen
				addRow(pCompany);
			}
		} catch (java.sql.SQLException e) {
			throw new FibuException("PdfKeyword.add() \n Message: " + e.getMessage());
		}
	}


	/**
	 * Aendert die Attribute der gewählten Zeile.
	 */
	public void updateAt(int row, CsvCompany pCompany) throws FibuException {
		Trace.println(7, "CsvCompanyData.updateAt(" + row +")");
		
		try {
			setupReadSetAll();
			if (mReadSetAll.absolute(row + 1)) {
//				mReadSetName.refreshRow();
				mReadSetAll.updateInt(1, pCompany.getCompanyID());
				mReadSetAll.updateString(2, pCompany.getCompanyName());
				mReadSetAll.updateString(3, pCompany.getKontoNrDefault());
				mReadSetAll.updateString(4, pCompany.getDirPath());
				
				mReadSetAll.updateRow();
			}
		}
		catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new FibuException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}
	
	/**
	 * Die CsvCompany mit dem suchWort wird zurückgegeben. Wenn nicht gefunden wird
	 * FibuException geworfen.
	 */
	public CsvCompany readData(String companyName) throws FibuException {
		CsvCompany lCompany = new CsvCompany();
		lCompany.setCompanyName(companyName);
		try {
			if (findRow(companyName)) {
				mReadSetName.refreshRow();
				lCompany.setCompanyID(mReadSetName.getInt(1));
				lCompany.setCompanyName(mReadSetName.getString(2));
				lCompany.setKontoNrDefault(mReadSetName.getString(3));
				lCompany.setDirPath(mReadSetName.getString(4));
				return lCompany;
			} else {
				throw new FibuException("CompanyName: " + companyName);
			}
		} catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new FibuException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}
	
	/**
	 * Die CsvCompany mit dem suchWort wird zurückgegeben. Wenn nicht gefunden wird
	 * FibuException geworfen.
	 */
	public CsvCompany readData(int companyId) throws FibuException {
		CsvCompany lCompany = new CsvCompany();
		try {
			if (findRow(companyId)) {
				mReadSetName.refreshRow();
				lCompany.setCompanyID(mReadSetName.getInt(1));
				lCompany.setCompanyName(mReadSetName.getString(2));
				lCompany.setKontoNrDefault(mReadSetName.getString(3));
				lCompany.setDirPath(mReadSetName.getString(4));
				return lCompany;
			} else {
				throw new FibuException("CompanyId: " + companyId);
			}
		} catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new FibuException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}
	
	
	/**
	 * Die CsvCompany an der Position zurückgeben.
	 *  Wenn nicht gefunden wird FibuException geworfen.
	 * 
	 * @param row, erste row = 1
	 * @return
	 * @throws FibuException
	 */
	public CsvCompany readAt(int row) throws FibuException {
		Trace.println(7, "CsvCompanyData.readAt(" + row +")");
		CsvCompany lCompany = new CsvCompany();
		mReadSetAll = null;	// zurücksetzen, da nicht von Anfang liest

		try {
			setupReadSetAll();
			if (mReadSetAll.absolute(row + 1)) {
//				mReadSetName.refreshRow();
				lCompany.setCompanyID(mReadSetAll.getInt(1));
				lCompany.setCompanyName(mReadSetAll.getString(2));
				lCompany.setKontoNrDefault(mReadSetAll.getString(3));
				lCompany.setDirPath(mReadSetAll.getString(4));
				return lCompany;
			} else {
				throw new FibuException("CsvCompanyData: Zeile nicht gefunden");
			}
		} catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new FibuException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}
	
	/**
	 * Ein Tupel löschen
	 */
	public void deleteRow(CsvCompany pCompany) throws SQLException {
		mReadSetName =	getReadSetName(pCompany.getCompanyName());
		if (findRow(pCompany.getCompanyName())) {
			mReadSetName.deleteRow();
		}
	}

	//----- private Methoden ------------------------------------------
	
	/**
	 * Die Daten einer Company lesen
	 * @param pCompany
	 * @return true wenn gefunden
	 * @throws SQLException
	 */
	private boolean findRow(String pCompanyName) throws SQLException {
		mReadSetName = getReadSetName(pCompanyName);
		mReadSetName.beforeFirst();
		if (mReadSetName.next()) {
			return true;
		}
		return false;
	}

	/**
	 * Die Daten einer Company über die ID
	 * @param pCompany
	 * @return true wenn gefunden
	 * @throws SQLException
	 */
	private boolean findRow(int pCompanyId) throws SQLException {
		mReadSetId = getReadSetId(pCompanyId);
		mReadSetId.beforeFirst();
		if (mReadSetId.next()) {
			return true;
		}
		return false;
	}

	/**
	 * Eine neue Zeile (Row) in die Tabelle eintragen. Kopiert die Attribute 
	 * Objekt PdfKeyword in den ResultSet. Der SQL-String wird zusammengestellt.
	 */
	private void addRow(CsvCompany pCompany) throws SQLException {
		Statement stmt = getConnection().createStatement();
		StringBuffer lQuery = new StringBuffer("INSERT INTO PdfCompany VALUES (");
		if (pCompany.getCompanyID() == 0) {
			lQuery.append ("NULL, '");
		}
		else {
			lQuery.append("'");
			lQuery.append(pCompany.getCompanyID());
			lQuery.append("', '");
		}
		lQuery.append(pCompany.getCompanyName());
		lQuery.append("', '");
		lQuery.append(pCompany.getKontoNrDefault());
		lQuery.append("', '");
		lQuery.append(pCompany.getDirPath());
		lQuery.append("', '");
		lQuery.append(pCompany.getTypeOfDoc());
		lQuery.append("')");
		
		stmt.executeUpdate(lQuery.toString());
		stmt.close();
		mReadSetName = null;
	}

	/**
	 * Aendert die Attribute der Company
	 */
	private void updateRow(CsvCompany pCompany) throws SQLException {
		StringBuffer sql = new StringBuffer(200);
		sql.append("UPDATE PdfCompany SET CompanyName = ?, KontoNrDefault = ?, DirPath = ? WHERE CompanyID = ");
		sql.append(pCompany.getCompanyID());
		sql.append(";");
	
		PreparedStatement updateCompany = getConnection().prepareStatement(sql.toString());
		updateCompany.setString(1, pCompany.getCompanyName());
		updateCompany.setString(2, pCompany.getKontoNrDefault());
		updateCompany.setString(3, pCompany.getDirPath());
		updateCompany.executeUpdate();
	}
	
	
	//----- Tabelle migrieren -----------------------------

	/**
	 * Die Anzahl Spalten der Tabelle.
	 * @return
	 */
	private int getNumberOfCols() throws SQLException {
		Statement stmt = getConnection().createStatement();
//		String lQuery = "SELECT count(*) AS NUMBEROFCOLUMNS FROM information_schema.columns "
//				+ "WHERE table_name = '"
//				+ TABLE_NAME
//				+ "';";
		String lQuery = "SELECT * from " + TABLE_NAME + ";";
		ResultSet lResult = stmt.executeQuery(lQuery);
		ResultSetMetaData rsmd = lResult.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		return columnsNumber;
	}

	private void addColumnV1() throws SQLException {
		Statement stmt = getConnection(). createStatement();
		stmt.execute(ADD_COL_CSVCOMPANY);
		stmt.execute(SET_INIT_VALUE);
		stmt.close();
	}
	
	//--- SQL Statements ---------------------------------------------------
	/**
	 * Setzt das Statement (Connection zur DB) und den Scroll-Set, der für
	 * Insert oder update verwendet werden kann.
	 */
	private synchronized void setupReadSetAll() throws SQLException {
		if (mReadStmt == null) {
			mReadStmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
		if (mReadSetAll == null) {
			mReadSetAll = mReadStmt.executeQuery("SELECT * FROM PdfCompany ORDER BY CompanyID");
		}
	}

	/**
	 * Setzt das Statement (Connection zur DB) und den Scroll-Set, der für
	 * Insert oder update verwendet werden kann.
	 */
	private synchronized ResultSet getReadSetName (String pCompany) throws SQLException {
		if (mReadStmt == null) {
			mReadStmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
		return mReadStmt.executeQuery("SELECT * FROM PdfCompany WHERE CompanyName = '" + pCompany + "'");
	}

	/**
	 * Setzt das Statement (Connection zur DB) und den Scroll-Set, der für
	 * Insert oder update verwendet werden kann.
	 */
	private synchronized ResultSet getReadSetId (int pCompanyId) throws SQLException {
		if (mReadStmt == null) {
			mReadStmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
		return mReadStmt.executeQuery("SELECT * FROM PdfCompany WHERE CompanyId = '" + pCompanyId + "'");
	}


	// ----- Iterator ---------------------------------------------

	/** Gibt einen Iterator zurück */
	public Iterator<CsvCompany> getIterator() {
		return new CsvCompanyIterator();
	}

	/** Iterator über alle Keywords */
	private class CsvCompanyIterator implements Iterator<CsvCompany> {
		private Statement mReadStmt;
		private ResultSet mReadSet;

		/**
		 * Konstruktur, setzt den ReadSet.
		 * @param companyId
		 */
		CsvCompanyIterator() {
			try {
				mReadStmt = getConnection()
						.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				mReadSet = mReadStmt.executeQuery("SELECT * FROM PdfCompany ORDER BY CompanyID");
				mReadSet.beforeFirst();
			} catch (SQLException ex) {
			}
		}

		/**
		 * Iterator, auf nächsten Eintrag setzen.
		 * return true wenn Eintrag vorhanden, sonst false.
		 */
		@Override
		public boolean hasNext() {
			try {
				if (mReadSet.next()) {
					return true;
				} else {
					mReadStmt.close();
					mReadSet.close();
					return false;
				}
			} catch (SQLException ex) {
				return false;
			}
		}

		/**
		 * Iterator, gibt den nächsten Eintrag zurück.
		 * mReadSet muss mit hasNext auf diesen zeigen.
		 */
		@Override
		public CsvCompany next() throws NoSuchElementException {
			try {
				CsvCompany lCompany = new CsvCompany();
				lCompany.setCompanyID(mReadSet.getInt(1));
				lCompany.setCompanyName(mReadSet.getString(2));
				lCompany.setKontoNrDefault(mReadSet.getString(3));
				lCompany.setDirPath(mReadSet.getString(4));
				return lCompany;
			} catch (SQLException ex) {
				throw new NoSuchElementException(ex.getMessage());
			}
		}

		@Override
		public void remove() {
			// not implemented
		}
	}
}
