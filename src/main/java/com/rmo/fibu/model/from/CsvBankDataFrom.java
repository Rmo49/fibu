package com.rmo.fibu.model.from;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.rmo.fibu.model.CsvBank;

/**
 * Model der gespeicherten Bank von denen CSV gelesen werden.
 * Auslesen über Iterator, wird für kopieren verwendet.
 * @author Ruedi
 *
 */
public class CsvBankDataFrom extends DataModelFrom {

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

	}



	private void copyToPdfBank(ResultSet mReadSet, CsvBank lBank) throws SQLException {
		lBank.setBankID(mReadSet.getInt(1));
		lBank.setBankName(mReadSet.getString(2));
		lBank.setKontoNrDefault(mReadSet.getString(3));
		String path = mReadSet.getString(4);
		path = path.replace('\\', '/');
		lBank.setDirPath(path);
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
