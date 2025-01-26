package com.rmo.fibu.model;

import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServicesListener;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.ExcelImport;
import com.rmo.fibu.util.Trace;

/**
 * Konto-Model der Fibu, Verbindung zu Kontorahmen in der DB. Schnittstelle zur
 * DB. Konti werden mit der Klasse Konto sichtbar gemacht.
 */
public class KontoData extends DataBase implements BeanContextServicesListener, Serializable {
	private static final long serialVersionUID = 6158252894613753607L;

	/**
	 * Enthält Connection zur DB. Wird in setupResultset gesetzt, bleibt während
	 * ganzer Sitzung erhalten.
	 */
	private Statement mReadStmt;

	/**
	 * Der Set mit allen Konto-Daten von dem gelesen wird. Ist ein scrollable
	 * Set der von allen Methoden verwendet wird. KontoNr: LongInteger <br>
	 * KontoText: String <br>
	 * StartSaldo: Double (Währung) <br>
	 * Saldo: Double (Währung) <br>
	 * IstSollKto: boolean
	 */
	private ResultSet mReadSet;

	/**
	 * Die Anzahl Rows in der Tabelle. Wird beim Start berechnet, dann immer
	 * updated, da Probleme bei vielen Zugriffen
	 */
	private int mMaxRows;

	/**
	 * KontoData constructor comment.
	 */
	public KontoData() throws Exception {
		super();
	}

	/**
	 * Implementieren, wenn verschiedene Versionen der Tabelle vorhanden sind.
	 * Diese Methode wird nach dem Start der Fibu aufgerufen.
	 */
	@Override
	public void checkTableVersion() {

	}


	/**
	 * Max. Anzahl Zeilen in der Tabelle.
	 */
	public int getRowCount() {
		if (mMaxRows == 0) {
			calculateMaxRows();
		}
		return mMaxRows;
	}

	/**
	 * Das Konto wird gespeichert. Falls die KontoNr nicht vorhanden ist, wird
	 * ein neues Konto angelegt
	 */
	public void add(Konto pKonto) throws KontoNotFoundException {
		try {
			if (findRow(pKonto.getKontoNr())) {
				updateRow(pKonto);
			} else {
				// wenn nicht gefunden, neues anlegen
				addRow(pKonto);
			}
		} catch (java.sql.SQLException e) {
			throw new KontoNotFoundException("KontoData.add() \n Message: " + e.getMessage());
		}
	}

