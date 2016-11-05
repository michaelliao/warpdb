package com.itranswarp.warpdb.invalid.duptable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "HELLO")
public class DupTableNameEntity {

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
