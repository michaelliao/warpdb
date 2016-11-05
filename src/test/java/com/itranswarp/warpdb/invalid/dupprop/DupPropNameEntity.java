package com.itranswarp.warpdb.invalid.dupprop;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class DupPropNameEntity {

	@Id
	public long id;

	@Column(length = 100, nullable = false, updatable = false)
	public String name;

	public long balance;

	@Column(length = 10)
	public String getName() {
		return "bob";
	}

	public void setName(String value) {
	}

	@Version
	@Column(nullable = false)
	public long version;

}
