package com.itranswarp.warpdb.test;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class EnumEntity extends BaseEntity {

	@Column(length = 100)
	public String name;

	public Role roleName;

}
