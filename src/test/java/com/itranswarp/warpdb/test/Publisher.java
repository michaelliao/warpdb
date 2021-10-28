package com.itranswarp.warpdb.test;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "default")
public class Publisher {

	@Id
	public String id;

	public String name;
}
