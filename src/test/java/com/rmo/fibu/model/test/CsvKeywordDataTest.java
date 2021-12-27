package com.rmo.fibu.model.test;

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.CsvCompany;
import com.rmo.fibu.model.CsvCompanyData;
import com.rmo.fibu.model.CsvKeyKonto;
import com.rmo.fibu.model.CsvKeyKontoData;
import com.rmo.fibu.model.CsvParserBase;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.util.Config;



public class CsvKeywordDataTest {

	private static final String dbName = "FibuLeer";
	private static String companyName = CsvParserBase.companyNamePost;
	private static CsvCompany mCompany = null;
	private static CsvCompanyData mCompanyData = null;

	private CsvKeyKontoData mKeywordData = null;


	/** Setup Database
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		Config.readProperties();
		DbHandling.makeDb(dbName);
		DbConnection.open(dbName);
		try {
			mCompanyData = (CsvCompanyData) DataBeanContext.getContext().getDataBean(CsvCompanyData.class);
			CsvCompany lCompany = new CsvCompany();
			lCompany.setCompanyID(0);
			lCompany.setCompanyName(companyName);
			lCompany.setDirPath("dir");
			lCompany.setKontoNrDefault("1000");
			mCompanyData.addData(lCompany);
			mCompany = mCompanyData.readData(companyName);
		} catch (FibuException ex) {
			fail(ex.getMessage());
			mCompany.getCompanyID();
		}		
		
	}

	
	@Test
	public void testAddEmptyRow() {
		try {
			mKeywordData = (CsvKeyKontoData) DataBeanContext.getContext().getDataBean(CsvKeyKontoData.class);
			mKeywordData.addEmptyRow(new CsvKeyKonto());
		} catch (FibuException ex) {
			fail(ex.getMessage());
		}		
	}
	
	@Test
	public void readEmptyRow() {
		try {
			mKeywordData = (CsvKeyKontoData) DataBeanContext.getContext().getDataBean(CsvKeyKontoData.class);
			mKeywordData.addEmptyRow(new CsvKeyKonto());
//			assertNotNull("Keyword nicht gefunden", lKeyword.getSuchWort());  
		} catch (FibuException ex) {
			fail(ex.getMessage());
		}		

	}
	
	
	@AfterClass
	public static void afterClass() {
		DbHandling.deleteDb(dbName);
	}

}
