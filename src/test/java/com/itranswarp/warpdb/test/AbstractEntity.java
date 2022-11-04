package com.itranswarp.warpdb.test;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractEntity {

	@Id
	@Column(length = 50, nullable = false)
	public String id;

	public Long createdAt;

	@Column(nullable = false)
	public Long updatedAt;

}
