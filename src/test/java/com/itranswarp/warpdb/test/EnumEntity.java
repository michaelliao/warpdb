package com.itranswarp.warpdb.test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class EnumEntity extends BaseEntity {

	@Column(length = 100)
	public String name;

	public Role roleName;

}
