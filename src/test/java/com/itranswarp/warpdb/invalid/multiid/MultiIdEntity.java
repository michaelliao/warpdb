package com.itranswarp.warpdb.invalid.multiid;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class MultiIdEntity {

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
