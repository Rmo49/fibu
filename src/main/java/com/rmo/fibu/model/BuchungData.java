package com.rmo.fibu.model;

import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.rmo.fibu.exception.BuchungNotFoundException;
import com.rmo.fibu.exception.BuchungValueException;
import com.rmo.fibu.exception.BuchungVorhandenException;
import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.util.Trace;

/** Das Model von Buchung, verwaltet alle Buchungseinträge.
 *  Verbindung zur DB. Stellt (CRUD)-Methoden zur Verfügung.
 *  Enthält eine temporäre Liste von neuen Buchungen, die nach saveNew in die
 *  Datenbank geschrieben werden (evt. diese zuerst auf File schreiben).
 *  Hat auch einen (internen) Iterator für lesen etc..
*/
public class BuchungData extends DataBase implements BeanContextMembershipListener, Serializable {
	private static final long serialVersionUID = -3172925095948099368L;

	/** Die Id der nächsten Buchung */
	private long mNextBuchungId = -1;

	/** Die Verbindung zu Konto, Verbuchung nachführen */
	private KontoData mKontoData = null;

	/**  Die Anzahl Rows in der Tabelle.
	 *   Wird beim Start berechnet, dann immer updated,
	 *   da Probleme bei vielen Zugriffen */
	private int         mCountRows = -1;

	/** Die Nummer der zuletzt gelesene Row  */
	private int         mPositionRead = -2;

	/** Die (zuletzt) gelesen Buchung */
	private Buchung     mBuchung;

	/** Enthält Connection zur DB.
	 *  Wird in setupResultset gesetzt, bleibt während ganzer Sitzung erhalten. */
	private Statement   mReadStmt;

	/** Der Set mit allen Buchungen.
	 *  Ist ein scrollable Set der von allen Methoden verwendet wird.
	 *  ID: long <br>
	 *  Datum: Date <br>
	 *  Beleg: String <br>
	 *  Text: String <br>
	 *  Soll, Haben: integer <br>
	 *  Betrag: double (Währung) */
	private ResultSet   mDataSet;

	/** Die Liste der neuen Buchungen */
	private Vector<Buchung>  mNewBuchung;

	/** Die ID bis zu dieser Buchungen gesichert wurden
	 * wird von der BuchungView gesetzt */
	private long		idSaved = 0;

	/**
	 * BuchungData constructor. Throws Exception, if Connection not found.
	 */
	public BuchungData() throws Exception {
		super();
		init();
	}

	/**
	 * Implementieren, wenn verschiedene Versionen der Tabelle vorhanden sind.
	 * Diese Methode wird nach dem Start der Fibu aufgerufen.
	 */
	@Override
	public void checkTableVersion() {

	}

	/** Max. Anzahl Zeilen von Buchungen (in der Tabelle und Temporäre).
	 */
	public int getRowCount() {
		Trace.print(7,"BuchungData.getRowCont()");
		Trace.println(7, " Tabel:" +getRowCountTable() +" new:" + getRowCountNew() );
		return getRowCountTable() + mNewBuchung.size();
	}

	/** Max. Anzahl Zeilen in der Tabelle
	 */
	public int getRowCountTable() {
		if (mCountRows < 0) {
			calculateMaxRows();
		}
		return mCountRows;
	}

	/** Max. Anzahl Zeilen neue Buchungen (Temp.)
	 */
	public int getRowCountNew() {
		return mNewBuchung.size();
	}

	/** Die Buchung an der Stelle position (0..x) zurückgeben.
	 * @return Buchung an der position, aus DB und im Array New
	 * @exception BuchungNotFoundException
	 */
	public Buchung readAt(int position) throws BuchungNotFoundException, BuchungValueException {
		Trace.println(7, "BuchungData.readAt(" + position + ")");
		if (position < getRowCountTable()) {
			// wenn bereits gelesen
			if (position == mPositionRead) {
				return mBuchung;
			}
			try {
			   // die gewünschte Zeile wird gelesen
				setupDataSet();
				// SQL startet von 1..n  Tabel aber von 0.., darum +1
				if (mDataSet.absolute(position +1)) {
					mPositionRead = position;
					copyToBuchung();
					return mBuchung;
				}
				else {
					return null;
				}
			}
			catch (SQLException e) {
				// @todo rmo: SQL-Exception noch untersuchen?
				throw new BuchungNotFoundException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
			}
		}
		else {
			// aus New-Array lesen
			int positionInNew = position - getRowCountTable();
			if (positionInNew < mNewBuchung.size()) {
				return mNewBuchung.elementAt(positionInNew);
			}
			throw new BuchungNotFoundException("Buchung an Position: " + position + " nicht vorhanden");
		}
	}

