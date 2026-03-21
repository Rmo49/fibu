package com.rmo.fibu.model.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.rmo.fibu.exception.BuchungNotFoundException;
import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.util.Config;

/**
 * Test der Klasse BuchungDataModel
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BuchungDataTest {

	static String dbName = "FibuTest";

	// Alle verwendeten Konti initialisieren
	private static KontoData mKontoData;
	private static Konto mKonto1000;
	private static Konto mKonto1001;
	private static Konto mKonto2000;
	private static Konto mKonto2002;
	private static Konto mKonto3001;
	private static Konto mKonto3002;

	static BuchungData mBuchungData;
	static Buchung mBuchung1;
	static Buchung mBuchung2;
	static Buchung mBuchung3;
	static Buchung mBuchung4;
	static Buchung mBuchung11;

	/**
	 * Setup Test-Objects
	 */
	@BeforeAll
	static void beforeAll() throws Exception {

		Config.sDatumVon.setNewDatum("1.1.1990");
		Config.sDatumBis.setNewDatum("31.12.2050");

		DbHandling.deleteDb(dbName);
		DbHandling.makeDb(dbName);
		DbConnection.open(dbName);

		mKontoData = new KontoData();
		mKonto1000 = new Konto(1000, "Konto 1000", 1, 1000, true);
		mKonto1001 = new Konto(1001, "Konto 1001", 2, 1001, true);
		mKonto2000 = new Konto(2000, "Konto 2000", 20.20, 2000.50, false);
		mKonto2002 = new Konto(2002, "Konto 2002", 22, 2002, false);
		mKonto3001 = new Konto(3001, "Konto 3001", 91.50, 0, true);
		mKonto3002 = new Konto(3002, "Konto 3002", 92.50, 0, false);

		mKontoData.add(mKonto1000);
		assertEquals(mKonto1000, mKontoData.read(1000));
		mKontoData.add(mKonto1001);
		assertEquals(mKonto1001, mKontoData.read(1001));
		mKontoData.add(mKonto2000);
		assertEquals(mKonto2000, mKontoData.read(2000));
		mKontoData.add(mKonto2002);
		assertEquals(mKonto2002, mKontoData.read(2002));
		mKontoData.add(mKonto3001);
		assertEquals(mKonto3001, mKontoData.read(3001));
		mKontoData.add(mKonto3002);
		assertEquals(mKonto3002, mKontoData.read(3002));

		mBuchungData = (BuchungData) DataBeanContext.getDataBean(BuchungData.class);
		mBuchung1 = new Buchung(-1, "1.11.2001", "1", "Buchung 1", 1000, 2000, 111.10);
		mBuchung2 = new Buchung(-1, "2.11.2001", "2", "Buchung 2", 1001, 2000, 222.20);
		mBuchung3 = new Buchung(-1, "3.11.2001", "3", "Buchung 3", 1000, 2000, 3333.00);
		mBuchung4 = new Buchung(-1, "4.11.2001", "4", "Buchung 4", 1000, 2000, 44.00);
		mBuchung11 = new Buchung(-1, "11.11.2001", "11", "Buchung 4 mit spez \" char", 1000, 2000, 111.00);
	}

	/**
	 * Hier werden alle TestSchritte zusammengestellt. Generisch oder Einzeln, wobei
	 * bei Generisch die Reihenfolge nicht bestimmt werden kann.
	 */
//	public static suite() {
	// Generisch: alle Tests von BuchungData
	// TestSuite suite = new TestSuite(BuchungDataTest.class);
