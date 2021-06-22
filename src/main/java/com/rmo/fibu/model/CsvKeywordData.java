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
 * Model der gespeicherten keywords für CSV.
 * Diese können gespeichert oder geändert werden (add).
 * Auslesen als Iterator
 * @author Ruedi
 *
 */
public class CsvKeywordData extends DataModel {

	/**
	 * Enthält Connection zur DB. Wird in setupResultset gesetzt, bleibt während
	 * ganzer Sitzung erhalten.
	 */
	private Statement mReadStmt;

	/**
	 * Der Set mit allen Keyword-Daten von dem gelesen wird. Ist ein scrollable
	 * Set der von allen Methoden verwendet wird.
	 */
	private ResultSet mResultSet;
	
	/**
	 * Die Anzahl Rows in der Tabelle. Wird beim Start berechnet, dann immer
	 * updated, da Probleme bei vielen Zugriffen
	 */
	private int mMaxRows = 0;

	/**
	 * Die Anzahl Cols, Version 1: 4, Version 2: 5.
	 */
	private int mAnzahlCols = 0;

	/**
	 * Model constructor comment.
	 */
	public CsvKeywordData() throws Exception {
		super();
	}

	/**
	 * Einen leeren Eintrag speichern,
	 * ein neues Tupel wird angelegt.
	 */
	public void addEmptyRow(CsvKeyword pKeyword) throws FibuException {
		try {
			addRow(pKeyword);
		} catch (java.sql.SQLException e) {
			throw new FibuException("PdfKeyword.add() \n Message: " + e.getMessage());
		}
	}

