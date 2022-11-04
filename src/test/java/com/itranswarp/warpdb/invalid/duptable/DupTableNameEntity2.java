package com.itranswarp.warpdb.invalid.duptable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "hello")
public class DupTableNameEntity2 {

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
