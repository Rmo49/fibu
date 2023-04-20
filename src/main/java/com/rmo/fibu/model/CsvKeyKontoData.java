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
 * Model der gespeicherten keywords für CSV. Diese können gespeichert oder
 * geändert werden (add). Auslesen als Iterator
 *
 * @author Ruedi
 *
 */
public class CsvKeyKontoData extends DataBase {
	
	private final static String TABLE_NAME = "pdfkeyword";
	
	public final static String CREATE_CSVKEYWORD = "CREATE TABLE `" + TABLE_NAME + "`("
			+ " `ID` int(11) NOT NULL AUTO_INCREMENT, `CompanyID` int,"
			+ " `SuchWort` varchar(20) NOT NULL,"
			+ " `KontoNr` varchar(6) DEFAULT NULL,"
			+ " `SH` varchar(2) DEFAULT NULL,"
			+ " PRIMARY KEY (`ID`,`CompanyID`) );";
//			+ " FOREIGN KEY (CompanyID) REFERENCES pdfcompany(CompanyID) );";
	
	private final int COLS_V1 = 5;		// Anzahl Cols in der Version1
	
	private final static String ADD_COL_V2 = "ALTER TABLE `" + TABLE_NAME + "`"
			+ "ADD COLUMN `textNeu` VARCHAR(20) NULL DEFAULT NULL AFTER `SH`;";

	public final static String CREATE_CSVKEYWORD_V2 = "CREATE TABLE `" + TABLE_NAME + "`("
			+ " `ID` int(11) NOT NULL AUTO_INCREMENT, `CompanyID` int,"
			+ " `SuchWort` varchar(20) NOT NULL,"
			+ " `KontoNr` varchar(6) DEFAULT NULL,"
			+ " `SH` varchar(2) DEFAULT NULL,"
			+ " `textNeu` VARCHAR(20) DEFAULT NULL,"
			+ " PRIMARY KEY (`ID`,`CompanyID`) );";
//			+ " FOREIGN KEY (CompanyID) REFERENCES pdfcompany(CompanyID) );";

	/**
	 * Enthält Connection zur DB. Wird in setupResultset gesetzt, bleibt während
	 * ganzer Sitzung erhalten.
	 */
	private Statement mReadStmt;

	/**
	 * Der Set mit allen Keyword-Daten von dem gelesen wird. Ist ein scrollable Set
	 * der von allen Methoden verwendet wird.
	 */
	private ResultSet mResultSet;

	/**
	 * Die Anzahl Rows in der Tabelle. Wird beim Start berechnet, dann immer
	 * updated, da Probleme bei vielen Zugriffen
	 */
	private int mMaxRows = 0;

	/**
	 * Die Anzahl Cols, Version 1: 4, Version 2: 5, Version 3: 6
	 */
	private int mAnzahlCols = 0;

	/**
	 * Die Version 1: 4, Version 2: 5, Version 3: 6
	 */
	private int mVersion = 0;

	/**
	 * Model constructor comment.
	 */
	public CsvKeyKontoData() throws Exception {
		super();
		init();
	}

	/**
	 * Initialisierung der Variablen
	 */
	private void init() {
		mMaxRows = 0;
		getVersion();
	}

	/**
	 * Implementieren, wenn verschiedene Versionen der Tabelle vorhanden sind.
	 * Diese Methode wird nach dem Start der Fibu aufgerufen.
	 */
	@Override
	public void checkTableVersion() {
		try {
			if (FibuDataBase.tableExist(TABLE_NAME)) {
				int colsAnzahl = getAnzahlCols();

				if (colsAnzahl <= COLS_V1) {
					addColumnV2();
				}
			}
		} catch (SQLException e) {
			Trace.println(1, "error in: CsvCompanyData.checkTableVersion: " + e.getMessage());
		}
	}

	/**
	 * Version 1 erweitern
	 * @throws SQLException
	 */
	private void addColumnV2() throws SQLException {
		Statement stmt = getConnection().createStatement();
		stmt.execute(ADD_COL_V2);
		stmt.close();
	}


