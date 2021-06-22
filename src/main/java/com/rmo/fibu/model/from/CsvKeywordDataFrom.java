 package com.rmo.fibu.model.from;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.rmo.fibu.model.CsvKeyword;

/**
 * Model der gespeicherten keywords für CSV.
 * Wird nur für lesen verwendet von alter Fibu auf Neue.
 * @author Ruedi
 *
 */
public class CsvKeywordDataFrom extends DataModelFrom {
	
	/**
	 * Model constructor comment.
	 */
	public CsvKeywordDataFrom() throws Exception {
		super();
	}

		
	// ----- Iterator ---------------------------------------------

	/** Gibt einen Iterator zurück */
	public Iterator<CsvKeyword> getIterator() {
		return new PdfKeywordIterator();
	}

	
	
	/** Iterator über alle Keywords */
	private class PdfKeywordIterator implements Iterator<CsvKeyword> {
		private Statement mReadStmt;
		private ResultSet mReadSet;
		private int mAnzahlCols = 0;

		/**
		 * Konstruktur, setzt den ReadSet.
		 * @param companyId
		 */
		PdfKeywordIterator() {
			try {
				mReadStmt = getConnection()
						.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				mReadSet = mReadStmt.executeQuery("SELECT * FROM PdfKeyword");
				mReadSet.beforeFirst();
				
				// wie viele columns
				ResultSetMetaData rsmd = mReadSet.getMetaData();
				mAnzahlCols = rsmd.getColumnCount();

			} catch (SQLException ex) {
			}
		}

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

		public CsvKeyword next() throws NoSuchElementException {
			try {
				CsvKeyword lPdfKeyword = new CsvKeyword();
				copyToKeyword(mReadSet, lPdfKeyword);
				return lPdfKeyword;
			} catch (SQLException ex) {
				throw new NoSuchElementException(ex.getMessage());
			}
		}

		/**
		 * Kopiert die Attribute vom ResultSet in das Objekt Keyword
		 */
		private void copyToKeyword(ResultSet pResult, CsvKeyword pKeyword) throws SQLException {
			int i = 0;
			if (mAnzahlCols == 4) {
				i = 1;
			}
			else {
				// die ID nicht kopieren, da AutoIncrement
				i = 2;
			}
			pKeyword.setCompanyId(pResult.getInt(i));
			pKeyword.setSuchWort(pResult.getString(++i));
			pKeyword.setKontoNr(pResult.getString(++i));
			pKeyword.setSh(pResult.getString(++i));
		}
	}
}
