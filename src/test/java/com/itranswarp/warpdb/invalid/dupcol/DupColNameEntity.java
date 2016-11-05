package com.itranswarp.warpdb.invalid.dupcol;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class DupColNameEntity {

	@Id
	public long id;

	@Column(length = 100, nullable = false, updatable = false)
	public String email;

	@Column(name = "Email", length = 100, nullable = false, updatable = false)
	public String mailAddress;

	@Version
	@Column(nullable = false)
	public long version;

}
