package com.itranswarp.warpdb.test;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AutoIncreamentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long id;

	public String name;

	public long createdAt;
}
