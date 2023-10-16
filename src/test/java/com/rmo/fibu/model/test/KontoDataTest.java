package com.rmo.fibu.model.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rmo.fibu.exception.KontoNotFoundException;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoCalculator;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.util.Config;

/** Test der Klasse KontoData
 */
public class KontoDataTest {
	private static final String dbName = "FibuLeer";
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
	@BeforeClass
	public static void beforeClass() throws Exception {
		Config.readProperties();
		DbHandling.makeDb(dbName);
		DbConnection.open(dbName);

		mKontoData = new KontoData();
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
		assertEquals(mKonto1000, mKontoData.read(1000));
		mKontoData.add(mKonto1001);
		assertEquals(mKonto1001, mKontoData.read(1001));
		mKontoData.add(mKonto2000);
		assertEquals(mKonto2000, mKontoData.read(2000));
		mKontoData.add(mKonto2002);
		assertEquals(mKonto2002, mKontoData.read(2002));
	}

	/** Zuerst dazufügen, dann löschen.
	 */
	@Test
	public void testDelete3002() throws Exception {
		mKontoData.add(mKonto3002);
		assertEquals(mKonto3002, mKontoData.read(3002));
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
		assertEquals(mKonto1000Update, mKontoData.read(1000));
	}

	/** Berechnen aller Saldi */
	@Test
	public void testBerechneSaldo() throws KontoNotFoundException {
		KontoCalculator calculator = new KontoCalculator();
		calculator.calculateSaldo();
	}

	/** Vergleicht alle Attribute von 2 Konti.
	 */
	private void assertEquals(Konto expected, Konto actual) {
		Assert.assertEquals("KontoNr", expected.getKontoNr(), actual.getKontoNr());
		Assert.assertEquals("Text", expected.getText(), actual.getText());
		Assert.assertEquals("StartSaldo Kto:" + actual.getKontoNr(), expected.getStartSaldo(), actual.getStartSaldo(), 0.1);
		Assert.assertEquals("Saldo", expected.getSaldo(), actual.getSaldo(), 0.1);
		Assert.assertEquals("IstSollKto", expected.isSollKonto() , actual.isSollKonto());
	}

	@AfterClass
	public static void afterClass() {
		DbHandling.deleteDb(dbName);
	}
}
