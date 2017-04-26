package com.itranswarp.warpdb.test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class AutoIncreamentEntity {

	@Id
	@GeneratedValue
	public long id;

	public String name;

	public long createdAt;
}
