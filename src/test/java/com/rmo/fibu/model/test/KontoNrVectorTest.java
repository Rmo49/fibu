package com.rmo.fibu.model.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.Konto;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.model.KontoNrVector;



/**
 * Test der Klasse KontoNrVector.
 * Es sollten einige Kontos in der DB vorhanden sein.
 */

public class KontoNrVectorTest {

	static KontoNrVector mKontoNrVector;
	static String dbName = "fibuTest";
	static  KontoData mKontoData;
	static  Konto mKonto1000;
	static  Konto mKonto1001;


	/** Setup Test-Objects
	 */
	@BeforeAll
	static void setUp() throws Exception  {

		DbHandling.deleteDb(dbName);
		DbHandling.makeDb(dbName);
		DbConnection.open(dbName);

		mKontoData = (KontoData) DataBeanContext.getDataBean(KontoData.class);
		mKonto1000 = new Konto(1000, "Konto 1000", 1.00, 1000.50, true);
		mKontoData.add(mKonto1000);
		mKonto1001 = new Konto(1001, "Konto 1001", 2.00, 1001.00, true);
		mKontoData.add(mKonto1001);

		// versichern, dass gespeichert.
		assertTrue(KontoDataTest.isSame(mKonto1000, mKontoData.read(1000)), mKonto1000.getKontoNrAsString());
		
		mKontoNrVector = new KontoNrVector();
	}


	/** Testet das Lesen des ersten elementes
	 */
	@Test
	public void testGetData() throws Exception {
		String nr = mKontoNrVector.get(0);
		assertNotNull(nr);
	}

	

}