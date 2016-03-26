package com.itranswarp.warpdb;

import org.springframework.dao.DataAccessException;

public class EntityNotFoundException extends DataAccessException {

	public final String name;

	public EntityNotFoundException(String name, String message) {
		super(message);
		this.name = name;
	}

	public EntityNotFoundException(Class<?> clazz, String message) {
		this(clazz.getSimpleName(), message);
	}

	public EntityNotFoundException(Class<?> clazz) {
		this(clazz.getSimpleName(), "Entity not found.");
	}

}
