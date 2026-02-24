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
 * Model der gespeicherten bank von denen CSV gelesen werden.
 * Auslesen als Iterator
 * @author Ruedi
 *
 */
public class CsvBankData extends DataBase {

	private final static String TABLE_NAME = "pdfcompany";

	/**
	 * Das SQL Statement für die Erstellung
	 */
	public final static String CREATE_CSVBANK_V1 = "CREATE TABLE `" + TABLE_NAME + "` ("
			 + "`CompanyID` int AUTO_INCREMENT PRIMARY KEY, "
			 + "`CompanyName` varchar(20) NOT NULL, "
			 + "`KontoNrDefault` varchar(6) DEFAULT NULL, "
			 + "`DirPath` varchar(100) DEFAULT NULL );";

	private final static String ADD_COL_V2 = "ALTER TABLE " + TABLE_NAME
			+ " ADD COLUMN TypeOfDoc SMALLINT(6) DEFAULT 1;";

	private final static String SET_INIT_V2 = "UPDATE `pdfcompany` SET `TypeOfDoc`=1 WHERE `TypeOfDoc` is NULL;";

	public final static String CREATE_CSVBANK_V2 = "CREATE TABLE `" + TABLE_NAME + "` ("
			 + "`CompanyID` int AUTO_INCREMENT PRIMARY KEY, "
			 + "`CompanyName` varchar(20) NOT NULL, "
			 + "`KontoNrDefault` varchar(6) DEFAULT NULL, "
			 + "`DirPath` varchar(100) DEFAULT NULL, "
			 + "`TypeOfDoc` SMALLINT(6) NULL DEFAULT NULL );";

	private final static String ADD_COL_V3 = "ALTER TABLE `" + TABLE_NAME + "`"
			+ "ADD COLUMN `WordBefore` VARCHAR(25) NULL DEFAULT NULL AFTER `TypeOfDoc`, "
			+ "ADD COLUMN `SpaltenArray` VARCHAR(20) NULL DEFAULT NULL AFTER `WordBefore`;";

	public final static String CREATE_CSVBANK_V3 = "CREATE TABLE `" + TABLE_NAME + "` ("
			 + "`CompanyID` int AUTO_INCREMENT PRIMARY KEY, "
			 + "`CompanyName` varchar(20) NOT NULL, "
			 + "`KontoNrDefault` varchar(6) DEFAULT NULL, "
			 + "`DirPath` varchar(100) DEFAULT NULL, "
			 + "`TypeOfDoc` SMALLINT(6) NULL DEFAULT NULL , "
			 + "`WordBefore` VARCHAR(25) NULL DEFAULT NULL, "
			 + "`SpaltenArray` VARCHAR(20) NULL DEFAULT NULL );";



	private final int COLS_V2 = 5;		// Anzahl Cols in der Version 2
	private final int COLS_V3 = 7;		// Anzahl Cols in der Version 3
	// die Anzahl Columns in der DB
	private int colsAnzahl = -1;

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
	 * Der Set mit den bank-Namen. Ist ein scrollable Set
	 */
	private ResultSet mReadSetName;

	/**
	 * Der Set mit den bank-ID. Ist ein scrollable Set
	 */
	private ResultSet mReadSetId;

	/**
	 * Der Set mit allen bank-Daten von dem gelesen wird. Ist ein scrollable
	 * Set der von allen Methoden verwendet wird.
	 */
	private ResultSet mReadSetAll;

	/**
	 * Model constructor comment.
	 */
	public CsvBankData() throws Exception {
		super();
	}


	/**
	 * Implementieren, wenn verschiedene Versionen der Tabelle vorhanden sind.
	 * Diese Methode wird nach dem Start der Fibu aufgerufen.
	 */
	@Override
	public void checkTableVersion() {
		try {
			if (FibuDataBase.tableExist(TABLE_NAME)) {
				colsAnzahl = getNumberOfCols();

				if (colsAnzahl < COLS_V2) {
					addColumnV2();
				}
				else {
					if (colsAnzahl < COLS_V3) {
						addColumnV3();
					}
				}
			}
			else {
				Statement stmt = getConnection().createStatement();
				stmt.execute(CREATE_CSVBANK_V3);
				stmt.close();
			}
		} catch (SQLException e) {
			Trace.println(3, "CsvBankData.checkTableVersion: " + e.getMessage());
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
			Trace.println(1, "error in: CsvBankData.getMaxRows: " + e.getMessage());
			mMaxRows = 0;
		}
		return mMaxRows;
	}


