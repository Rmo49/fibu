package com.rmo.fibu.model.test;

import java.text.ParseException;
import java.util.Iterator;

import com.rmo.fibu.exception.BuchungNotFoundException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Test der Klasse BuchungDataModel
 */
public class BuchungDataTest extends TestCase {
	private final String dbName = "FibuLeer";
	private BuchungData mBuchungData;
	private Buchung mBuchung1;
	private Buchung mBuchung2;
	private Buchung mBuchung3;
	private Buchung mBuchung4;
	private Buchung mBuchung11;

	/**
	 * BuchungDataTest constructor comment.
	 * @param name java.lang.String
	 */
	public BuchungDataTest(String name) {
		super(name);
	}

	/** Diese Tests starten
	*/
	public static void main(String[] args) {
		try {
			Config.sDatumVon.setNewDatum("1.1.1990");
			Config.sDatumBis.setNewDatum("31.12.2050");
		}
		catch (ParseException ex) {
			Trace.println(1, "Config.readProperties(): " + ex.getMessage());
		}
		junit.textui.TestRunner.run(suite());
	}

	/** Setup Test-Objects
	 */
	@Override
	public void setUp() throws Exception {
		DbHandling.deleteDb(dbName);
		DbHandling.makeDb(dbName);
		DbConnection.open(dbName);

		// mBuchungData = new BuchungData();
		mBuchungData = (BuchungData) DataBeanContext.getContext().getDataObject(BuchungData.class);
		mBuchung1 = new Buchung(-1, "1.11.2001", "1", "Buchung 1", 1000, 2000, 111.10);
		mBuchung2 = new Buchung(-1, "2.11.2001", "2", "Buchung 2", 1001, 2000, 222.20);
		mBuchung3 = new Buchung(-1, "3.11.2001", "3", "Buchung 3", 1000, 2000, 3333.00);
		mBuchung4 = new Buchung(-1, "4.11.2001", "4", "Buchung 4", 1000, 2000, 44.00);
		mBuchung11 = new Buchung(-1, "11.11.2001", "11", "Buchung 4 mit spez ' \" char", 1000, 2000, 111.00);
	}

	/** Hier werden alle TestSchritte zusammengestellt.
	 Generisch oder Einzeln, wobei bei Generisch die Reihenfolge nicht bestimmt werden kann.
	*/
	public static Test suite() {
		// Generisch: alle Tests von BuchungData
		//TestSuite suite = new TestSuite(BuchungDataTest.class);
		TestSuite suite = new TestSuite("Manual");
		suite.addTest(new BuchungDataTest("testIteratorDelete"));
		suite.addTest(new BuchungDataTest("testIteratorLeer"));
		suite.addTest(new BuchungDataTest("testAddBuchung"));
		suite.addTest(new BuchungDataTest("testAddBuchungWithSpezChar"));
		suite.addTest(new BuchungDataTest("testSaveBuchung"));
		suite.addTest(new BuchungDataTest("testUpdate"));
		suite.addTest(new BuchungDataTest("testUpdateNew"));
		suite.addTest(new BuchungDataTest("testDel"));
		suite.addTest(new BuchungDataTest("testIterator2"));
		return suite;
	}

	/** Eine Buchung dazufügen
	 */
	public void testSaveBuchung() throws Exception {
		mBuchungData.save(mBuchung1);
		assertEquals(mBuchung1, mBuchungData.read(mBuchung1.getID()));
		mBuchungData.save(mBuchung2);
		assertEquals(mBuchung2, mBuchungData.read(mBuchung2.getID()));
	}

	/** Eine Buchung dem temporären Speicher dazufügen
	 */
	public void testAddBuchung() throws Exception {
		mBuchungData.add(mBuchung1);
		mBuchungData.add(mBuchung2);
		mBuchungData.saveNew();
		assertEquals(mBuchung1, mBuchungData.read(mBuchung1.getID()));
		assertEquals(mBuchung2, mBuchungData.read(mBuchung2.getID()));
	}

	/** Eine Buchung mit speziellem Char
	 */
	public void testAddBuchungWithSpezChar() throws Exception {
		mBuchungData.add(mBuchung11);
		mBuchungData.saveNew();
		assertEquals(mBuchung11, mBuchungData.read(mBuchung11.getID()));
	}

	/** Eine Buchung ändern
	 */
	public void testUpdate() throws Exception {
		// zuerst lesen
		Buchung lBuchung = mBuchungData.read("2");
		lBuchung.setBuchungText("Buchung 2 geändert");
		mBuchungData.save(lBuchung);
		assertEquals(lBuchung, mBuchungData.read(lBuchung.getID()));
	}

	/** Eine neue Buchung ändern
	 */
	public void testUpdateNew() throws Exception {
		// zuerst speichern
		mBuchungData.add(mBuchung4);
		Buchung lBuchung = mBuchungData.readAt(mBuchungData.getRowCount()-1);
		lBuchung.setBuchungText("Buchung 4 geändert");
		mBuchungData.add(lBuchung);
		mBuchungData.saveNew();
		assertEquals(lBuchung, mBuchungData.read(mBuchung4.getID()));
	}

	/** Die Liste von Buchungen (Länge der Liste, new dazufügen, nicht speichern)
	 */
	public void testDel() throws Exception {
		int lRowCount = mBuchungData.getRowCount();
		// die Buchung an der Position 1 (müsste gespeichert sein) lesen
		Buchung lBuchung1 = mBuchungData.readAt(1);
		assertNotNull(lBuchung1);
		// eine Buchung dazufügen
		mBuchungData.add(mBuchung3);
		assertEquals("Anzahl Rows nach add", lRowCount+1, mBuchungData.getRowCount());
		// die zuletzt gespeicherte Buchung lesen
		Buchung lBuchung2 = mBuchungData.readAt(lRowCount);
		// Gespeicherte Buchung an der Position 1 löschen
		mBuchungData.delete(lBuchung1.getID());
		assertEquals("Anzahl Rows nach del", lRowCount, mBuchungData.getRowCount());
		// Buchung im temporären Speicher löschen
		mBuchungData.delete(lBuchung2.getID());
		assertEquals("Anzahl Rows nach del", lRowCount-1, mBuchungData.getRowCount());
		// über die Grenze lesen
		try {
			lBuchung2 = mBuchungData.readAt(lRowCount);
			assertTrue("Lesen über die Grenze, muss Exception werfen", true);
		} catch (BuchungNotFoundException e) {
			assertNotNull("Exception erwartet, lesen über Grenze", e);
		}
	}

	/** Alle Buchungen löschen
	 */
	public void testIteratorDelete() throws Exception {
		Iterator<Buchung> iter = mBuchungData.getIterator();
		while (iter.hasNext()) {
			iter.remove();
		}
	}

	/** Versuch eine Buchung zu lesen
	 */
	public void testIteratorLeer() throws Exception {
		Iterator<Buchung> iter = mBuchungData.getIterator();
		if (iter.hasNext()) {
			fail("Iterator sollte leer sein");
		}
	}

	/** Iteration über alle Buchungen
	 */
	public void testIterator2() throws Exception {
		Iterator<Buchung> iter = mBuchungData.getIterator();
		while (iter.hasNext()) {
			Buchung result = iter.next();
			assertNotNull(result);
		}
	}

}
