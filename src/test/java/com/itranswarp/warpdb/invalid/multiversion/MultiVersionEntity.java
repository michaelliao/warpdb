package com.itranswarp.warpdb.invalid.multiversion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class MultiVersionEntity {

	@Id
	public long id;

	@Column(length = 100, nullable = false, updatable = false)
	public String name;

	public long balance;

	@Version
	@Column(nullable = false)
	public long version;

	@Version
	@Column(nullable = false)
	public long optLock;

}
