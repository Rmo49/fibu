package com.rmo.fibu.model.depot.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.rmo.fibu.model.depot.DbConnectionDepot;
import com.rmo.fibu.model.depot.TransaktionData;
import com.rmo.fibu.model.depot.DepotDataBase;
import com.rmo.fibu.model.depot.Transaktion;

@Order(5)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransaktionTest {

	@Test
	@Order(1)
	void testDbOpen() throws Exception {
		DepotDataBase.openDepot(DbHandlingDepot.dbName);
		assertNotNull(DbConnectionDepot.getConnection());
	}

	
	@Test
	@Order(2)
	void add() throws Exception {
		Transaktion buchung = new Transaktion("01.01.2020", "AA", "Einkauf", 123.40);
		TransaktionData.getInstance().add(buchung);
		
		Transaktion buchung2 = new Transaktion();
		buchung2.setDatum("02.02.2022");
		buchung2.setStockNr("AA");
		buchung2.setText("Verkauf");
		buchung2.setBetrag(220.20);
		TransaktionData.getInstance().add(buchung2);

		Transaktion buchung3 = new Transaktion();
		buchung3.setDatum("03.03.2022");
		buchung3.setStockNr("CC Stock");
		buchung3.setText("Test Null");
		buchung3.setBetrag(null);
		TransaktionData.getInstance().add(buchung3);
		
		
	}

	@Test
	@Order(4)
	void read() throws Exception {
		List<Transaktion> buchung = TransaktionData.getInstance().readAll("AA");
		assertEquals(buchung.size(), 2);
	}

	@Test
	@Order(4)
	void readAll() throws Exception {
		TransaktionData transData = TransaktionData.getInstance();
		Iterator<Transaktion> iter = transData.getIterator();
		while (iter.hasNext()) {
			Transaktion trans = iter.next();
			assertNotNull(trans.getDatum());
		}
	}
	
	
}
