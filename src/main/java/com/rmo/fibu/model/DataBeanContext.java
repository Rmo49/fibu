package com.rmo.fibu.model;

import java.beans.beancontext.BeanContextSupport;
import java.util.Iterator;

import com.rmo.fibu.util.Trace;

/** Der Context des Models. Enthält alle Model-Beans (Konto, Buchung etc.).
 */
public class DataBeanContext extends BeanContextSupport
{
private static final long serialVersionUID = -7328602077981751509L;
private static DataBeanContext sDataBeanContext = null;

/**
 * DataBeanContext constructor comment.
 */
private DataBeanContext() {
	super();
	setup();
}

/** Gibt den Context, der alle Beans enthält zurück.
 * @return com.rmo.fibu.model.DataBeanContext
 */
public static DataBeanContext getContext() {
	if (sDataBeanContext == null) {
		sDataBeanContext = new DataBeanContext();
	}
	return sDataBeanContext;
}

/** Gibt ein DataObjekt der gesuchten Klasse zurück
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

/** Gibt ein DataObjekt der gesuchten Klasse zurück.
	Ist nur hier für Testzwecke, da ein anderer Parameter verwendet wird.
 */
public Object getDataObject(Object pObject) {
	Object [] lBeans = toArray();
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
		DataBase base = (DataBase) lIter.next();
		base.checkTableVersion();
	}
}

/** Setup the Context
 */
private void setup() {
	try {
		add(new BuchungData());
		add(new KontoData());
//		add(new KontoDataFrom());
		add(new CsvBankData());
//		add(new CsvBankDataFrom());
		add(new CsvKeyKontoData());
//		add(new CsvKeywordDataFrom());

		//addBeanContextServicesListener(mKontoM);
		// FibuDaten zuletzt, da Konto und Buchung einen Link darauf haben
		add(FibuData.getFibuData());
//		add(new FibuDataFrom());
	}
	catch (Exception e) {
		Trace.println(1, "Error in DataBeanContext.setup(): " + e.getMessage());
	}
}

/** Entfernt alle Beans vom Context
 */
public static void removeAll() {
	try {
		//removeAll();
		sDataBeanContext = null;
	}
	catch (Exception e) {
		Trace.println(1, "Error in DataBeanContext.removeAll(): " + e.getMessage());
	}
}

} //EndOfClass