	/**
	 * Das Konto mit der pKontoNr wird zurückgegeben. Wenn nicht gefunden wird
	 * KontoNotFoundException geworfen.
	 */
	public Konto read(int pKontoNr) throws KontoNotFoundException {
		try {
			if (findRow(pKontoNr)) {
				Konto lKonto = new Konto();
				lKonto.setKontoNr(pKontoNr);
				mReadSet.refreshRow();
				copyToKonto(lKonto);
				return lKonto;
			} else {
				throw new KontoNotFoundException("KontoNr: " + pKontoNr);
			}
		} catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new KontoNotFoundException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	/**
	 * Das Konto mit der pKontoNr wird zurückgegeben. Wenn nicht gefunden wird
	 * KontoNotFoundException geworfen.
	 */
	public Konto read(String pKontoNr) throws KontoNotFoundException {
		int lKontoNr = Integer.valueOf(pKontoNr).intValue();
		if (lKontoNr > 0) {
			return read(lKontoNr);
		} else {
			throw new KontoNotFoundException(pKontoNr);
		}
	}

	/**
	 * Das Konto an der Stelle position (0..x) zurückgeben.
	 *
	 * @return Konto an der position, null wenn nicht vorhanden
	 */
	public Konto readAt(int position) throws KontoNotFoundException {
		Trace.println(7, "KontoData.readAt()");
		try {
			setupReadSet();
			if (mReadSet.absolute(position + 1)) {
				Konto lKonto = new Konto();
				copyToKonto(mReadSet, lKonto);
				return lKonto;
			} else {
				throw new KontoNotFoundException("Konto an Position: " + position);
			}
		} catch (java.sql.SQLException e) {
			throw new KontoNotFoundException("Konto an Position: " + position + " Message: " + e.getMessage());
		}
	}

	/**
	 * Das Konto mit der Nummer pKontoNr wird gelöscht. Falls die KontoNr nicht
	 * vorhanden ist, wird die Exception KontoNotFoundException geworfen.
	 */
	public void delete(int pKontoNr) throws KontoNotFoundException {
		try {
			if (findRow(pKontoNr)) {
				mReadSet.deleteRow();
				// die Anzahl Zeilen korrigieren
				synchronized (this) {
					mMaxRows--;
				}
			} else {
				throw new KontoNotFoundException("Konto " + pKontoNr + " nicht gelöscht!");
			}
		} catch (SQLException e) {
			throw new KontoNotFoundException("deleteKonto:" + pKontoNr + "Message: " + e.getMessage());
		}
	}

	/**
	 * Das Konto an der Position position löschen. Falls kein Konto vorhanden
	 * ist, wird die Exception KontoNotFoundException geworfen.
	 */
	public void deleteAt(int position) throws KontoNotFoundException {
		try {
			setupReadSet();
			if (mReadSet.absolute(position + 1)) {
				// int x = mReadSet.getInt(1);
				// String s = mReadSet.getString(2);
				mReadSet.deleteRow();
				// die Anzahl Zeilen korrigieren
				synchronized (this) {
					mMaxRows--;
				}
				mReadSet.refreshRow();
			} else {
				throw new KontoNotFoundException("Position " + position + " nicht gelöscht!");
			}
		} catch (SQLException e) {
			String msg = null;
			if (e.getErrorCode() == 0) {
				msg = "Es sind noch Buchungen mit diesem Konto vorhanden";
			} else {
				msg = "Konto löschen: " + e.getMessage();
			}
			throw new KontoNotFoundException(msg);
		}
	}

	/**
	 * verbucht die Buchung im Kontorahmen
	 *
	 * @param double pBetrag der Betrag der Buchung
	 * @para int pKontoNr das Konto auf das verbucht werden soll
	 * @para boolean pSoll der Betrag steht in der Soll- / Haben-Spalte diese
	 *       Methode muss 2 mal aufgerufen werden
	 */
	public void verbuche(double pBetrag, int pKontoNr, boolean pSoll) throws KontoNotFoundException {
		// Konto lesen, check ob Soll- oder Haben-Konto
		Konto lKonto = read(pKontoNr);
		double lSaldo = lKonto.getSaldo();
		// verbuche: wenn es ein Soll-Konto ist
		if (lKonto.isSollKonto()) {
			// wenn in Buchung in der Soll-Spalte steht: dazuzählen
			if (pSoll) {
				lSaldo += pBetrag;
			} else {
				lSaldo -= pBetrag;
			}
		} else {
			if (pSoll) {
				lSaldo -= pBetrag;
			} else {
				lSaldo += pBetrag;
			}
		}
		lKonto.setSaldo(lSaldo);
		add(lKonto);
	}

	// ----- Iterator ---------------------------------------------

	/** Gibt einen Iterator zurück */
	public Iterator<Konto> getIterator() {
		return new KontoIterator();
	}

	/** Iterator über alle Konti */
	private class KontoIterator implements Iterator<Konto> {
		private Statement mReadStmt;
		private ResultSet mReadSet;

		KontoIterator() {
			try {
				mReadStmt = getConnection()
						.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				mReadSet = mReadStmt.executeQuery("SELECT * FROM Kontorahmen ORDER BY KontoNr");
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
		public Konto next() throws NoSuchElementException {
			try {
				Konto lKonto = new Konto();
				copyToKonto(mReadSet, lKonto);
				return lKonto;
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
	 * Kopiert die End-Saldi zu Start-Saldo, wenn konto nicht gefunden, wird
	 * neue angelegt.
	 */
	public void copyExcelToKonto(ExcelImport lExcel) throws KontoNotFoundException {
		Konto lKonto = null;
		int lKontoNr = 0;
		int maxRow = lExcel.getMaxRow();
		// iterate über alle Konti, wenn nicht vorhanden anlegen
		for (int rowNr = 1; rowNr <= maxRow; rowNr++) {
			try {
				lKontoNr = (int) lExcel.getDoubleAt(rowNr, 0);
				lKonto = read(lKontoNr);
			} catch (KontoNotFoundException pEx) {
				lKonto = new Konto();
				lKonto.setKontoNr(lKontoNr);
			}
			lKonto.setText(lExcel.getStringAt(rowNr, 1));
			String soll = lExcel.getStringAt(rowNr, 2);
			if (soll.equalsIgnoreCase("S")) {
				lKonto.setIstSollKonto(true);
			} else {
				lKonto.setIstSollKonto(false);
			}
			if (lKontoNr < Config.sERStart) {
			lKonto.setStartSaldo(lExcel.getDoubleAt(rowNr, 4));
			lKonto.setSaldo(lExcel.getDoubleAt(rowNr, 4));
			} else {
				lKonto.setStartSaldo(0);
				lKonto.setSaldo(0);
			}
			add(lKonto);
			Trace.println(7, "add: " + lKonto.toString());
		}
	}

	// ------ interne Methoden -----------------------------------------

	/**
	 * Sucht die Row mit der Kontonummer. Wenn true, steht mReadSet auf dieser
	 * Zeile.
	 */
	private boolean findRow(int pKontoNr) throws SQLException {
		setupReadSet();
		mReadSet.beforeFirst();
		while (mReadSet.next()) {
			if (mReadSet.getInt(1) == pKontoNr) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Kopiert die Attribute vom ResultSet in das Objekt Konto
	 */
	private void copyToKonto(Konto pKonto) throws SQLException {
		pKonto.setText(mReadSet.getString(2));
		pKonto.setStartSaldo(mReadSet.getDouble(3));
		pKonto.setSaldo(mReadSet.getDouble(4));
		pKonto.setIstSollKonto(mReadSet.getBoolean(5));
		// rmo: wenn als erstes Statement, dann Exception
		pKonto.setKontoNr(mReadSet.getString(1));
	}

	/**
	 * Kopiert die Attribute vom ResultSet in das Objekt Konto
	 */
	private void copyToKonto(ResultSet pResult, Konto pKonto) throws SQLException {
		pKonto.setText(pResult.getString(2));
		pKonto.setStartSaldo(pResult.getDouble(3));
		pKonto.setSaldo(pResult.getDouble(4));
		pKonto.setIstSollKonto(pResult.getBoolean(5));
		// rmo: wenn als erstes Statement, dann Exception
		pKonto.setKontoNr(pResult.getString(1));
	}

	/**
	 * Eine neue Zeile (Row) in die Tabelle eintragen. Kopiert die Attribute vom
	 * ResultSet in das Objekt Konto. Der SQL-String wird zusammengestellt.
	 */
	private void addRow(Konto pKonto) throws SQLException {
		Statement stmt = getConnection().createStatement();
		StringBuffer lQuery = new StringBuffer("INSERT INTO Kontorahmen VALUES (");
		lQuery.append(pKonto.getKontoNr());
		lQuery.append(", '");
		lQuery.append(pKonto.getText());
		lQuery.append("',");
		lQuery.append(pKonto.getStartSaldo());
		lQuery.append(", ");
		lQuery.append(pKonto.getSaldo());
		lQuery.append(", ");
		lQuery.append(pKonto.isSollKonto());
		lQuery.append(")");
		// String lQ = lQuery.toString();
		stmt.executeUpdate(lQuery.toString());
		stmt.close();
		mReadSet = null;
		// die Anzahl Zeilen erhöhen
		synchronized (this) {
			mMaxRows++;
		}
	}

	/**
	 * Aendert die Attribute der gewählten Zeile.
	 */
	private void updateRow(Konto pKonto) throws SQLException {
		PreparedStatement updateKonto = getConnection().prepareStatement(
				"UPDATE Kontorahmen SET KontoText = ? , StartSaldo = ? , Saldo = ? , IstSollKto = ? WHERE KontoNr = ?");
		updateKonto.setString(1, pKonto.getText());
		updateKonto.setDouble(2, pKonto.getStartSaldo());
		updateKonto.setDouble(3, pKonto.getSaldo());
		updateKonto.setBoolean(4, pKonto.isSollKonto());
		updateKonto.setInt(5, pKonto.getKontoNr());
		updateKonto.executeUpdate();
		// Reader neu aufsetzen
	}

	/**
	 * Max. Anzahl Zeilen in der Tabelle berechnen Werden im mMaxRows
	 * gespeichert.
	 */
	private synchronized void calculateMaxRows() {
		try {
			Statement stmt = getConnection().createStatement();
			String lQuery = "SELECT Count(*) FROM Kontorahmen;";
			ResultSet lResult = stmt.executeQuery(lQuery);
			if (lResult.next()) {
				mMaxRows = lResult.getInt(1);
				lResult.close();
			}
		} catch (SQLException e) {
			System.err.println("KontoData.getMaxRows: " + e.getMessage());
			mMaxRows = 0;
		}
	}

	/**
	 * Setzt das Statement (Connection zur DB) und den Scroll-Set, der für
	 * Insert oder update verwendet werden kann.
	 */
	private synchronized void setupReadSet() throws SQLException {
		if (mReadStmt == null) {
			mReadStmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
		if (mReadSet == null) {
			mReadSet = mReadStmt.executeQuery("SELECT * FROM Kontorahmen ORDER BY KontoNr");
		}
	}

	// ------- Bean Support ---------------------
	@Override
	public void serviceAvailable(BeanContextServiceAvailableEvent bcsae) {
	}

	@Override
	public void serviceRevoked(BeanContextServiceRevokedEvent bcsre) {
	}

}
