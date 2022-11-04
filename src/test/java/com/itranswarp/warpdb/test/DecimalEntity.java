package com.itranswarp.warpdb.test;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class DecimalEntity extends BaseEntity {

	@Column(length = 100)
	public String name;

	@Column(scale = 4)
	public BigDecimal balance;

}