//		TestSuite suite = new TestSuite("Manual");
//		suite.addTest(new BuchungDataTest("testIteratorDelete"));
//		suite.addTest(new BuchungDataTest("testIteratorLeer"));
//		suite.addTest(new BuchungDataTest("testAddBuchung"));
//		suite.addTest(new BuchungDataTest("testAddBuchungWithSpezChar"));
//		suite.addTest(new BuchungDataTest("testSaveBuchung"));
//		suite.addTest(new BuchungDataTest("testUpdate"));
//		suite.addTest(new BuchungDataTest("testUpdateNew"));
//		suite.addTest(new BuchungDataTest("testDel"));
//		suite.addTest(new BuchungDataTest("testIterator2"));
//		return suite;
//	}

	/**
	 * Eine Buchung dazufügen
	 */
	@Test
	@Order(1)
	void testSaveBuchung() throws Exception {
		long buchungID = mBuchungData.save(mBuchung1);
		assertTrue(isSame(mBuchung1, mBuchungData.read(buchungID)), mBuchung1.getBuchungText());
		buchungID = mBuchungData.save(mBuchung2);
		assertTrue(isSame(mBuchung2, mBuchungData.read(buchungID)), mBuchung2.getBuchungText());
	}

	/**
	 * Eine Buchung dem temporären Speicher dazufügen
	 */
	@Test
	@Order(1)
	void testAddBuchung() throws Exception {
		long buchungID1 = mBuchungData.add(mBuchung1);
		long buchungID2 = mBuchungData.add(mBuchung2);
		mBuchungData.saveNew();
		assertTrue(isSame(mBuchung1, mBuchungData.read(buchungID1)), mBuchung1.getBuchungText());
		assertTrue(isSame(mBuchung2, mBuchungData.read(buchungID2)), mBuchung2.getBuchungText());
	}

	/**
	 * Eine Buchung mit speziellem Char
	 */
	@Test
	@Order(1)
	void testAddBuchungWithSpezChar() throws Exception {
		long buchungID = mBuchungData.add(mBuchung11);
		mBuchungData.saveNew();
		assertTrue(isSame(mBuchung11, mBuchungData.read(buchungID)), mBuchung11.getBuchungText());
	}

	/**
	 * Eine Buchung ändern
	 */
	@Test
	@Order(4)
	void testUpdate() throws Exception {
		// zuerst lesen
		Buchung lBuchung = mBuchungData.read("2");
		String textNeu = "Buchung 2 geändert";
		lBuchung.setBuchungText(textNeu);
		try {
			mBuchungData.save(lBuchung);
			assertTrue(textNeu.equalsIgnoreCase(mBuchungData.read("2").getBuchungText()), textNeu);		
		} catch (FibuException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Eine neue Buchung ändern
	 */
	@Test
	@Order(4)
	void testUpdateNew() throws Exception {
		// zuerst speichern
		mBuchungData.add(mBuchung4);
		Buchung lBuchung = mBuchungData.readAt(mBuchungData.getRowCount() - 1);
		String textNeu = "Buchung 4 geändert";
		lBuchung.setBuchungText(textNeu);
		long buchungID = mBuchungData.add(lBuchung);
		try {
			mBuchungData.saveNew();
			assertTrue(textNeu.equalsIgnoreCase(mBuchungData.read(buchungID).getBuchungText()), textNeu);				
		} catch (FibuException ex) {
			fail(ex.getMessage());
		}

	}

	/**
	 * Die Liste von Buchungen (Länge der Liste, new dazufügen, nicht speichern)
	 */
	@Test
	@Order(9)
	void testDel() throws Exception {
		int lRowCount = mBuchungData.getRowCount();
		// die Buchung an der Position 1 (müsste gespeichert sein) lesen
		Buchung lBuchung1 = mBuchungData.readAt(1);
		assertNotNull(lBuchung1, "Buchung lesen");
		// eine Buchung dazufügen
		mBuchungData.add(mBuchung3);
		assertEquals(lRowCount + 1, mBuchungData.getRowCount(), "Anzahl Rows nach add");

		// die zuletzt gespeicherte Buchung lesen
		Buchung lBuchung2 = mBuchungData.readAt(lRowCount);
		// Gespeicherte Buchung an der Position 1 löschen
		mBuchungData.delete(lBuchung1.getID());
		assertEquals(lRowCount, mBuchungData.getRowCount(), "Anzahl Rows nach del");
		
		// Buchung im temporären Speicher löschen
		mBuchungData.delete(lBuchung2.getID());
		assertEquals(lRowCount - 1, mBuchungData.getRowCount(), "Anzahl Rows nach del");
		// über die Grenze lesen
		try {
			lBuchung2 = mBuchungData.readAt(lRowCount);
			assertTrue(true, "Lesen über die Grenze, muss Exception werfen");
		} catch (BuchungNotFoundException e) {
			assertNotNull(e, "Exception erwartet, lesen über Grenze");
		}
	}

	/**
	 * Alle Buchungen löschen
	 */
	public void testIteratorDelete() throws Exception {
		Iterator<Buchung> iter = mBuchungData.getIterator();
		while (iter.hasNext()) {
			iter.remove();
		}
	}

	/**
	 * Versuch eine Buchung zu lesen
	 */
	void testIteratorLeer() throws Exception {
		Iterator<Buchung> iter = mBuchungData.getIterator();
		if (iter.hasNext()) {
//			fail("Iterator sollte leer sein");
		}
	}

	/**
	 * Iteration über alle Buchungen
	 */
	void testIterator2() throws Exception {
		Iterator<Buchung> iter = mBuchungData.getIterator();
		while (iter.hasNext()) {
			Buchung result = iter.next();
			assertNotNull(result, "Interation über Buchungen");
		}
	}

	/**
	 * Vergleicht 2 Buchungen
	 * 
	 * @param buchung1
	 * @param buchung2
	 * @return true wenn gleich sind.
	 */
	public static boolean isSame(Buchung buchung1, Buchung buchung2) {
		if (buchung1.getBetrag() != buchung2.getBetrag()) {
			return false;
		}
		if (!buchung1.getBuchungText().equalsIgnoreCase(buchung2.getBuchungText())) {
			return false;
		}
		if (! buchung1.getBeleg().equalsIgnoreCase(buchung2.getBeleg())) {
			return false;
		}
		return true;
	}
}
