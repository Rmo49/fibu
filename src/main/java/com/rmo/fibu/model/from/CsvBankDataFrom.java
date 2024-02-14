package com.rmo.fibu.model.from;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.rmo.fibu.model.CsvBank;
import com.rmo.fibu.util.Trace;

/**
 * Model der gespeicherten Bank von denen CSV gelesen werden.
 * Auslesen über Iterator, wird für kopieren verwendet.
 * @author Ruedi
 *
 */
public class CsvBankDataFrom extends DataBaseFrom {
	
	private final static String TABLE_NAME = "pdfcompany";

	private final int COLS_V2 = 5;		// Anzahl Cols in der Version 2
//	private final int COLS_V3 = 7;		// Anzahl Cols in der Version 3

	// die Anzahl Columns in der DB
	private int colsAnzahl = -1;


	/**
	 * Model constructor comment.
	 */
	public CsvBankDataFrom() throws Exception {
		super();
	}

	/**
	 * Implementieren, wenn verschiedene Versionen der Tabelle vorhanden sind.
	 * Diese Methode wird nach dem Start der Fibu aufgerufen.
	 */
	@Override
	public void checkTableVersion() {
		try {
			if (FibuDataBaseFrom.tableExist(TABLE_NAME)) {
				colsAnzahl = getNumberOfCols();
			}
		} catch (SQLException e) {
			Trace.println(3, "CsvBankData.checkTableVersion: " + e.getMessage());
		}
	}

	/**
	 * Die Anzahl Spalten der Tabelle.
	 * @return
	 */
	private int getNumberOfCols() throws SQLException {
		Statement stmt = getConnection().createStatement();
		String lQuery = "SELECT * from " + TABLE_NAME + ";";
		ResultSet lResult = stmt.executeQuery(lQuery);
		ResultSetMetaData rsmd = lResult.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		return columnsNumber;
	}


	/**
	 * Die Daten übertragen
	 * @param mReadSet
	 * @param lBank
	 * @throws SQLException
	 */
	private void copyToPdfBank(ResultSet mReadSet, CsvBank lBank) throws SQLException {
		lBank.setBankID(mReadSet.getInt(1));
		lBank.setBankName(mReadSet.getString(2));
		lBank.setKontoNrDefault(mReadSet.getString(3));
		String path = mReadSet.getString(4);
		path = path.replace('\\', '/');
		lBank.setDirPath(path);
		lBank.setDocType(mReadSet.getInt(5));
		if (colsAnzahl > COLS_V2) {
			lBank.setWordBefore(mReadSet.getString(6));
			lBank.setSpaltenArray(mReadSet.getString(7));
		}
	}
	

	// ----- Iterator ---------------------------------------------

	/** Gibt einen Iterator zurück */
	public Iterator<CsvBank> getIterator() {
		return new PdfIterator();
	}

	/** Iterator über alle Konti */
	private class PdfIterator implements Iterator<CsvBank> {
		private Statement mReadStmt;
		private ResultSet mReadSet;

		PdfIterator() {
			try {
				mReadStmt = getConnection()
						.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				mReadSet = mReadStmt.executeQuery("SELECT * FROM " +TABLE_NAME);
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
		public CsvBank next() throws NoSuchElementException {
			try {
				CsvBank lPdfCompay = new CsvBank();
				copyToPdfBank(mReadSet, lPdfCompay);
				return lPdfCompay;
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
