package com.rmo.fibu.model.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Hauptklasse, startet alle Tests, von JUnit 3.0
 */
public class AllModelTests extends TestCase {
	/** AllTests constructor comment. */
	public AllModelTests() {
		super("All Model Test");
	}

	public static Test suite() {
		// alle Tests von Model
		TestSuite suite = new TestSuite("All Fibu-Model Test");
		suite.addTest(DataBeanContextTest.suite());
//		suite.addTest(KontoDataTest.suite());
		suite.addTest(KontoNrVectorTest.suite());
		suite.addTest(BuchungDataTest.suite());
		return suite;
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
