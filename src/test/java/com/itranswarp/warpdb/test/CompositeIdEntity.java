package com.itranswarp.warpdb.test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CompositeIdEntity {

	@Id
	@Column(length = 50, nullable = false, updatable = false)
	public String uid;

	@Id
	@Column(length = 50, nullable = false, updatable = false)
	public String sid;

	@Column(nullable = false, length = 100)
	public String name;

	@Column(nullable = false)
	public long balance;

}
