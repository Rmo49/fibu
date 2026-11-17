package com.rmo.fibu.model.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.model.DbConnection;
import com.rmo.fibu.model.ParserKeyWord;
import com.rmo.fibu.model.ParserKeywordData;
import com.rmo.fibu.util.Config;

/**
 * Test mit Keywords für CSV und PDF
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParserKeywordDataTest {
	static String dbName = "fibuTest";
	static ParserKeywordData mParserData;
	
	int bankID1 = 1;
	int bankID2 = 2;
	ParserKeyWord keyWord1 = new ParserKeyWord(bankID1, "Test1", "1000", "S", "Test1 neu");
	ParserKeyWord keyWord2 = new ParserKeyWord(bankID1, "Test2", "1000", "S", "");
	ParserKeyWord keyWord5 = new ParserKeyWord(bankID2, "Test5", "1000", "S", "Test5 neu");
	ParserKeyWord keyWord6 = new ParserKeyWord(bankID2, "Test6", "1000", "S", "");

	
	
	@BeforeAll
	static void setup () throws Exception {
		Config.readPropertyFile();
		Config.setAllProperties();
		Config.saveProperties();
		DbHandling.deleteDb(dbName);
		DbHandling.makeDb(dbName);
		DbConnection.open(dbName);
		
		mParserData = (ParserKeywordData) DataBeanContext.getDataBean(ParserKeywordData.class);	
	}
	
	@Test
	@Order(0)
	void oldTable () {
		try {
			Statement statement = DbConnection.getConnection().createStatement();
			statement.execute("Drop Table " + ParserKeywordData.TABLE_NAME + ";");
			// die alte Version anlegen
			statement.execute(ParserKeywordData.CREATE_CSVKEYWORD);			
		}
		catch (SQLException ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	@Order(1)
	void newTable () {
		mParserData.checkTableVersion();
		assertEquals(6, mParserData.getAnzahlCols());
	}

	
	@Test
	@Order(4)
	void add1 () {
		try {
			mParserData.add(keyWord1);
		}
		catch (SQLException ex) {
			fail(ex.getMessage());
		}
		try {
			ParserKeyWord keyWord = mParserData.readAt(bankID1, 0);
			assertEquals(keyWord1.getBankId(), keyWord.getBankId());
			assertEquals(keyWord1.getSuchWort(), keyWord.getSuchWort());
			assertEquals(keyWord1.getKontoNr(), keyWord.getKontoNr());			
			assertEquals(keyWord1.getSh(), keyWord.getSh());
		}
		catch (FibuException ex) {
			assertNotNull(ex, ex.getMessage());
		}
	}

	
	@Test
	@Order(5)
	void löschenEinTupel() {
		try {
			ParserKeyWord keyWord = mParserData.readAt(bankID1, 0);
			mParserData.deleteRow(keyWord.getId());
		}
		catch (Exception ex) {
			fail("probleme beim löschen");
		}
	}
	
	
	@Test
	@Order(6)
	void add2() {
		try {
			mParserData.add(keyWord1);
			mParserData.add(keyWord2);
			mParserData.add(keyWord5);
			mParserData.add(keyWord6);
		}
		catch (SQLException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	@Order(7)
	void löschenBankRow() {
		try {
			mParserData.deleteAllRowsOfBank(bankID1);
		}
		catch (SQLException ex) {
			fail("probleme beim löschen");
		}
	}

	@Test
	@Order(8)
	void bankNochmalsLesen() {
		int rows = mParserData.getRowCount(bankID1);
		assertEquals(0, rows);	
		rows = mParserData.getRowCount(bankID2);
		assertEquals(2, rows);	
	}

	
}
