package com.rmo.fibu.model.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoCalculator;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.util.Config;

/** Test der Klasse KontoData
 */
public class KontoDataTest {
	private static final String dbName = "FibuTest";
	private static  KontoData mKontoData;
	private static  Konto mKonto1000;
	private static  Konto mKonto1001;
	private static  Konto mKonto2000;
	private static  Konto mKonto2002;
	private static  Konto mKonto3001;
	private static  Konto mKonto3002;
	private static  Konto mKonto1000Update;


	/** Setup Test-Objects
	 */
	@BeforeAll
	public static void beforeClass() throws Exception {
		Config.readPropertyFile();
		DbHandling.deleteDb(dbName);
		DbHandling.makeDb(dbName);
		DbConnection.open(dbName);

		mKontoData = (KontoData) DataBeanContext.getDataBean(KontoData.class);
		mKonto1000 = new Konto(1000, "Konto 1000", 1, 1000, true);
		mKonto1001 = new Konto(1001, "Konto 1001", 2, 1001, true);
		mKonto2000 = new Konto(2000, "Konto 2000", 20.20, 2000.50, false);
		mKonto2002 = new Konto(2002, "Konto 2002", 22, 2002, false);
		mKonto3001 = new Konto(3001, "Konto 3001", 91.50, 0, true);
		mKonto3002 = new Konto(3002, "Konto 3002", 92.50, 0, false);
		mKonto1000Update = new Konto(1000, "Konto 1000 new", 10.1, 100.10, false);
	}


	/** Einige Konto dazufügen.
	 */
	@Test
	public void testAdd() throws Exception {
		mKontoData.add(mKonto1000);
		assertTrue(isSame(mKonto1000, mKontoData.read(1000)), mKonto1000.getText());
		mKontoData.add(mKonto1001);
		assertTrue(isSame(mKonto1001, mKontoData.read(1001)), mKonto1001.getText());
		mKontoData.add(mKonto2000);
		assertTrue(isSame(mKonto2000, mKontoData.read(2000)), mKonto2000.getText());
		mKontoData.add(mKonto2002);
		assertTrue(isSame(mKonto2002, mKontoData.read(2002)), mKonto2002.getText());
	}

	/** Zuerst dazufügen, dann löschen.
	 */
	@Test
	public void testDelete3002() throws Exception {
		mKontoData.add(mKonto3002);
		assertTrue(isSame(mKonto3002, mKontoData.read(3002)), mKonto3002.getKontoNrAsString());
		mKontoData.delete(3002);
		try {
			mKontoData.read(3002);
			fail("Sollte KontoNotFoundException werfen");
		}
		catch (KontoNotFoundException ex) {
			assertNotNull(ex);
		}
	}

	/** Zuerst dazufügen, dann löschen an der letzten Position
	 */
	@Test
	public void testDeleteAt() throws Exception {
		mKontoData.add(mKonto3001);
		assertTrue(isSame(mKonto3001, mKontoData.read(3001)), mKonto3001.getKontoNrAsString());
		assertEquals(mKonto3001, mKontoData.read(3001));
		int pos = mKontoData.getRowCount()-1;
		Konto lKonto = mKontoData.readAt(pos);
		mKontoData.deleteAt(pos);
		try {
			Konto result = mKontoData.read(lKonto.getKontoNr());
			fail("Sollte KontoNotFoundException werfen");
			result.setKontoNr(2);	// damit Fehler bezüglich "never read locally" verschwindet
		}
		catch (KontoNotFoundException ex) {
			assertNotNull(ex);
		}
	}

	/** Anzahl max. Zeilen.
	*/
	@Test
	public void testMaxRows() throws Exception {
		int lMax = mKontoData.getRowCount();
		// wie assert, darf keine Exception werfen
		assertTrue (lMax > 0);
	}

	/** Nicht existierendes Konto.
	 */
	@Test
	public void testReadKontoNotExisting() throws Exception {
		try {
			mKontoData.read(99);
			fail("Should raise an KontoNotFoundException");
		}
		catch(KontoNotFoundException e) {
		}
	}

	/** Ein Konto lesen über Position die es nicht gibt.
	 */
	@Test
	public void testReadAtNotExistion() throws Exception {
		try {
			mKontoData.readAt(99999);
			fail("Should raise an KontoNotFoundException");
		}
		catch(KontoNotFoundException e) {
		}
	}

	/** Ein Konto überschreiben.
	 */
	@Test
	public void testUpdateKonto1000() throws Exception {
		mKontoData.add(mKonto1000Update);
		assertTrue(isSame(mKonto1000Update, mKontoData.read(1000)), mKonto1000Update.getText());
	}

	/** Berechnen aller Saldi */
	@Test
	public void testBerechneSaldo() throws KontoNotFoundException {
		KontoCalculator calculator = new KontoCalculator();
		calculator.calculateSaldo();
	}

	@AfterAll
	public static void afterClass() {
		DbHandling.deleteDb(dbName);
	}

	/**
	 * Vergleicht 2 Konto
	 *
	 * @param konto1
	 * @param konto2
	 * @return true wenn gleich sind.
	 */
	public static boolean isSame(Konto konto1, Konto konto2) {
		if ((konto1.getKontoNr()  != konto2.getKontoNr()) || ! konto1.getText().equalsIgnoreCase(konto2.getText()) || (konto1.getStartSaldo() != konto2.getStartSaldo()) || (konto1.getSaldo() != konto2.getSaldo())) {
			return false;
		}
		if (konto1.isSollKonto() != konto2.isSollKonto()) {
			return false;
		}
		return true;
	}
}
