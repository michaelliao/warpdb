package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.warpdb.test.User;

public class WarpQueryNumberTest extends WarpDbTestBase {

	@Before
	public void prepareData() {
		for (int i = 0; i < 10; i++) {
			User u = new User();
			u.id = "A" + i;
			u.name = "Mr " + i;
			u.email = u.id.toLowerCase() + "@somewhere.org";
			u.age = 20 + i;
			u.score = 20 + i;
			// tag = "A" or "B":
			u.tag = i % 2 == 0 ? "A" : "B";
			warpdb.save(u);
		}
	}

	@Test
	public void testQueryForInt() throws Exception {
		OptionalInt n = warpdb.queryForInt("select count(id) from User");
		assertTrue(n.isPresent());
		assertEquals(10, n.getAsInt());
	}

	@Test
	public void testQueryForLong() throws Exception {
		OptionalLong n = warpdb.queryForLong("select count(id) from User");
		assertTrue(n.isPresent());
		assertEquals(10, n.getAsLong());
	}

	@Test
	public void testQueryForLongWithMax() throws Exception {
		OptionalLong n = warpdb.queryForLong("select max(age) from User");
		assertTrue(n.isPresent());
		assertEquals(29, n.getAsLong());
	}

	@Test
	public void testQueryForIntWithMin() throws Exception {
		OptionalInt n = warpdb.queryForInt("select min(age) from User");
		assertTrue(n.isPresent());
		assertEquals(20, n.getAsInt());
	}

	@Test
	public void testQueryForIntNotPresent() throws Exception {
		warpdb.updateSql("delete from User");
		OptionalInt n = warpdb.queryForInt("select min(age) from User");
		assertFalse(n.isPresent());
	}

	@Test
	public void testQueryForLongNotPresent() throws Exception {
		warpdb.updateSql("delete from User");
		OptionalLong n = warpdb.queryForLong("select max(age) from User");
		assertFalse(n.isPresent());
	}

	@Test
	public void testQueryForNumberNotPresent() throws Exception {
		warpdb.updateSql("delete from User");
		Optional<Number> n = warpdb.queryForNumber("select max(age) from User");
		assertFalse(n.isPresent());
	}

	@Test
	public void testQueryForNumberPresent() throws Exception {
		warpdb.updateSql("delete from User");
		Optional<Number> n = warpdb.queryForNumber("select count(id) from User");
		assertTrue(n.isPresent());
		assertEquals(0, n.get().intValue());
	}

	@Test
	public void testQueryForNumberByAvg() throws Exception {
		Optional<Number> n = warpdb.queryForNumber("select avg(score) from User where age > ?", 27);
		assertTrue(n.isPresent());
		assertEquals(28.5, n.get().doubleValue(), 0.001);
	}
}
