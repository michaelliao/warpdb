package com.itranswarp.warpdb;

import javax.persistence.PersistenceException;

/**
 * Thrown when invalid configuration found.
 * 
 * @author liaoxuefeng
 */
@SuppressWarnings("serial")
public class ConfigurationException extends PersistenceException {

	public ConfigurationException() {
		super();
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

}
