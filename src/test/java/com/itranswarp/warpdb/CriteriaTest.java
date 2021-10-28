package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.warpdb.test.User;

public class CriteriaTest {

	WarpDb warpdb;

	@Before
	public void setUp() throws Exception {
		warpdb = new WarpDb();
		warpdb.basePackages = Arrays.asList("com.itranswarp.warpdb.test");
		warpdb.init();
	}

	@Test
	public void testSelect() {
		assertEquals("SELECT * FROM user", warpdb.from(User.class).sql());
		assertEquals("SELECT * FROM user", warpdb.select().from(User.class).sql());
		assertEquals("SELECT * FROM user ORDER BY name", warpdb.select().from(User.class).orderBy("name").sql());
		assertEquals("SELECT DISTINCT * FROM user", warpdb.select().distinct().from(User.class).sql());
		assertEquals("SELECT DISTINCT name FROM user", warpdb.select("name").distinct().from(User.class).sql());
		assertEquals("SELECT email, name FROM user", warpdb.select("email", "name").from(User.class).sql());
		assertEquals("SELECT * FROM user LIMIT ?, ?", warpdb.select().from(User.class).limit(100).sql());
		assertEquals("SELECT * FROM user LIMIT ?, ?", warpdb.select().from(User.class).limit(10, 100).sql());
		assertEquals("SELECT * FROM user WHERE name = ?",
				warpdb.select().from(User.class).where("name = ?", "Bob").sql());
		assertEquals("SELECT * FROM user WHERE name = ? AND the_tag > ?",
				warpdb.select().from(User.class).where("name = ?", "Bob").and("tag > ?", 10).sql());
		assertEquals("SELECT * FROM user WHERE name = ? ORDER BY name",
				warpdb.select().from(User.class).where("name = ?", "Bob").orderBy("name").sql());
		assertEquals("SELECT * FROM user WHERE name = ? ORDER BY name, the_tag DESC",
				warpdb.select().from(User.class).where("name = ?", "Bob").orderBy("name").orderBy("tag desc").sql());
		assertEquals("SELECT * FROM user WHERE name = ? ORDER BY name DESC, the_tag ASC", warpdb.select()
				.from(User.class).where("name = ?", "Bob").orderBy("name").desc().orderBy("tag asc").sql());
	}

	@Test
	public void testSelectForUpdate() {
		assertEquals("SELECT * FROM user FOR UPDATE", warpdb.selectForUpdate().from(User.class).sql());
		assertEquals("SELECT * FROM user ORDER BY name FOR UPDATE",
				warpdb.selectForUpdate().from(User.class).orderBy("name").sql());
		assertEquals("SELECT email, name FROM user FOR UPDATE",
				warpdb.selectForUpdate("email", "name").from(User.class).sql());
		assertEquals("SELECT * FROM user LIMIT ?, ? FOR UPDATE",
				warpdb.selectForUpdate().from(User.class).limit(100).sql());
		assertEquals("SELECT * FROM user WHERE name = ? FOR UPDATE",
				warpdb.selectForUpdate().from(User.class).where("name = ?", "Bob").sql());
		assertEquals("SELECT * FROM user WHERE name = ? AND the_tag > ? FOR UPDATE",
				warpdb.selectForUpdate().from(User.class).where("name = ?", "Bob").and("tag > ?", 10).sql());
	}

	@Test
	public void testAggregate() {
		assertEquals("SELECT COUNT(*) FROM user", warpdb.from(User.class).sql("COUNT(*)"));
		assertEquals("SELECT COUNT(*) FROM user WHERE name > ?",
				warpdb.from(User.class).where("name > ?", "Bob").sql("COUNT(*)"));
		assertEquals("SELECT COUNT(*) FROM user WHERE name > ?",
				warpdb.select("name", "age").from(User.class).where("name > ?", "Bob").sql("COUNT(*)"));
		assertEquals("SELECT COUNT(*) FROM user WHERE name > ?",
				warpdb.select("name", "age").from(User.class).where("name > ?", "Bob").orderBy("name").sql("COUNT(*)"));
		assertEquals("SELECT COUNT(*) FROM user WHERE name > ?",
				warpdb.select("name", "age").from(User.class).where("name > ?", "Bob").limit(10, 100).sql("COUNT(*)"));
	}
}
