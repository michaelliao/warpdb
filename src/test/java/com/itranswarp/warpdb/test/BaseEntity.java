package com.itranswarp.warpdb.test;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
public abstract class BaseEntity {

	static int nextId = 0;

	public static String nextId() {
		nextId++;
		return String.format("%04d", nextId);
	}

	public static void resetId() {
		nextId = 0;
	}

	@Id
	@Column(length = 50, nullable = false, updatable = false)
	public String id;

	@Column(nullable = false, updatable = false)
	public long createdAt;

	@Column(nullable = false)
	public long updatedAt;

	@Version
	@Column(nullable = false)
	public long version;

}