	/** nächstes Buchung vom Iterator des Member-ResultSet lesen.
     * Iterationen über alle Buchungen sollten über getIterator() durchgeführt werden.
	 * @return Buchung das nächste Buchung.
	 * @exception BuchungNotFoundException wenn kein Buchung mehr vorhaden.
	 */
	public Buchung readNextRow() throws BuchungNotFoundException, BuchungValueException {
		Trace.println(5, "BuchungData.readNextRow()");
		try {
			if (mDataSet.next()) {
				mPositionRead++;
				copyToBuchung();
				return mBuchung;
			}
			else {
				throw new BuchungNotFoundException("Kein Buchung vorhanden");
			}
		}
		catch (java.sql.SQLException e) {
			// @todo rmo: SQL-Exception noch untersuchen?
			throw new BuchungNotFoundException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	/** Eine neue oder geänderte Buchung im temporären Speicher dazufügen.
	 *  Diese müssen mit saveNewBuchung() definitiv gespeichert werden.
	 */
	public void add(Buchung pBuchung) {
		Trace.println(4, "BuchungData.add(" +pBuchung.toString() +')');
		// neuen Recordset anlegen, falls vorher nicht gelesen
		// wenn ID < 0, dann nicht gelesen, neue dazufügen
		if (pBuchung.getID() < 0) {
			pBuchung.setID(getNextBuchungId());
			mNewBuchung.addElement(pBuchung);
			return;
		}
		// Buchung wurde geändert, alte Buchung zurückschreiben
		int i;
		for (i=0; i < mNewBuchung.size(); i++) {
			if (mNewBuchung.elementAt(i).getID() == pBuchung.getID()) {
				break;
			}
		}
		if (i < mNewBuchung.size()) {
			mNewBuchung.setElementAt(pBuchung, i);
		}
		else {
			mNewBuchung.addElement(pBuchung);
		}
		/*
		try {
			getKontoData().adjustSaldo(pBuchung);
			setInKontoData(pBuchung);
		}
		catch (KontoNotFoundException e) {
			//mSet.cancelUpdate();
			throw e;
		}
		*/
	}

	/** Den temporären Speicher (neue Buchungen) in die DB schreiben.
	 *  Den temp. Speicher leeren, falls die Buchungen keinen Fehler haben */
	public void saveNew() throws FibuException {
		Trace.println(3, "BuchungData.saveNew()");

		saveNewBookings();
		// prüfen ob keine Fehler, wenn nicht entfernen aus der Liste
		for (int i = 0; i < mNewBuchung.size(); i++) {
			if ( !mNewBuchung.elementAt(i).isFehler()) {
				mNewBuchung.removeElementAt(i);
			}
		}
		// die Anzahl zeilen in der Tabelle später wieder neu berechnen
		mCountRows = -1;
	}

	/** Die neuen Buchungen vom temporären Speicher in die DB schreiben */
	private void saveNewBookings() throws FibuException {
		Trace.println(4, "BuchungData.saveNewBookings()");
		for (int i = 0; i < mNewBuchung.size(); i++) {
			save(mNewBuchung.elementAt(i));
		}
	}

	/** Alle neuen (noch nicht gespeicherten) Buchungen löschen */
	public void deleteNewBookings() {
		mNewBuchung.removeAllElements();
	}

	/** Die Buchung mit der ID pID löschen */
	public void delete(long pID) throws FibuException {
		Trace.println(2, "BuchungData.delete(ID: " + pID +')');
		try {
			setupDataSet();
			mDataSet.beforeFirst();
			while (mDataSet.next()) {
				if (mDataSet.getLong(1) == pID) {
					mDataSet.deleteRow();
					mCountRows--;
					return;
				}
			}
		}
		catch (SQLException e) {
			// mach nix, versuche mit newBuchung
		}
		//
		for (int i = 0; i < mNewBuchung.size(); i++) {
			if ( mNewBuchung.elementAt(i).getID() == pID) {
				mNewBuchung.removeElementAt(i);
				return;
			}
		}
		throw new BuchungNotFoundException("delete ID: " + pID);
	}

	/** Eine neue oder geänderte Buchung der DB (Recordset) dazufügen.
	 Wenn ID der Buchung 0 ist, dann ist das eine neue Buchung,
	 sonst wird die Buchung mit der ID geändert.
	 */
	public void save(Buchung pBuchung) throws FibuException {
		Trace.println(2, "BuchungData.save(" + pBuchung.toString() +')');
		if (pBuchung.isFehler()) {
			return;
		}
		try {
			// neuen Recordset anlegen, falls vorher nicht gelesen
			// wenn ID < 0, dann nicht gelesen
			if (pBuchung.getID() < 0) {
				pBuchung.setID(getNextBuchungId());
				addRow(pBuchung);
			} else {
				try {
					// Buchung wurde geändert, alte Buchung suchen
					readRow(pBuchung.getID());
					// update Buchung wenn read keine Exception geworfen hat
					updateRow(pBuchung);
				}
				catch (BuchungNotFoundException e) {
					addRow(pBuchung);
					mDataSet = null;
					return;
				}
			}
		}
		catch (SQLException e) {
			pBuchung.isFehler();
			if (e.getErrorCode() == 1452) {
				throw new BuchungNotFoundException("Konto nicht vorhanden " + pBuchung.getSoll() + " oder " + pBuchung.getHaben());
			}
			throw new BuchungNotFoundException(e.getMessage());
		}
	}

	/** Eine neue oder geänderte Buchung dem Recordset dazufügen.
	 * Wenn notSame, wird zuerst überprüft ob diese Buchung schon vorhanden ist,
	 * (Datum, Beleg, Soll, Haben, und Betrag müssen übereinstimmen).
	 * Wenn bereits vorhanden wird BuchungVorhandenException geworfen.
	 */
	public void save(Buchung pBuchung, boolean notSame) throws FibuException {
		// zuerst Buchung suchen
		if (notSame) {
			if (exist(pBuchung)) {
				throw new BuchungVorhandenException(pBuchung.toString());
			}
		}
		// verbuchen
		save(pBuchung);
	}


	/** Check, ob eine Buchung vorhanden ist,
	 * @return true wenn gefunden und alle Felder gleichen Inhalt haben.
	*/
	public boolean exist(Buchung pBuchung) {
		// zuerst Buchung suchen
		try {
			setupDataSet();
			mDataSet.beforeFirst();
			while (mDataSet.next()) {
				Buchung lBuchung = copyToBuchung(mDataSet);
				if (lBuchung.equals(pBuchung)) {
					return true;
				}
			}
			return false;
		} catch (SQLException e) {
			return false;
		} catch (BuchungValueException ex) {
			return false;
		}
	}


	/** Check, ob eine potentielle Buchung schon vorhanden ist.
	 * @return true wenn gefunden und alle Felder gleichen Inhalt haben.
	*/
	public Buchung isInDb(Buchung pBuchung) {
		// zuerst Buchung suchen
		try {
			setupDataSet();
			mDataSet.beforeFirst();
			while (mDataSet.next()) {
				Buchung lBuchung = copyToBuchung(mDataSet);
				if (lBuchung.sameAs(pBuchung)) {
					return lBuchung;
				}
			}
			return null;
		} catch (SQLException e) {
			return null;
		} catch (BuchungValueException ex) {
			return null;
		}
	}


	/** Gibt das Model von Konto zurück.
	 *  wenn nicht gefunden: KontoNotFoundException.
	 */
	public KontoData getKontoData() throws KontoNotFoundException {
		if (mKontoData == null) {
			Iterator<?> lBeans = mBeanContext.iterator();

			while (lBeans.hasNext()) {
				Object lObject = lBeans.next();
				if (lObject instanceof KontoData) {
					mKontoData = (KontoData) lObject;
					break;
				}
			}

			if (mKontoData == null) {
				throw new KontoNotFoundException("Kein Konto-Model vorhanden");
			}
		}
		return mKontoData;
	}

	/** Max. Anzahl Zeilen in der Tabelle berechnen
	 *  Werden im mCountRows gespeichert.
	 */
	private synchronized void calculateMaxRows() {
		Trace.println(7,"BuchungData.calculateMaxRows()");
		try {
			Statement stmt = getConnection().createStatement();
			String lQuery = "SELECT Count(*) FROM Buchungen;";
			ResultSet lResult = stmt.executeQuery(lQuery);
			if (lResult.next()) {
				mCountRows = lResult.getInt(1);
				lResult.close();
			}
		} catch (SQLException e) {
			Trace.println(1, "BuchungData.getMaxRows: " + e.getMessage());
			mCountRows = -1;
		}
	}

	/**
	 * Reload data into DataSet, muss nach einem update ausgeführt werden.
	 */
	public void reloadData() {
		try {
			if (mDataSet != null) {
				mDataSet.close();
			}
			mDataSet = null;
			setupDataSet();
		}
		catch (SQLException e) {
			Trace.println(1, "BuchungData.reloadData: " + e.getMessage());
		}

	}

	/** Setzt das Statement (Connection zur DB)
	 *  und den Scroll-Set, der für Insert oder update verwendet werden kann.
	 */
	private void setupDataSet() throws SQLException {
		if (mReadStmt == null) {
			mReadStmt = DbConnection.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
		if (mDataSet == null) {
			mDataSet = mReadStmt.executeQuery("SELECT * FROM Buchungen ORDER BY Datum,Beleg,ID");
		}
	}

	/** Initialisiert das Objekt.
	 */
	private void init() {
		Trace.println(1, "BuchungData.init()");
		mNewBuchung = new Vector<>();
	}

	/** Listeners dazufügen
	 */
	@Override
	protected void initContextListener() {
		mBeanContext.addBeanContextMembershipListener(this);
		// rmo: probleme inVA mit inner classes
		mBeanContext.addBeanContextMembershipListener(new BeanContextMembershipListener() {
			@Override
			public void childrenAdded(BeanContextMembershipEvent bcme) {
				System.out.println("Another bean has been added to the context.");
			}
			@Override
			public void childrenRemoved(BeanContextMembershipEvent bcme) {
				System.out.println("Another bean has been removed from the context.");
			}
		});
	}

	/** Die Buchung mit der BelegNr pBeleg wird zurückgegeben.
	 * Wenn nicht gefunden wird BuchungNotFoundException geworfen
	 */
	public Buchung read(String pBeleg) throws BuchungNotFoundException, BuchungValueException {
		try {
			Statement stmt = getConnection().createStatement();
			String lQuery = "SELECT * FROM Buchungen WHERE Beleg LIKE '" + pBeleg + "'";
			ResultSet lResult = stmt.executeQuery(lQuery);
			if (lResult.next()) {
				Buchung lBuchung = copyToBuchung(lResult);
				return lBuchung;
			}
			else {
				throw new BuchungNotFoundException("Beleg: " + pBeleg);
			}
		}
		catch (java.sql.SQLException e) {
			// rmo: SQL-Exception noch untersuchen?
			throw new BuchungNotFoundException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	/** Die Buchung mit der ID wird zurückgegeben.
	 * Wenn nicht gefunden wird BuchungNotFoundException geworfen
	 */
	public Buchung read(long pID) throws BuchungNotFoundException, BuchungValueException {
		Trace.println(3, "BuchungData.read(ID: " + pID +')');
		ResultSet lResult = readRow(pID);
		try {
			Buchung lBuchung = copyToBuchung(lResult);
			return lBuchung;
		}
		catch (SQLException e) {
			throw new BuchungNotFoundException(e.getMessage());
		}
	}


	/** Gibt die aktuelle BuchungId zurück
	 * Berechnet diese falls am Anfang.
	 */
	public long getLastBuchungId() {
		if (mNextBuchungId < 0) {
		// Die letzt Zeile lesen (geht scheinbar nicht mit JDBC 2)
		// Nach öffnen immmer zuerst bereichnen
			try {
				//Statement stmt = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				Statement stmt = getConnection().createStatement();
				ResultSet lResult = stmt.executeQuery("SELECT ID FROM Buchungen");
				//lResult.last();
				while (lResult.next()) {
					mNextBuchungId = Math.max(lResult.getLong(1), mNextBuchungId);
				}
			} catch (SQLException e) {
				Trace.println(1, "getNextBuchungId(): " + e.getMessage());
			}
		}
		return mNextBuchungId;
	}

	/** Gibt die nächste Belegsnummer zurück
	 */
	public String getNextBelegNr() {
		int max = 0;
		try {
			Statement stmt = getConnection().createStatement();
			ResultSet lResult = stmt.executeQuery("SELECT Beleg FROM Buchungen");
			while (lResult.next()) {
				try {
					int i = lResult.getInt(1);
					if (i > max) {
						max = i;
					}
				}
				catch (NumberFormatException e) {
					// do nothing
				}
			}
		} catch (SQLException e) {
			Trace.println(3, "getNextBelegNr(): " + e.getMessage());
		}
		return String.valueOf(max++);
	}

	//----- Iterator ---------------------------------------------
	/** Gibt einen Iterator zurück */
	public Iterator<Buchung> getIterator() {
		return new BuchungIterator();
	}

	/** Iterator über alle gespeicherten Buchungen */
	private class BuchungIterator implements Iterator<Buchung>
	{
		private Statement   mReadStmt;
		private ResultSet   mReadSet;
		/** Konstruktor, setzt den Readset auf */
		BuchungIterator() {
			try {
				mReadStmt = DbConnection.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				mReadSet = mReadStmt.executeQuery("SELECT * FROM Buchungen ORDER BY Datum,Beleg,ID");
				mReadSet.beforeFirst();
			}
			catch (SQLException ex) {}
		}

		@Override
		public boolean hasNext() {
			try {
				if (mReadSet.next()) {
					return true;
				}
				else {
					mReadSet.close();
					mReadStmt.close();
					return false;
				}
			}
			catch (SQLException ex) {
				return false;
			}
		}

		@Override
		public Buchung next() throws NoSuchElementException {
			try {
				Buchung lBuchung = copyToBuchung(mReadSet);
				return lBuchung;
			}
			catch (BuchungValueException ex) {
				throw new NoSuchElementException(ex.getMessage());
			}
			catch (SQLException ex) {
				throw new NoSuchElementException(ex.getMessage());
			}
		}

		@Override
		public void remove() {
			try {
				Buchung lBuchung = copyToBuchung(mReadSet);
				delete(lBuchung.getID());
			}
			catch (Exception ex) {
				// do nothing
			}

		}
	}


	//----- interne Methoden -----------------------------------------
	/** Gibt die nächste Buchungs-ID zurück.
	 */
	private long getNextBuchungId() {
		getLastBuchungId();
		synchronized (this) {
			if (mNextBuchungId <= 0) {
				mNextBuchungId = 0;
			}
			// die nächste ist +1
			return ++mNextBuchungId;
		}
	}

	/** Die Row mit der ID wird gesucht.
	 * Wenn gefunden wird das ensprechende ResultSet zurückgegeben
	 * Wenn nicht gefunden wird BuchungNotFoundException geworfen
	 */
	private ResultSet readRow(long pID) throws BuchungNotFoundException {
		try {
			Statement stmt = getConnection().createStatement();
			String lQuery = "SELECT * FROM Buchungen WHERE ID = " + pID;
			ResultSet lResult = stmt.executeQuery(lQuery);
			if (lResult.next()) {
				return lResult;
			}
			else {
				throw new BuchungNotFoundException("ID: " + pID);
			}
		}
		catch (java.sql.SQLException e) {
			// rmo: SQL-Exception noch untersuchen?
			throw new BuchungNotFoundException("SQL: " + e.getSQLState() + " Message: " + e.getMessage());
		}
	}

	/** fügt eine Buchung in die DB ein. Die ID wird um einen Zähler erhöht.
	 */
	private void addRow(Buchung pBuchung) throws SQLException {
		Statement stmt = getConnection().createStatement();
		StringBuffer lQuery = new StringBuffer("INSERT INTO Buchungen VALUES (");
		lQuery.append(pBuchung.getID());
		lQuery.append(", '");
		java.sql.Date lDate = pBuchung.getDatum().asSqlDate();
		lQuery.append(lDate.toString());
		lQuery.append("', '");
		lQuery.append(pBuchung.getBeleg());
		lQuery.append("', '");
		lQuery.append(pBuchung.getBuchungTextSql());
		lQuery.append("', ");
		lQuery.append(pBuchung.getSoll());
		lQuery.append(", ");
		lQuery.append(pBuchung.getHaben());
		lQuery.append(", ");
		lQuery.append(pBuchung.getBetrag());
		lQuery.append(")");
		stmt.executeUpdate(lQuery.toString());
	}

	/** Kopiert die Attribute vom ResultSet in das Member-Objekt mBuchung
	 */
	private void copyToBuchung () throws SQLException, BuchungValueException {
		Trace.println(7, "BuchungData.copyToBuchung()");
		if (mBuchung == null) {
			mBuchung = new Buchung();
		}
		// KontoNr zurücksetzen, damit nicht alte nr vorhanden ist, wenn neu gesetzt.
		mBuchung.setSoll(0);
		mBuchung.setHaben(0);
		mBuchung.setID(mDataSet.getLong(1));
		mBuchung.setDatum(mDataSet.getDate(2));
		mBuchung.setBeleg(mDataSet.getString(3));
		mBuchung.setBuchungText(mDataSet.getString(4));
		mBuchung.setSoll(mDataSet.getInt(5));
		mBuchung.setHaben(mDataSet.getInt(6));
		mBuchung.setBetrag(mDataSet.getDouble(7));
	}

	/** Kopiert die Attribute vom ResultSet in das Objekt Buchung
	 */
	private Buchung copyToBuchung(ResultSet pResult) throws SQLException, BuchungValueException {
		Trace.println(7, "BuchungData.copyToBuchung(ResultSet, Buchung)");
		Buchung buchung = new Buchung();
		buchung.setID(pResult.getLong(1));
		buchung.setDatum(pResult.getDate(2));
		buchung.setBeleg(pResult.getString(3));
		buchung.setBuchungText(pResult.getString(4));
		buchung.setSoll(pResult.getInt(5));
		buchung.setHaben(pResult.getInt(6));
		buchung.setBetrag(pResult.getDouble(7));
		return buchung;
	}

	/** Verändert eine zuvor gelesene Buchung.
	 */
	private void updateRow(Buchung pBuchung) throws SQLException {
		String lQuery = "UPDATE Buchungen SET Datum = ? , Beleg = ? , BuchungText = ?, Soll = ?, Haben = ?, Betrag = ? WHERE ID = ";
		lQuery += pBuchung.getID();
		PreparedStatement updateBuchung = getConnection().prepareStatement(lQuery);
		//	"UPDATE Buchungen SET Datum = ? , Beleg = ? , BuchungText = ?, Soll = ?, Haben = ?, Betrag = ? WHERE ID = ?");
		//updateBuchung.setDate(1, (java.sql.Date)pBuchung.getDatum());
		java.sql.Date lDate = new java.sql.Date(pBuchung.getDatum().getTime());
		updateBuchung.setDate(1, lDate);
		updateBuchung.setString(2, pBuchung.getBeleg());
		updateBuchung.setString(3, pBuchung.getBuchungTextSql());
		updateBuchung.setInt(4, pBuchung.getSoll());
		updateBuchung.setInt(5, pBuchung.getHaben());
		updateBuchung.setDouble(6, pBuchung.getBetrag());
		//updateBuchung.setLong(7, pBuchung.getID());
		updateBuchung.executeUpdate();
	}

	/**
	 * Andere Context-Member dazugefügt. (nicht impl.)
	 */
	@Override
	public void childrenAdded(BeanContextMembershipEvent bcme) {
		// Trace.println(4, "BuchungData.childrenAdded() called, not impl.");
	}

	/**
	 * Andere Context-Member entfernen. (nicht impl.)
	 */
	@Override
	public void childrenRemoved(BeanContextMembershipEvent bcme) {
		// Trace.println(4, "BuchungData.childrenRemoved) called, not impl.");
	}

	/** Die Nummer der BuchungId bis zu dieser gesichert wurde
	 */
	public long getIdSaved() {
		return idSaved;
	}

	/** Die Nummer der BuchungId bis zu dieser gesichert wurde.
	 * Ist die aktuelle Id mNextBuchungId
	 */
	public void setIdSaved() {
		idSaved = getLastBuchungId();
	}
}