	/**
	 * Einen leeren Eintrag speichern, ein neues Tupel wird angelegt.
	 */
	public void addEmptyRow(CsvKeyKonto pKeyword) throws FibuException {
		try {
			addRow(pKeyword);
		} catch (java.sql.SQLException e) {
			throw new FibuException("CsvKeyKontoData.add() \n Message: " + e.getMessage());
		}
	}

	/**
	 * Ein Eintrag für eine Keyword speichern, falls nicht vorhanden ist, wird ein
	 * neues Tupel angelegt.
	 */
	public void add(CsvKeyKonto pKeyword) throws FibuException {
		try {
			if (findRow(pKeyword)) {
				updateRow(pKeyword);
			} else {
				// wenn nicht gefunden, neues anlegen
				addRow(pKeyword);
			}
		} catch (java.sql.SQLException e) {
			throw new FibuException("CsvKeyKontoData.add() \n Message: " + e.getMessage());
		}
	}

	/**
	 * Max. Anzahl Zeilen in der Tabelle.
	 */
	public int getRowCount(int companyId) {
//		if (mMaxRows == 0) {
		calculateMaxRows(companyId);
//		}
		return mMaxRows;
	}

	/**
	 * Das Keywort an der Stelle position (0..x) zurückgeben.
	 *
	 * @return Keywort an der position, null wenn nicht vorhanden
	 */
	public CsvKeyKonto readAt(int companyId, int position) throws FibuException {
		Trace.println(7, "CsvKeyKontoData.readAt()");
		CsvKeyKonto lKeyword = new CsvKeyKonto();
		lKeyword.setCompanyId(companyId);
		try {
			setupReadSet(companyId);
			if (mResultSet.absolute(position + 1)) {
				// erste Version: companyID, SuchWort, KontoNr, SH
				if (mVersion == 1) {
					lKeyword.setId(0);
					lKeyword.setCompanyId(companyId);
					lKeyword.setSuchWort(mResultSet.getString(2));
					lKeyword.setKontoNr(mResultSet.getString(3));
					lKeyword.setSh(mResultSet.getString(4));
				}
				// 2. Version: ID, companyID, SuchWort, KontoNr, SH
				if (mVersion == 2) {
					lKeyword.setId(mResultSet.getInt(1));
					lKeyword.setCompanyId(companyId);
					lKeyword.setSuchWort(mResultSet.getString(3));
					lKeyword.setKontoNr(mResultSet.getString(4));
					lKeyword.setSh(mResultSet.getString(5));
				}
				// 3. Version: ID, companyID, SuchWort, KontoNr, SH, TextNeu
				if (mVersion == 3) {
					lKeyword.setId(mResultSet.getInt(1));
					lKeyword.setCompanyId(companyId);
					lKeyword.setSuchWort(mResultSet.getString(3));
					lKeyword.setKontoNr(mResultSet.getString(4));
					lKeyword.setSh(mResultSet.getString(5));
					lKeyword.setTextNeu(mResultSet.getString(6));
				}

				return lKeyword;
			} else {
				throw new FibuException("Keyword an Position: " + position + " nicht gefunden");
			}
		} catch (java.sql.SQLException e) {
			throw new FibuException("Keyword an Position: " + position + " Message: " + e.getMessage());
		}
	}

	/**
	 * Das Tupel an der Position (0..x) ändern.
	 */
	public void updateAt(int position, CsvKeyKonto pKeyword) throws FibuException {
		Trace.println(7, "CsvKeyKontoData.updateAt()");
		try {
			setupReadSet(pKeyword.getCompanyId());
			if (mResultSet.absolute(position + 1)) {
				int i = 1;
				if (mVersion >= 2) {
					i++;
				}
//				mResultSet.updateInt(1, pKeyword.getId());
				mResultSet.updateInt(i, pKeyword.getCompanyId());
				mResultSet.updateString(++i, pKeyword.getSuchWort());
				mResultSet.updateString(++i, pKeyword.getKontoNr());
				mResultSet.updateString(++i, pKeyword.getSh());
				if (mVersion >= 3) {
					mResultSet.updateString(++i, pKeyword.getTextNeu());
				}
				mResultSet.updateRow();
			} else {
				throw new FibuException("Keyword an Position: " + position + " nicht gefunden");
			}
		} catch (java.sql.SQLException e) {
			throw new FibuException("Keyword an Position: " + position + " Message: " + e.getMessage());
		}
	}

