package com.itranswarp.warpdb;

import org.springframework.dao.DataAccessException;

public class EntityConflictException extends DataAccessException {

	public final String name;

	public EntityConflictException(String name) {
		super("Entity conflict.");
		this.name = name;
	}

	public EntityConflictException(String name, String message) {
		super(message);
		this.name = name;
	}

	public EntityConflictException(Class<?> clazz) {
		this(clazz.getSimpleName());
	}

}
