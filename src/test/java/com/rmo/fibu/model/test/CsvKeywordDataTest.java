package com.rmo.fibu.model.test;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.ParserBankData;
import com.rmo.fibu.model.ParserKeyWord;
import com.rmo.fibu.model.ParserKeywordData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.CsvParserBase;
import com.rmo.fibu.util.ParserBank;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CsvKeywordDataTest {

	private static final String dbName = "FibuTest";
	private static String companyName = CsvParserBase.companyNamePost;
	private static ParserBank mCompany = null;
	private static ParserBankData mCompanyData = null;

	private ParserKeywordData mKeywordData = null;


	/** Setup Database
	 */
	@BeforeAll
	static void beforeClass() throws Exception {
		Config.readPropertyFile();
		DbHandling.deleteDb(dbName);
		DbHandling.makeDb(dbName);
		DbConnection.open(dbName);
		
		
		try {

			mCompanyData = (ParserBankData) DataBeanContext.getDataBean(ParserBankData.class);
			mCompanyData.checkTableVersion();

			ParserBank lCompany = new ParserBank();
			lCompany.setBankID(0);
			lCompany.setBankName(companyName);
			lCompany.setDirPath("dir");
			lCompany.setKontoNrDefault("1000");
			mCompanyData.addData(lCompany);
			mCompany = mCompanyData.readData(companyName);
		} catch (FibuException ex) {
			fail(ex.getMessage());
			mCompany.getBankID();
		}

	}


	@Test
	@Order(1)
	void testAddEmptyRow() throws Exception {
		try {
			mKeywordData = (ParserKeywordData) DataBeanContext.getDataBean(ParserKeywordData.class);
			
			mKeywordData.addEmptyRow(new ParserKeyWord(mCompany.getBankID(), "such", null, "S", "neu"));
		} catch (FibuException ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	@Order(2)
	void readEmptyRow() throws Exception {
		try {
			mKeywordData = (ParserKeywordData) DataBeanContext.getDataBean(ParserKeywordData.class);
			// eine Zeile lesen, die es nicht gibt
			ParserKeyWord keyWord = mKeywordData.readAt(0, 1);
			assertNull(keyWord);
//			assertNotNull("Keyword nicht gefunden", lKeyword.getSuchWort());
		} catch (FibuException ex) {
			assertNotNull(ex, ex.getMessage());
		}

	}


}
