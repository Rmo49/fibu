package com.rmo.fibu.model.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.SQLException;

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

/**
 * Test mit Keywords für CSV und PDF
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParserKeywordDataTest {
	static String dbName = "fibuTest";
	static ParserKeywordData mData;
	
	int bankID1 = 1;
	int bankID2 = 2;
	ParserKeyWord keyWord1 = new ParserKeyWord(bankID1, "Test1", "1000", "S", "Test1 neu");
	ParserKeyWord keyWord2 = new ParserKeyWord(bankID1, "Test2", "1000", "S", "");
	ParserKeyWord keyWord5 = new ParserKeyWord(bankID2, "Test5", "1000", "S", "Test5 neu");
	ParserKeyWord keyWord6 = new ParserKeyWord(bankID2, "Test6", "1000", "S", "");

	
	
	@BeforeAll
	static void setup () {
		DbHandling.deleteDb(dbName);
		DbHandling.makeDb(dbName);
		DbConnection.open(dbName);
		
		mData = (ParserKeywordData) DataBeanContext.getDataBean(ParserKeywordData.class);
		
	}
	
	//TODO Test mit Versionen der Tabelle
	
	@Test
	@Order(1)
	void add1 () {
		try {
			mData.add(keyWord1);
		}
		catch (SQLException ex) {
			fail(ex.getMessage());
		}
		try {
			ParserKeyWord keyWord = mData.readAt(bankID1, 0);
			assertEquals(keyWord1.getBankId(), keyWord.getBankId());
			assertEquals(keyWord1.getSuchWort(), keyWord.getSuchWort());
		}
		catch (FibuException ex) {
			assertNotNull(ex, ex.getMessage());
		}
	}

	
	@Test
	@Order(2)
	void löschenEinTupel() {
		try {
			ParserKeyWord keyWord = mData.readAt(bankID1, 0);
			mData.deleteRow(keyWord.getId());
		}
		catch (Exception ex) {
			fail("probleme beim löschen");
		}
	}
	
	
	@Test
	@Order(3)
	void add2() {
		try {
			mData.add(keyWord1);
			mData.add(keyWord2);
			mData.add(keyWord5);
			mData.add(keyWord6);
		}
		catch (SQLException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	@Order(5)
	void löschenBankRow() {
		try {
			mData.deleteAllRowsOfBank(bankID1);
		}
		catch (SQLException ex) {
			fail("probleme beim löschen");
		}
	}

	@Test
	@Order(6)
	void bankNochmalsLesen() {
		int rows = mData.getRowCount(bankID1);
		assertEquals(0, rows);	
		rows = mData.getRowCount(bankID2);
		assertEquals(2, rows);	
	}

	
}
