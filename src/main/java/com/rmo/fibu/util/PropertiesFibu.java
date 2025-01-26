package com.rmo.fibu.util;

import java.util.Properties;

/**
 * Erweiterung von Porperties.
 * Wenn ein Property beim lesen nicht vorhanden ist, wird dieses angelegt.
 */
public class PropertiesFibu extends Properties {

	private static final long serialVersionUID = 1301421174104920697L;

	@Override
	public String getProperty(String key) {
		// wenn Property nicht vorhanden, wird ein leeres property
	    String value = super.getProperty(key);
	    if (value == null) {
			super.setProperty(key, "");
		}
		return super.getProperty(key);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		// wenn Property nicht vorhanden, wird ein property mit default gesetzt
	    String value = super.getProperty(key);
	    if ((value == null) || (value.length() < 1)) {
			super.setProperty(key, defaultValue);
			return defaultValue;
		}
		return super.getProperty(key, defaultValue);
	}


}
