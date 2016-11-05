package com.itranswarp.warpdb.test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public abstract class AutoIncreamentIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long id;

	public Long createdAt;

	@Column(nullable = false)
	public Long updatedAt;

}
