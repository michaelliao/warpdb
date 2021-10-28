package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.warpdb.schematest.Publisher;

public class CriteriaWithSchemaTest {

	WarpDb warpdb;

	@Before
	public void setUp() throws Exception {
		warpdb = new WarpDb();
		warpdb.basePackages = Arrays.asList("com.itranswarp.warpdb.schematest");
		warpdb.init();
	}

	@Test
	public void testWithSchema() {
		assertEquals("SELECT * FROM default.publisher", warpdb.from(Publisher.class).sql());
	}

	@Test
	public void testSelectForUpdate() {
		assertEquals("SELECT * FROM default.publisher FOR UPDATE",
				warpdb.selectForUpdate().from(Publisher.class).sql());
		assertEquals("SELECT * FROM default.publisher ORDER BY name FOR UPDATE",
				warpdb.selectForUpdate().from(Publisher.class).orderBy("name").sql());
	}

	@Test
	public void testAggregate() {
		assertEquals("SELECT COUNT(*) FROM default.publisher", warpdb.from(Publisher.class).sql("COUNT(*)"));
		assertEquals("SELECT COUNT(*) FROM default.publisher WHERE name > ?",
				warpdb.from(Publisher.class).where("name > ?", "Bob").sql("COUNT(*)"));
	}
}
