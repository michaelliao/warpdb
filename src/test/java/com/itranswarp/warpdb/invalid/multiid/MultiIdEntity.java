package com.itranswarp.warpdb.invalid.multiid;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class MultiIdEntity {

	@Id
	public long id;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long sid;

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