	/**
	 * Das Tupel CsvKeyword ändern, der ResultSet steht bereits auf der richtigen Position
	 * CompanyId und Suchwort stimmen.
	 */
	public void updateRow(CsvKeyKonto pKeyword) throws FibuException {
		Trace.println(7, "CsvKeyKontoData.update()");

		try {
			int i = 1;
			if (mVersion >= 2) {
				i++;
			}
			mResultSet.updateInt(i, pKeyword.getCompanyId());
			mResultSet.updateString(++i, pKeyword.getSuchWort());
			mResultSet.updateString(++i, pKeyword.getKontoNr());
			mResultSet.updateString(++i, pKeyword.getSh());
			if (mVersion >= 3) {
				mResultSet.updateString(++i, pKeyword.getTextNeu());
			}
			mResultSet.updateRow();
		} catch (java.sql.SQLException e) {
			throw new FibuException("Keyword ändern, Message: " + e.getMessage());
		}
	}

	/**
	 * Das Tupel mit der ID löschen
	 */
	public void deleteRow(int id) throws SQLException {
		String sql = "DELETE FROM PdfKeyword WHERE id = ?";
		PreparedStatement ps = getConnection().prepareStatement(sql);
		ps.setInt(1, id);
		ps.executeUpdate();
		ps.close();
		mMaxRows--;
	}

	public void resetVersion() {
		mVersion = 0;
	}

	/**
	 * Die Version, abhängig von der Anzahl Felder in der DB
	 * 1: companyID, SuchWort, KontoNr, SH
	 * 2: ID, companyID, SuchWort, KontoNr, SH
	 * 3: ID, companyID, SuchWort, KontoNr, SH, TextNeu
	 * @return die aktuelle Version
	 */
	public int getVersion() {
		// wird nur das erstemal berechnet
		if (mVersion == 0) {
//			int cols = getAnzahlCols();
			if (getAnzahlCols() <=4) {
				mVersion = 1;
			}
			else if (getAnzahlCols() == 5) {
				mVersion = 2;
			}
			else {
				mVersion = 3;
			}
		}
		return mVersion;
	}

		
	/**
	 * Die Anzahl Felder in der DB
	 *
	 * @return
	 * @throws SQLException
	 */
	private int getAnzahlCols() {
		if (mAnzahlCols == 0) {
			try {
				Statement stmt = getConnection().createStatement();
				String lQuery = "SELECT * from " + TABLE_NAME + ";";
				ResultSet rs = stmt.executeQuery(lQuery);
				// wie viele columns
				ResultSetMetaData rsmd = rs.getMetaData();
				mAnzahlCols = rsmd.getColumnCount();
			} catch (SQLException e) {
				Trace.println(1, "error in: CsvKeyKontoData.getAnzahlCols: " + e.getMessage());
				mAnzahlCols = 0;
			}
		}
		return mAnzahlCols;
	}

