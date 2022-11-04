package com.itranswarp.warpdb.invalid.dupprop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

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
