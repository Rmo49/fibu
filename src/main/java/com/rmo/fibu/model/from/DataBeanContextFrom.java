package com.rmo.fibu.model.from;

import java.util.HashMap;
import java.util.Map;

import com.rmo.fibu.util.Trace;

/**
 * Der Context des Models. Enthält alle Model-Beans (Konto, Buchung etc.).
 * für das Kopieren
 * Ist ein Singelton
 */
public class DataBeanContextFrom   {

	private static DataBeanContextFrom sDataBeanContext = null;

	/**
	 * Die Map mit allen Klassen
	 */
	private final Map<Class<?>, DataObjectFrom> beans = new HashMap<>();

	/**
	 * DataBeanContext constructor comment.
	 */
	private DataBeanContextFrom() {
		setup();
	}

	/**
	 * Gibt den Context, der alle Beans enthält zurück.
	 *
	 * @return com.rmo.fibu.model.DataBeanContext
	 */
	public static DataBeanContextFrom getContext() {
		if (sDataBeanContext == null) {
			sDataBeanContext = new DataBeanContextFrom();
		}
		return sDataBeanContext;
	}

	/**
	 * Ein bean der Liste dazufügen.
	 * @param <T>
	 * @param type
	 * @param instance
	 */
	public <T> void addBean(Class<T> type, DataObjectFrom instance) {
	    beans.put(type, instance);
	}

	/**
	 * Gibt das Bean der gesuchten Klasse zurück
	 * @param <T>
	 * @param type
	 * @return
	 */
	public <T> T getDataBean(Class<T> type) {
	    return type.cast(beans.get(type));
	}

	/**
	 * In alle Data Klassen die Methode checkTableVersion() aufrufen.
	 */
	public void checkAllTableVersions() {
		for (DataObjectFrom data : beans.values()) {
			data.checkTableVersion();
		}
	}

	/**
	 * Setup the Context
	 */
	private void setup() {
		try {
			addBean(KontoDataFrom.class, new KontoDataFrom());
			addBean(CsvBankDataFrom.class,  new CsvBankDataFrom());
			addBean(CsvKeywordDataFrom.class, new CsvKeywordDataFrom());
			addBean(FibuDataFrom.class, new FibuDataFrom());

		} catch (Exception e) {
			Trace.println(1, "Error in DataBeanContext.setup(): " + e.getMessage());
		}
	}

	/**
	 * Entfernt alle Beans vom Context
	 */
	public static void removeAll() {
		try {
			// removeAll();
			sDataBeanContext = null;
		} catch (Exception e) {
			Trace.println(1, "Error in DataBeanContext.removeAll(): " + e.getMessage());
		}
	}

} // EndOfClass