	/**
	 * Ein Eintrag für eine bank speichern, falls nicht vorhanden ist, wird
	 * ein neues Tupel angelegt.
	 */
	public void addData(CsvBank pCompany) throws FibuException {
		try {
			if (findRow(pCompany.getBankName())) {
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
	public void updateAt(int row, CsvBank pCompany) throws FibuException {
		Trace.println(7, "CsvBankData.updateAt(" + row +")");

		try {
			setupReadSetAll();
			if (mReadSetAll.absolute(row + 1)) {
//				mReadSetName.refreshRow();
				mReadSetAll.updateInt(1, pCompany.getBankID());
				mReadSetAll.updateString(2, pCompany.getBankName());
				mReadSetAll.updateString(3, pCompany.getKontoNrDefault());
				mReadSetAll.updateString(4, pCompany.getDirPath());
				mReadSetAll.updateInt(5, pCompany.getDocType());
				mReadSetAll.updateString(6, pCompany.getWordBefore());
				mReadSetAll.updateString(7, pCompany.getSpaltenArray());
				mReadSetAll.updateRow();
			}
		}
		catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new FibuException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	/**
	 * Die CsvBank mit dem suchWort wird zurückgegeben. Wenn nicht gefunden wird
	 * FibuException geworfen.
	 */
	public CsvBank readData(String companyName) throws FibuException {
		CsvBank lCompany = new CsvBank();
		lCompany.setBankName(companyName);
		try {
			if (findRow(companyName)) {
				mReadSetName.refreshRow();
				return copyCompanyValues(mReadSetName);
			} else {
				throw new FibuException("CompanyName: " + companyName);
			}
		} catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new FibuException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	/**
	 * Die CsvBank mit dem suchWort wird zurückgegeben. Wenn nicht gefunden wird
	 * FibuException geworfen.
	 */
	public CsvBank readData(int companyId) throws FibuException {
		try {
			if (findRow(companyId)) {
				mReadSetName.refreshRow();
				return copyCompanyValues(mReadSetName);
			} else {
				throw new FibuException("CompanyId: " + companyId);
			}
		} catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new FibuException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}


	/**
	 * Die CsvBank an der Position zurückgeben.
	 *  Wenn nicht gefunden wird FibuException geworfen.
	 *
	 * @param row, erste row = 1
	 * @return
	 * @throws FibuException
	 */
	public CsvBank readAt(int row) throws FibuException {
		Trace.println(7, "CsvBankData.readAt(" + row +")");
		mReadSetAll = null;	// zurücksetzen, da nicht von Anfang liest

		try {
			setupReadSetAll();
			if (mReadSetAll.absolute(row + 1)) {
//				mReadSetName.refreshRow();
				return copyCompanyValues(mReadSetAll);
			} else {
				throw new FibuException("CsvBankData: Zeile nicht gefunden");
			}
		} catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new FibuException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	/**
	 * Ein Tupel löschen
	 */
	public void deleteRow(CsvBank pCompany) throws SQLException {
		mReadSetName =	getReadSetName(pCompany.getBankName());
		if (findRow(pCompany.getBankName())) {
			mReadSetName.deleteRow();
		}
	}

	/**
	 * Alle Einräge löschen
	 * @param pCompany
	 * @throws SQLException
	 */
	public void deleteAll() throws SQLException {
		setupReadSetAll();
		mReadSetAll.beforeFirst();
		while (mReadSetAll.next()) {
			mReadSetAll.deleteRow();
		}
	}



	//----- private Methoden ------------------------------------------

	/**
	 * Die Daten einer bank lesen
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
	 * Die Daten einer bank über die ID
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
	 * Die Wert vom ReadSet in das Objekt kopieren
	 */
	private CsvBank copyCompanyValues(ResultSet readSet) throws SQLException {
		CsvBank lCompany = new CsvBank();
//		mReadSetAll = null;	// zurücksetzen, da nicht von Anfang liest
//		setupReadSetAll();
//
//		mReadSetAll.next();
		lCompany.setBankID(readSet.getInt(1));
		lCompany.setBankName(readSet.getString(2));
		lCompany.setKontoNrDefault(readSet.getString(3));
		lCompany.setDirPath(readSet.getString(4));
		lCompany.setDocType(readSet.getInt(5));
		lCompany.setWordBefore(readSet.getString(6));
		lCompany.setSpaltenArray(readSet.getString(7));
		return lCompany;
	}


	/**
	 * Eine neue Zeile (Row) in die Tabelle eintragen. Kopiert die Attribute
	 * Objekt PdfKeyword in den ResultSet. Der SQL-String wird zusammengestellt.
	 */
	private void addRow(CsvBank pCompany) throws SQLException {
		Statement stmt = getConnection().createStatement();
		StringBuffer lQuery = new StringBuffer("INSERT INTO PdfCompany VALUES (");
		if (pCompany.getBankID() == 0) {
			lQuery.append ("NULL, '");
		}
		else {
			lQuery.append("'");
			lQuery.append(pCompany.getBankID());
			lQuery.append("', '");
		}
		lQuery.append(pCompany.getBankName());
		lQuery.append("', '");
		lQuery.append(pCompany.getKontoNrDefault());
		lQuery.append("', '");
		lQuery.append(pCompany.getDirPath());
		lQuery.append("', '");
		lQuery.append(pCompany.getDocType());
		lQuery.append("', '");
		lQuery.append(pCompany.getWordBefore());
		lQuery.append("', '");
		lQuery.append(pCompany.getSpaltenArray());
		lQuery.append("')");

		stmt.executeUpdate(lQuery.toString());
		stmt.close();
		mReadSetName = null;
	}

	/**
	 * Aendert die Attribute der bank
	 */
	private void updateRow(CsvBank pCompany) throws SQLException {
		StringBuffer sql = new StringBuffer(200);
		sql.append("UPDATE PdfCompany SET CompanyName = ?, KontoNrDefault = ?, DirPath = ?, TypeOfDoc = ?,  "
				+ "WordBefore = ?, SpaltenArray = ? WHERE CompanyID = ");
		sql.append(pCompany.getBankID());
		sql.append(";");

		PreparedStatement updateCompany = getConnection().prepareStatement(sql.toString());
		updateCompany.setString(1, pCompany.getBankName());
		updateCompany.setString(2, pCompany.getKontoNrDefault());
		updateCompany.setString(3, pCompany.getDirPath());
		updateCompany.setInt(4, pCompany.getDocType());
		updateCompany.setString(5, pCompany.getWordBefore());
		updateCompany.setString(6, pCompany.getSpaltenArray());

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

	/**
	 * Version 1 erweitern
	 * @throws SQLException
	 */
	private void addColumnV2() throws SQLException {
		Statement stmt = getConnection(). createStatement();
		stmt.execute(ADD_COL_V2);
		stmt.execute(SET_INIT_V2);
		stmt.close();
	}

	/**
	 * Version 2 erweitern
	 * @throws SQLException
	 */
	private void addColumnV3() throws SQLException {
		Statement stmt = getConnection(). createStatement();
		stmt.execute(ADD_COL_V3);
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
	public Iterator<CsvBank> getIterator() {
		return new CsvCompanyIterator();
	}

	/** Iterator über alle Keywords */
	private class CsvCompanyIterator implements Iterator<CsvBank> {
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
		public CsvBank next() throws NoSuchElementException {
			try {
				CsvBank lCompany = new CsvBank();
				lCompany.setBankID(mReadSet.getInt(1));
				lCompany.setBankName(mReadSet.getString(2));
				lCompany.setKontoNrDefault(mReadSet.getString(3));
				lCompany.setDirPath(mReadSet.getString(4));
				lCompany.setDocType(mReadSet.getInt(5));
				if (colsAnzahl > COLS_V2) {
					lCompany.setWordBefore(mReadSet.getString(6));
					lCompany.setSpaltenArray(mReadSet.getString(7));
				}
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
