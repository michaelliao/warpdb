package com.itranswarp.warpdb;

import org.springframework.dao.DataAccessException;

public class EntityNotReadyException extends DataAccessException {

	public final String name;

	public EntityNotReadyException(String name, String message) {
		super(message);
		this.name = name;
	}

	public EntityNotReadyException(Class<?> clazz) {
		this(clazz.getSimpleName(), "Entity not ready.");
	}

}