	/**
	 * Ein Eintrag für eine Keyword speichern, falls nicht vorhanden ist, wird
	 * ein neues Tupel angelegt.
	 */
	public void add(CsvKeyword pKeyword) throws FibuException {
		try {
			if (findRow(pKeyword)) {
//				updateRow(pKeyword);
			} else {
				// wenn nicht gefunden, neues anlegen
				addRow(pKeyword);
			}
		} catch (java.sql.SQLException e) {
			throw new FibuException("PdfKeyword.add() \n Message: " + e.getMessage());
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
	public CsvKeyword readAt(int companyId, int position) throws FibuException {
		Trace.println(7, "PdfKeywordData.readAt()");
		CsvKeyword lKeyword = new CsvKeyword();
		lKeyword.setCompanyId(companyId);
		try {
			setupReadSet(companyId);
			if (mResultSet.absolute(position + 1)) {
				if (getAnzahlCols() == 4) {
					lKeyword.setId(0);
					lKeyword.setCompanyId(companyId);
					lKeyword.setSuchWort(mResultSet.getString(2));
					lKeyword.setKontoNr(mResultSet.getString(3));
					lKeyword.setSh(mResultSet.getString(4));				
				}
				else {
					lKeyword.setId(mResultSet.getInt(1));
					lKeyword.setCompanyId(companyId);
					lKeyword.setSuchWort(mResultSet.getString(3));
					lKeyword.setKontoNr(mResultSet.getString(4));
					lKeyword.setSh(mResultSet.getString(5));
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
	public void updateAt(int position, CsvKeyword pKeyword) throws FibuException {
		Trace.println(7, "PdfKeywordData.updateAt()");
		try {
			setupReadSet(pKeyword.getCompanyId());
			if (mResultSet.absolute(position + 1)) {
				int i = 1;
				if (getAnzahlCols() > 4) {
					i++;
				}		
//				mResultSet.updateInt(1, pKeyword.getId());
				mResultSet.updateInt(i, pKeyword.getCompanyId());
				mResultSet.updateString(++i, pKeyword.getSuchWort());
				mResultSet.updateString(++i, pKeyword.getKontoNr());
				mResultSet.updateString(++i, pKeyword.getSh());
				mResultSet.updateRow();
			} else {
				throw new FibuException("Keyword an Position: " + position + " nicht gefunden");
			}
		} catch (java.sql.SQLException e) {
			throw new FibuException("Keyword an Position: " + position + " Message: " + e.getMessage());
		}
	}	

	/**
	 * Das Tupel mit der ID löschen
	 */
	public void deleteRow(int id) throws SQLException {
		String sql ="DELETE FROM PdfKeyword WHERE id = ?";
		PreparedStatement ps = getConnection().prepareStatement(sql);
		ps.setInt(1, id);
		ps.executeUpdate();
		ps.close();
		mMaxRows--;
	}

	/**
	 * Die Anzahl Felder in der DB
	 * @return
	 * @throws SQLException
	 */
	private int getAnzahlCols() throws SQLException {
		if (mAnzahlCols == 0) {
			Statement stmt = getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM PdfKeyword");
			// wie viele columns
			ResultSetMetaData rsmd = rs.getMetaData();
			mAnzahlCols = rsmd.getColumnCount();
		}
		return mAnzahlCols;
	}

	
	/**
	 * Den Wert einer Zeile zurückgeben.
	 * @param pKeyword
	 * @return
	 * @throws SQLException
	 */
	private boolean findRow(CsvKeyword pKeyword) throws SQLException {
		setupReadSet(pKeyword.getCompanyId());
		mResultSet.beforeFirst();
		while (mResultSet.next()) {
			if (mResultSet.getInt(2) == pKeyword.getCompanyId()) {
				if	(mResultSet.getString(3).equalsIgnoreCase(pKeyword.getSuchWort())) {
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * Setzt das Statement (Connection zur DB) und den Scroll-Set, der für
	 * Insert oder update verwendet werden kann.
	 */
	private synchronized void setupReadSet(int companyId) throws SQLException {
		if (mReadStmt == null) {
			mReadStmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
		mResultSet = mReadStmt.executeQuery("SELECT * FROM PdfKeyword WHERE CompanyId = '" + companyId + "' ORDER BY SuchWort");
	}

	
	/**
	 * Eine neue Zeile (Row) in die Tabelle eintragen. Kopiert die Attribute 
	 * Objekt PdfKeyword in den ResultSet. Der SQL-String wird zusammengestellt.
	 */
	private int addRow(CsvKeyword pKeyword) throws SQLException {
		int id = 0;
		Statement stmt = getConnection().createStatement();
		StringBuffer lQuery = new StringBuffer(100);
		if (getAnzahlCols() == 4) {
			lQuery.append("INSERT INTO PdfKeyword VALUES ('");			
		}
		else {
		// die erste Column leer, da Autoincrement
			lQuery.append("INSERT INTO PdfKeyword VALUES (null,'");
		}
		lQuery.append(pKeyword.getCompanyId());
		lQuery.append("', '");
		lQuery.append(pKeyword.getSuchWort());
		lQuery.append("', '");
		lQuery.append(pKeyword.getKontoNr());
		lQuery.append("', '");
		lQuery.append(pKeyword.getSh());
		lQuery.append("')");
		stmt.executeUpdate(lQuery.toString());
		stmt.close();
		mResultSet = null;
		mMaxRows++;
		return id;
	}
	
	
	/**
	 * Max. Anzahl Zeilen in der Tabelle berechnen Werden im mMaxRows
	 * gespeichert.
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
			System.err.println("PdfKeyword.getMaxRows: " + e.getMessage());
			mMaxRows = 0;
		}
	}


		
	
	// ----- Iterator ---------------------------------------------

	/** Gibt einen Iterator zurück */
	public Iterator<CsvKeyword> getIterator(int companyId) {
		return new CsvKeywordIterator(companyId);
	}

	/** Iterator über alle Keywords */
	private class CsvKeywordIterator implements Iterator<CsvKeyword> {
		private Statement mReadStmt;
		private ResultSet mReadSet;

		/**
		 * Konstruktur, setzt den ReadSet.
		 * @param companyId
		 */
		CsvKeywordIterator(int companyId) {
			try {
				mReadStmt = getConnection()
						.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				mReadSet = mReadStmt.executeQuery("SELECT * FROM PdfKeyword WHERE CompanyId = '" + companyId + "' ORDER BY SuchWort");
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
		public CsvKeyword next() throws NoSuchElementException {
			try {
				CsvKeyword lPdfKeyword = new CsvKeyword();
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
	private void copyToKeyword(ResultSet pResult, CsvKeyword pKeyword) throws SQLException {	
		int i = 1;
		if (getAnzahlCols() > 4) {
			pKeyword.setId(pResult.getInt(i));
			i++;
		}
		pKeyword.setCompanyId(pResult.getInt(i));
		pKeyword.setSuchWort(pResult.getString(++i));
		pKeyword.setKontoNr(pResult.getString(++i));
		pKeyword.setSh(pResult.getString(++i));
	}

	
	
}
