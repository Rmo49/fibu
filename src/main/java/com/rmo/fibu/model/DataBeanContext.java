package com.rmo.fibu.model;

import java.util.HashMap;
import java.util.Map;

import com.rmo.fibu.util.Trace;

/**
 * Der Context des Models. Enthält alle Model-Beans (Konto, Buchung etc.). Ist
 * ein Singelton
 */
public class DataBeanContext {

	/**
	 * Die Map mit allen Klassen
	 */
	private static final Map<Class<?>, DataObject> beans = new HashMap<>();

	/**
	 * Setup the Context
	 */
	private static void setup() {
		if (beans.size() == 0) {
			try {
				addBean(BuchungData.class, new BuchungData());
				addBean(KontoData.class, new KontoData());
				addBean(ParserBankData.class, new ParserBankData());
				addBean(ParserKeywordData.class, new ParserKeywordData());

				// FibuDaten zuletzt, da Konto und Buchung einen Link darauf haben
				addBean(FibuData.class, FibuData.getFibuData());
			} catch (Exception e) {
				Trace.println(1, "Error in DataBeanContext.setup(): " + e.getMessage());
			}
		}
	}

	public static <T> void addBean(Class<T> type, DataObject instance) {
		beans.put(type, instance);
	}

	/**
	 * Gibt das Bean der gesuchten Klasse zurück
	 *
	 * @param <T>
	 * @param type
	 * @return
	 */
	public static <T> T getDataBean(Class<T> type) {
		setup();
		return type.cast(beans.get(type));
	}

	/**
	 * In alle Data Klassen die Methode checkTableVersion() aufrufen.
	 */
	public static void checkAllTableVersions() {
		setup();
		for (DataObject data : beans.values()) {
			data.checkTableVersion();
		}
	}

	/**
	 * Entfernt alle Beans vom Context
	 */
	public static void removeAll() {
		beans.clear();
	}

} // EndOfClass
