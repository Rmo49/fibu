package com.rmo.fibu.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.sql.Connection;

import com.rmo.fibu.exception.FibuRuntimeException;

/**
Basis-Klasse aller Model-Klassen.
Ist zuständig für den Bean-Support aller Model-Klassen.
 */
public abstract class DataModel implements BeanContextChild {
	// Bean-Support
	protected BeanContext mBeanContext;
	protected PropertyChangeSupport pcSupport;
	protected VetoableChangeSupport vcSupport;

	/**
	 * Model constructor comment.
	 */
	public DataModel() throws Exception {
		super();
		init();
	}

	/** add a change listener */
	public void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
		pcSupport.addPropertyChangeListener(name, pcl);
	}

	/** add a vetoable change listener */
	public void addVetoableChangeListener(String name, VetoableChangeListener vcl) {
	  vcSupport.addVetoableChangeListener(name, vcl);
	}

	/** Der BeanContext aller Model-Beans */
	public BeanContext getBeanContext() {
		return mBeanContext;
	}

	/** Die Connection zu der DB */
	protected Connection getConnection() throws FibuRuntimeException {
		return DbConnection.getConnection();
	}
	
	/** Setup the Conneciton.
	*/
	private void init() throws Exception {
		pcSupport = new PropertyChangeSupport(this);
		vcSupport = new VetoableChangeSupport(this);
	}

	/** To get Listeners of Context
	 */
	protected void initContextListener() {
	}

	/**
	 * remove a property change listener
	 */
	public void removePropertyChangeListener(String name, PropertyChangeListener pcl) {
		pcSupport.removePropertyChangeListener(name, pcl);
	}

	/**
	 * remove a vetoable change listener
	 */
	public void removeVetoableChangeListener(String name, VetoableChangeListener vcl) {
		vcSupport.removeVetoableChangeListener(name, vcl);
	}

	/** Sets the bean context for this Object
	 */
	public void setBeanContext(BeanContext bc) throws PropertyVetoException {
		mBeanContext = bc;
		pcSupport.firePropertyChange("beanContext", null, bc);
		initContextListener();
	}
}
