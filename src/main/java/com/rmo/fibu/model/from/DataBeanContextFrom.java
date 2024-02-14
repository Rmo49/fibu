package com.rmo.fibu.model.from;

import java.beans.beancontext.BeanContextSupport;
import java.util.Iterator;

import com.rmo.fibu.util.Trace;

public class DataBeanContextFrom extends BeanContextSupport {

	private static final long serialVersionUID = 1633487783198824158L;

	private static DataBeanContextFrom sDataBeanContext = null;

	/**
	 * DataBeanContext constructor comment.
	 */
	private DataBeanContextFrom() {
		super();
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
	 * Gibt ein DataObjekt der gesuchten Klasse zurück
	 */
	public Object getDataBean(Class<?> pClass) {
		Iterator<?> lIter = iterator();
		while (lIter.hasNext()) {
			Object lObj = lIter.next();
			// rmo: kann so verglichen werden?
			if (lObj.getClass().equals(pClass)) {
				return lObj;
			}
		}
		return null;
	}

	/**
	 * Gibt ein DataObjekt der gesuchten Klasse zurück. Ist nur hier für Testzwecke,
	 * da ein anderer Parameter verwendet wird.
	 */
	public Object getDataObject(Object pObject) {
		Object[] lBeans = toArray();
		for (Object lBean : lBeans) {
			if (lBean.getClass().equals(pObject)) {
				return lBean;
			}
		}
		return null;
	}

	/**
	 * In alle Data Klassen die Methode checkTableVersion() aufrufen.
	 */
	public void checkAllTableVersions() {
		Iterator<?> lIter = iterator();
		while (lIter.hasNext()) {
			DataBaseFrom base = (DataBaseFrom) lIter.next();
			base.checkTableVersion();
		}
	}

	/**
	 * Setup the Context
	 */
	private void setup() {
		try {
			add(new KontoDataFrom());
			add(new CsvBankDataFrom());
			add(new CsvKeywordDataFrom());
			add(new FibuDataFrom());
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
