package com.rmo.fibu.model.test;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.ParserBankData;
import com.rmo.fibu.model.ParserKeywordData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.KontoData;


/** Test der Klasse DataBeanContext
 */
public class DataBeanContextTest {


    /** Testet das Lesen von Beans vom context
     */
    @Test
    void testGetData() throws Exception {
        KontoData lKontoData = (KontoData) DataBeanContext.getDataBean(KontoData.class);
        assertNotNull(lKontoData);
        KontoData lKontoData2 = (KontoData) DataBeanContext.getDataBean(KontoData.class);
        assertNotNull(lKontoData2);
        BuchungData lBuchungData = (BuchungData) DataBeanContext.getDataBean(BuchungData.class);
        assertNotNull(lBuchungData);
        BuchungData lBuchungData2 = (BuchungData) DataBeanContext.getDataBean(BuchungData.class);
        assertNotNull(lBuchungData2);
        ParserBankData lCsvBankData = (ParserBankData) DataBeanContext.getDataBean(ParserBankData.class);
        assertNotNull(lCsvBankData);
        ParserKeywordData lCsvKeyKontoData = (ParserKeywordData) DataBeanContext.getDataBean(ParserKeywordData.class);
        assertNotNull(lCsvKeyKontoData);      
    }
    
    
 }
