package com.rmo.fibu.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Point;

import javax.swing.JList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.util.Config;

/**
 * Test der Config Klasse.
 */
public class ConfigTest {
	
	Point point1 = new Point(10, 20);
	
	@BeforeAll
	static void setup() throws FibuException {
		Config.setConfigFileName("TestConfig.txt");
		
		String[] args = new String[0];
		Config.checkArgs(args);
		try {
			// Config-File einlesen
			Config.readPropertyFile();
		}
		catch (FibuException ex) {
			// muss exception werfen
			assertNotNull(ex);
		}
		// alle Werte auf Default setzen
		Config.setAllProperties();
	}

	@Test
	void addFibuList() throws Exception {
		Config.addFibuToList("Test1");
		Config.addFibuToList("Test2");
		assertEquals(2, Config.getFibuList().getSize());
		assertEquals("Test1", Config.getFibuList().get(0));		
	}

	@Test
	void readProperties() throws Exception {
		assertEquals(Config.dbPassword, "xxx");
		Config.winBilanzenLoc = point1;
		Config.saveProperties();
		Config.readPropertyFile();
		Config.setAllProperties();
		assertEquals(Config.winBilanzenLoc.x, point1.x);
		assertEquals(Config.winBilanzenLoc.y, point1.y);
	}
	
	

}
