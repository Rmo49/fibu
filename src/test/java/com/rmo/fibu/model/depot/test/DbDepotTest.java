package com.rmo.fibu.model.depot.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.rmo.fibu.model.depot.DbConnectionDepot;
import com.rmo.fibu.util.Config;

/**
 * Datenbank anlegen und wieder löschen
 *
 */

@Order(1)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DbDepotTest {

	/**
	 * Setup Test-Objects
	 */
	@BeforeAll
	public static void beforeClass() throws Exception {
//		DbHandlingDepot.deleteDb(dbName);
//		DbHandlingDepot.makeDb(dbName);
	}

	/**
	 * Ist Config vorhanden?
	 */
	@Test
	@Order(1)
	void testConfig() throws Exception {
		Config.checkArgs(null);
		String confFile = Config.sConfigFile.getAbsolutePath();
		assertNotNull(confFile);
		Config.readPropertyFile();
		Config.setAllProperties();
		assertTrue(Config.dbPassword.length() > 3);
	}

	/**
	 * Ist Config vorhanden?
	 */
	@Test
	@Order(2)
	void testDbPassword() throws Exception {
		String url = Config.dbUrl;
		assertNotNull(url);
		assertNotNull(Config.dbPassword);
	}

	/**
	 * Ist DB öffnen und löschen, neue anlegen
	 */
	@Test
	@Order(5)
	void testDbOpen() throws Exception {
		DbHandlingDepot.deleteDb(DbHandlingDepot.dbName);
		DbHandlingDepot.makeDb(DbHandlingDepot.dbName);	
		DbConnectionDepot.open(DbHandlingDepot.dbName);
		Connection conn = DbConnectionDepot.getConnection();
		String con = conn.getSchema();
		System.out.println(con);

		assertNotNull(DbConnectionDepot.getConnection());
		conn = DbConnectionDepot.getConnection();
		con = conn.getSchema();
		System.out.println(con);
	}

}
