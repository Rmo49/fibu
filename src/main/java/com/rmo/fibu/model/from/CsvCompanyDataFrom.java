package com.rmo.fibu.model.from;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.rmo.fibu.model.CsvCompany;

/**
 * Model der gespeicherten Company von denen CSV gelesen werden.
 * Auslesen über Iterator, wird für kopieren verwendet.
 * @author Ruedi
 *
 */
public class CsvCompanyDataFrom extends DataModelFrom {

	/**
	 * Model constructor comment.
	 */
	public CsvCompanyDataFrom() throws Exception {
		super();
	}

	/**
	 * Implementieren, wenn verschiedene Versionen der Tabelle vorhanden sind.
	 * Diese Methode wird nach dem Start der Fibu aufgerufen.
	 */
	@Override
	public void checkTableVersion() {

	}



	private void copyToPdfCompany(ResultSet mReadSet, CsvCompany lCompany) throws SQLException {
		lCompany.setCompanyID(mReadSet.getInt(1));
		lCompany.setCompanyName(mReadSet.getString(2));
		lCompany.setKontoNrDefault(mReadSet.getString(3));
		String path = mReadSet.getString(4);
		path = path.replace('\\', '/');
		lCompany.setDirPath(path);
	}

	// ----- Iterator ---------------------------------------------

	/** Gibt einen Iterator zurück */
	public Iterator<CsvCompany> getIterator() {
		return new PdfIterator();
	}

	/** Iterator über alle Konti */
	private class PdfIterator implements Iterator<CsvCompany> {
		private Statement mReadStmt;
		private ResultSet mReadSet;

		PdfIterator() {
			try {
				mReadStmt = getConnection()
						.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				mReadSet = mReadStmt.executeQuery("SELECT * FROM pdfcompany");
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
		public CsvCompany next() throws NoSuchElementException {
			try {
				CsvCompany lPdfCompay = new CsvCompany();
				copyToPdfCompany(mReadSet, lPdfCompay);
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
