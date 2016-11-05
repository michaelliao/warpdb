package com.itranswarp.warpdb.invalid.multiupdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PreUpdate;
import javax.persistence.Version;

@Entity
public class MultiPreUpdateEntity {

	@Id
	public long id;

	@Column(length = 100, nullable = false, updatable = false)
	public String name;

	public long balance;

	public long updatedAt;

	@Version
	@Column(nullable = false)
	public long version;

	@PreUpdate
	public void beforeUpdate() {
		this.version++;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = System.currentTimeMillis();
	}
}
