package com.itranswarp.warpdb;

import java.util.Arrays;

import org.junit.Before;

public class DatabaseTestBase {

	protected Database database = null;

	@Before
	public void setUpDatabase() {
		database = new Database();
		database.setBasePackages(Arrays.asList("com.itranswarp.warpdb.entity", "com.itranswarp.warpdb.test"));
		database.setJdbcTemplate(JdbcTemplateHsqldbFactory.createJdbcTemplate());
		database.init();
	}

}
