package com.itranswarp.warpdb.test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.itranswarp.warpdb.context.DbUser;
import com.itranswarp.warpdb.entity.BaseEntity;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UK_User_email", columnNames = { "email" }))
public class User extends BaseEntity implements DbUser {

	@Column(length = VARCHAR_100, nullable = false, updatable = false)
	public String name;

	@Column(length = VARCHAR_100, nullable = false, updatable = false)
	public String email;

	@Column(nullable = false)
	public boolean verified;

	@Column(length = ENUM, nullable = false, updatable = false)
	public String gender;

	@Column(nullable = false)
	public long balance;

	@Column(length = VARCHAR_1000, nullable = false)
	public String imageUrl;

	@Override
	public String getId() {
		return id;
	}

}
