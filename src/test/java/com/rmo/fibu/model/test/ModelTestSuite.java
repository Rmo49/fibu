package com.rmo.fibu.model.test;


import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Alle Model Testcases")
@SelectPackages("com.rmo.fibu.model.test")
@SelectClasses({ KontoDataTest.class, BuchungDataTest.class})
@IncludeClassNamePatterns(".*Test")

public class ModelTestSuite {
	// diese Klasse bleibt leer.
}
