package com.rmo.fibu.model.test;

import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.KontoData;
import com.rmo.fibu.util.Config;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Test der Klasse DataBeanContext
 */
public class DataBeanContextTest extends TestCase {
	private DataBeanContext mDataContext;

    /**
     * BuchungMTest constructor comment.
     * @param name java.lang.String
     */
    public DataBeanContextTest(String name) {
        super(name);
    }

    /** Diese Tests starten
    */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /** Setup Test-Objects
     */
    @Override
	public void setUp() throws Exception {
        super.setUp();
        Config.setDbName("FibuLeer");
        mDataContext = DataBeanContext.getContext();
    }

    /** Hier werden alle TestSchritte zusammengestellt.
     Generisch oder Einzeln, wobei bei Generisch die Reihenfolge nicht bestimmt werden kann.
    */
    public static Test suite() {
        // Generisch: alle Tests von DataBeanContext
        TestSuite suite = new TestSuite(DataBeanContextTest.class);
        return suite;
    }

    /** Testet das Lesen von Beans vom context
     */
    public void testGetData() throws Exception {
        KontoData lKontoData = (KontoData) mDataContext.getDataObject(KontoData.class);
        KontoData lKontoData2 = (KontoData) mDataContext.getDataBean(KontoData.class);
        assertEquals(lKontoData, lKontoData2);
        BuchungData lBuchungData = (BuchungData) mDataContext.getDataObject(BuchungData.class);
        BuchungData lBuchungData2 = (BuchungData) mDataContext.getDataBean(BuchungData.class);
        assertEquals(lBuchungData, lBuchungData2);
    }
}
