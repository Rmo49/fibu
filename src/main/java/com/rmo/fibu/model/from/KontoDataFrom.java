package com.rmo.fibu.model.from;

import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServicesListener;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.rmo.fibu.model.Konto;


/**
 * Konto-Model der Fibu, Verbindung zu Kontorahmen in der DB. Schnittstelle zur DB.
 * Konti werden mit der Klasse Konto sichtbar gemacht.
 * Wird verwendet um Daten zu kopieren.
 */
public class KontoDataFrom extends DataBaseFrom implements BeanContextServicesListener, Serializable {
	private static final long serialVersionUID = 6158252894613753607L;


	/**
	 * KontoData constructor comment.
	 */
	public KontoDataFrom() throws Exception {
		super();
	}

	/**
	 * Implementieren, wenn verschiedene Versionen der Tabelle vorhanden sind.
	 * Diese Methode wird nach dem Start der Fibu aufgerufen.
	 */
	@Override
	public void checkTableVersion() {

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
	}

	// ------- Bean Support ---------------------
	@Override
	public void serviceAvailable(BeanContextServiceAvailableEvent bcsae) {
	}

	@Override
	public void serviceRevoked(BeanContextServiceRevokedEvent bcsre) {
	}

}
