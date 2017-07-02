package com.itranswarp.warpdb.test;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class DecimalEntity extends BaseEntity {

	@Column(length = 100)
	public String name;

	@Column(scale = 4)
	public BigDecimal balance;

}
