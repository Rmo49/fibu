package com.rmo.fibu.model;

import java.beans.beancontext.BeanContextSupport;
import java.util.Iterator;

import com.rmo.fibu.model.from.CsvCompanyDataFrom;
import com.rmo.fibu.model.from.CsvKeywordDataFrom;
import com.rmo.fibu.model.from.FibuDataFrom;
import com.rmo.fibu.model.from.KontoDataFrom;
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
	for (int i=0; i<lBeans.length; i++) {
		if (lBeans[i].getClass().equals(pObject)) {
			return lBeans[i];
		}
	}
	return null;
}

/** Setup the Context
 */
private void setup() {
	try {
		add(new BuchungData());
		add(new KontoData());
		add(new KontoDataFrom());
		add(new CsvCompanyData());
		add(new CsvCompanyDataFrom());
		add(new CsvKeywordData());
		add(new CsvKeywordDataFrom());

		//addBeanContextServicesListener(mKontoM);
		// FibuDaten zuletzt, da Konto und Buchung einen Link darauf haben
		add(FibuData.getFibuData());
		add(new FibuDataFrom());
	}
	catch (Exception e) {
		Trace.println(1, "Fehler in DataBeanContext.setup(): " + e.getMessage());
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
		Trace.println(1, "Fehler in DataBeanContext.removeAll(): " + e.getMessage());
	}
}

} //EndOfClass
