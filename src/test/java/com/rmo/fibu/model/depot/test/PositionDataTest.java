package com.rmo.fibu.model.depot.test;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.rmo.fibu.model.depot.DbConnectionDepot;
import com.rmo.fibu.model.depot.PositionData;
import com.rmo.fibu.model.depot.DepotDataBase;
import com.rmo.fibu.model.depot.Position;
import com.rmo.fibu.util.Datum;

@Order(3)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PositionDataTest {

	/**
	 * DB öffnen
	 */
	@Test
	@Order(1)
	void testDbOpen() throws Exception {
		DepotDataBase.openDepot(DbHandlingDepot.dbName);
		assertNotNull(DbConnectionDepot.getConnection());
	}

	@Test
	@Order(2)
	void add() throws Exception {
		Position position = new Position("AA", "Aktie AA", "Bezeichung AA", 100.20, null, "01.01.2020", "31.12.2022");
		PositionData.getInstance().add(position);
		
		Position position2 = new Position();
		position2.setStockNr("BBA");
		position2.setProdukt("Obli BB");
		position2.setBezeichnung(null);
		position2.setEinstand(200.20);
		position2.setKurs(null);
		position2.setKaufDatum("2.2.22");
		position2.setVerkaufDatum("");
		PositionData.getInstance().add(position2);

		Position position3 = new Position();
		position3.setStockNr("CC Stock");
		position3.setProdukt("CC Prod");
		position3.setBezeichnung("CC Bez");
		position3.setEinstand(300.30);
		position3.setKurs(340.0);
		position3.setKaufDatum("3.3.23");
		position3.setVerkaufDatum("");
		PositionData.getInstance().add(position3);
		
		Position position4 = new Position("DD", "Aktie DD", "Bezeichung DD", 400.00, 0.00, "04.01.2020", "");
		PositionData.getInstance().add(position4);
	}

	@Test
	@Order(4)	
	void read1() throws Exception  {
		Position pos = PositionData.getInstance().read("AA");
		assertTrue(pos.getProdukt().equals("Aktie AA"));
		assertEquals(pos.getEinstand(), 100.20);
		assertTrue(pos.getKaufDatum().compareTo(new Datum("01.01.2020")) == 0);		
	}
	
	@Test
	@Order(4)	
	void read2() throws Exception  {
		Position pos = PositionData.getInstance().read("BBA");
//		assertNull(pos.getBezeichnung());
//		assertNull(pos.getKurs());
		assertNull(pos.getVerkaufDatum());
	}
	
	@Test
	@Order(4)	
	void read3() throws Exception  {
		Position pos = PositionData.getInstance().read("DD");
		assertTrue(pos.getProdukt().equals("Aktie DD"));
		assertEquals(pos.getEinstand(), 400.00);
		assertTrue(pos.getKaufDatum().compareTo(new Datum("04.01.2020")) == 0);
		assertNull(pos.getVerkaufDatum());
	}



}
