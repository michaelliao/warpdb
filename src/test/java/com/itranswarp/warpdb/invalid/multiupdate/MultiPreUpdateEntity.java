package com.itranswarp.warpdb.invalid.multiupdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;

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
