package com.rmo.fibu.model.from;

import java.sql.Connection;

import com.rmo.fibu.exception.FibuRuntimeException;
import com.rmo.fibu.model.DataModel;

/**
Erweiterung des Data-Models auf die Connection2
 */
public abstract class DataModelFrom extends DataModel {

	/**
	 * Model constructor comment.
	 */
	public DataModelFrom() throws Exception {
		super();
	}

	/** Die Connection zu der DB */
	protected Connection getConnection() throws FibuRuntimeException {
		return DbConnectionFrom.getConnection();
	}

}
