package com.rmo.fibu.model.test;

import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.KontoNrVector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test der Klasse KontoNrVector.
 * Es sollten einige Kontos in der DB vorhanden sein.
 */

public class KontoNrVectorTest extends TestCase {

	//private DataBeanContext mDataContext;
	//private KontoData mKontoData;
	private KontoNrVector mKontoNrVector;

	public KontoNrVectorTest(String name) {
		super(name);
	}

	/** Setup Test-Objects
	 */
	@Override
	public void setUp() throws Exception  {
		super.setUp();
		DbConnection.open("FibuLeer");
		//mDataContext = DataBeanContext.getContext();
		//mKontoData = (KontoData) mDataContext.getDataBean(KontoData.class);
		mKontoNrVector = new KontoNrVector();
	}

	/** Hier werden alle TestSchritte zusammengestellt.
	 Generisch oder Einzeln, wobei bei Generisch die Reihenfolge nicht bestimmt werden kann.
	*/
	public static Test suite() {
		// Generisch: alle Tests von DataBeanContext
		TestSuite suite = new TestSuite(KontoNrVectorTest.class);
		return suite;
	}

	/** Testet das Lesen des ersten elementes
	 */
	public void testGetData() throws Exception {
		String nr = mKontoNrVector.get(0);
		assertNotNull(nr);
	}

	/** Diese Tests starten
	*/
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}