	/**
	 * Einen Csv Eintrag suchen
	 *
	 * @param pKeyword
	 * @return
	 * @throws SQLException
	 */
	private boolean findRow(CsvKeyKonto pKeyword) throws SQLException {
		setupReadSet(pKeyword.getCompanyId());
		mResultSet.beforeFirst();
		while (mResultSet.next()) {
			if (mResultSet.getInt(2) == pKeyword.getCompanyId()) {
				if (mResultSet.getString(3).equalsIgnoreCase(pKeyword.getSuchWort())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Setzt das Statement (Connection zur DB) und den Scroll-Set, der für Insert
	 * oder update verwendet werden kann.
	 */
	private synchronized void setupReadSet(int companyId) throws SQLException {
		if (mReadStmt == null) {
			mReadStmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
		mResultSet = mReadStmt
				.executeQuery("SELECT * FROM PdfKeyword WHERE CompanyId = '" + companyId + "' ORDER BY SuchWort");
	}

	/**
	 * Eine neue Zeile (Row) in die Tabelle eintragen. Kopiert die Attribute Objekt
	 * PdfKeyword in den ResultSet. Der SQL-String wird zusammengestellt.
	 */
	private int addRow(CsvKeyKonto pKeyword) throws SQLException {
		int id = 0;
		Statement stmt = getConnection().createStatement();
		StringBuffer lQuery = new StringBuffer(100);
		// nur für erste Version ohne ID
		if (mVersion == 1) {
			lQuery.append("INSERT INTO PdfKeyword VALUES ('");
		} else {
			// die erste Column (ID) leer, da Autoincrement
			if (pKeyword.getId() < 0) {
				lQuery.append("INSERT INTO PdfKeyword VALUES (null,'");
			}
			// wenn die ID bereits bekannt ist.
			else {
				lQuery.append("INSERT INTO PdfKeyword VALUES('");
				lQuery.append(pKeyword.getId());
				lQuery.append("', '");
			}
		}
		lQuery.append(pKeyword.getCompanyId());
		lQuery.append("', '");
		lQuery.append(pKeyword.getSuchWort());
		lQuery.append("', '");
		lQuery.append(pKeyword.getKontoNr());
		lQuery.append("', '");
		lQuery.append(pKeyword.getSh());
		if (mVersion >= 3) {
			lQuery.append("', '");
			lQuery.append(pKeyword.getTextNeu());
		}
		lQuery.append("')");
		stmt.executeUpdate(lQuery.toString());
		stmt.close();
		mResultSet = null;
		mMaxRows++;
		return id;
	}

	/**
	 * Max. Anzahl Zeilen in der Tabelle berechnen Werden im mMaxRows gespeichert.
	 */
	private synchronized void calculateMaxRows(int companyId) {
		try {
			Statement stmt = getConnection().createStatement();
			String lQuery = "SELECT Count(*) FROM PdfKeyword WHERE CompanyId = '" + companyId + "'";
			ResultSet lResult = stmt.executeQuery(lQuery);
			if (lResult.next()) {
				mMaxRows = lResult.getInt(1);
				lResult.close();
			}
		} catch (SQLException e) {
			Trace.println(1, "errir in: PdfKeyword.getMaxRows: " + e.getMessage());
			mMaxRows = 0;
		}
	}

	// ----- Iterator ---------------------------------------------

	/** Gibt einen Iterator zurück */
	public Iterator<CsvKeyKonto> getIterator(int companyId) {
		return new CsvKeywordIterator(companyId);
	}

	/** Iterator über alle Keywords */
	private class CsvKeywordIterator implements Iterator<CsvKeyKonto> {
		private Statement mReadStmt;
		private ResultSet mReadSet;

		/**
		 * Konstruktur, setzt den ReadSet.
		 *
		 * @param companyId
		 */
		CsvKeywordIterator(int companyId) {
			try {
				mReadStmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
				mReadSet = mReadStmt.executeQuery(
						"SELECT * FROM PdfKeyword WHERE CompanyId = '" + companyId + "' ORDER BY SuchWort");
				mReadSet.beforeFirst();
			} catch (SQLException ex) {
			}
		}

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

		@Override
		public CsvKeyKonto next() throws NoSuchElementException {
			try {
				CsvKeyKonto lPdfKeyword = new CsvKeyKonto();
				copyToKeyword(mReadSet, lPdfKeyword);
				return lPdfKeyword;
			} catch (SQLException ex) {
				throw new NoSuchElementException(ex.getMessage());
			}
		}

		@Override
		public void remove() {
			// not implemented
		}
	}

	/**
	 * Kopiert die Attribute vom ResultSet in das Objekt Keyword
	 */
	private void copyToKeyword(ResultSet pResult, CsvKeyKonto pKeyword) throws SQLException {
		int i = 1;
		if (mVersion >= 2) {
			pKeyword.setId(pResult.getInt(i));
			i++;
		}
		pKeyword.setCompanyId(pResult.getInt(i));
		pKeyword.setSuchWort(pResult.getString(++i));
		pKeyword.setKontoNr(pResult.getString(++i));
		pKeyword.setSh(pResult.getString(++i));
		if (mVersion >= 3) {
			pKeyword.setTextNeu(pResult.getString(++i));
		}
	}

}
