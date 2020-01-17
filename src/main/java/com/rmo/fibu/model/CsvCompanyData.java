package com.rmo.fibu.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.rmo.fibu.exception.FibuException;

/**
 * Model der gespeicherten company von denen CSV gelesen werden.
 * Auslesen als Iterator
 * @author Ruedi
 *
 */
public class CsvCompanyData extends DataModel {

	/**
	 * Enthält Connection zur DB. Wird in setupResultset gesetzt, bleibt während
	 * ganzer Sitzung erhalten.
	 */
	private Statement mReadStmt;

	/**
	 * Der Set mit allen Keyword-Daten von dem gelesen wird. Ist ein scrollable
	 * Set der von allen Methoden verwendet wird.
	 */
	private ResultSet mReadSet;
	
	/**
	 * Model constructor comment.
	 */
	public CsvCompanyData() throws Exception {
		super();
	}

	/**
	 * Ein Eintrag für eine Company speichern, falls nicht vorhanden ist, wird
	 * ein neues Tupel angelegt.
	 */
	public void addData(CsvCompany pCompany) throws FibuException {
		try {
			if (findRow(pCompany)) {
				updateRow(pCompany);
			} else {
				// wenn nicht gefunden, neues anlegen
				addRow(pCompany);
			}
		} catch (java.sql.SQLException e) {
			throw new FibuException("PdfKeyword.add() \n Message: " + e.getMessage());
		}
	}

	private boolean findRow(CsvCompany pCompany) throws SQLException {
		setupReadSet(pCompany.getCompanyName());
		mReadSet.beforeFirst();
		if (mReadSet.next()) {
			return true;
		}
		return false;
	}

	/**
	 * Setzt das Statement (Connection zur DB) und den Scroll-Set, der für
	 * Insert oder update verwendet werden kann.
	 */
	private synchronized void setupReadSet(String pCompany) throws SQLException {
		if (mReadStmt == null) {
			mReadStmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
		mReadSet = mReadStmt.executeQuery("SELECT * FROM PdfCompany WHERE CompanyName = '" + pCompany + "'");
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
		lQuery.append("')");
		// String lQ = lQuery.toString();
		stmt.executeUpdate(lQuery.toString());
		stmt.close();
		mReadSet = null;
	}

	/**
	 * Aendert die Attribute der gewählten Zeile.
	 */
	private void updateRow(CsvCompany pCompany) throws SQLException {
		PreparedStatement updateCompany = getConnection().prepareStatement(
				"UPDATE PdfCompany SET CompanyName = ? , KontoNrDefault = ?, DirPath = ? WHERE CompanyName = ?");
		updateCompany.setString(1, pCompany.getCompanyName());
		updateCompany.setString(2, pCompany.getKontoNrDefault());
		updateCompany.setString(3, pCompany.getDirPath());
		updateCompany.setString(4, pCompany.getCompanyName());
		updateCompany.executeUpdate();
		// Reader neu aufsetzen
//		synchronized (this) {
//			mReadSet = null;
//		}
	}
	
	/**
	 * Das PdfCompany mit dem suchWort wird zurückgegeben. Wenn nicht gefunden wird
	 * FibuException geworfen.
	 */
	public CsvCompany readData(String companyName) throws FibuException {
		CsvCompany lCompany = new CsvCompany();
		lCompany.setCompanyName(companyName);
		try {
			if (findRow(lCompany)) {
				mReadSet.refreshRow();
				lCompany.setCompanyID(mReadSet.getInt(1));
				lCompany.setCompanyName(mReadSet.getString(2));
				lCompany.setKontoNrDefault(mReadSet.getString(3));
				lCompany.setDirPath(mReadSet.getString(4));
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
	 * Ein Tupel löschen
	 */
	public void deleteRow(CsvCompany pCompany) throws SQLException {
		if (findRow(pCompany)) {
			mReadSet.deleteRow();
		}
	}

	
	
}
