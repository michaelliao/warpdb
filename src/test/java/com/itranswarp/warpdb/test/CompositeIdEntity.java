package com.itranswarp.warpdb.test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

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
