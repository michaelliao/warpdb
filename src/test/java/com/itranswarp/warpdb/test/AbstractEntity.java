package com.itranswarp.warpdb.test;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractEntity {

	@Id
	@Column(length = 50, nullable = false, updatable = false)
	public String id;

	public Long createdAt;

	@Column(nullable = false)
	public Long updatedAt;

}
