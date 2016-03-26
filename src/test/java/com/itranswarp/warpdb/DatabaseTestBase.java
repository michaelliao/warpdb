package com.itranswarp.warpdb;

import java.util.Arrays;

import org.junit.Before;

public class DatabaseTestBase {

	protected Database database = null;

	@Before
	public void setUpDatabase() {
		database = new Database(new SqlObjectConverters(),
				Arrays.asList("com.itranswarp.warpdb.entity", "com.itranswarp.warpdb.test"));
		database.jdbcTemplate = JdbcTemplateHsqldbFactory.createJdbcTemplate();
	}

}
