package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import org.junit.Test;

import com.itranswarp.warpdb.context.UserContext;
import com.itranswarp.warpdb.test.User;

public class DatabaseTest extends DatabaseTestBase {

	@Test
	public void testGet() {
		final String USER_ID = IdUtil.next();
		User user = new User();
		user.id = USER_ID;
		user.name = "Test";
		user.email = "test@example.com";
		user.gender = "male";
		user.imageUrl = "http://test";
		try (UserContext<User> context = new UserContext<User>(user)) {
			database.save(user);
		}
		// get by ID:
		User u1 = database.get(User.class, USER_ID);
		assertEquals("Test", u1.name);
		// fetch by ID:
		User u2 = database.fetch(User.class, USER_ID);
		assertEquals("Test", u2.name);
		// fetch by non-exist ID:
		User u3 = database.fetch(User.class, IdUtil.next());
		assertNull(u3);
	}

	@Test
	public void testFetch() {
		final String USER_ID = IdUtil.next();
		User user = new User();
		user.id = USER_ID;
		user.name = "Test";
		user.email = "test@example.com";
		user.gender = "male";
		user.imageUrl = "http://test";
		try (UserContext<User> context = new UserContext<User>(user)) {
			database.save(user);
		}
		// get by ID:
		User u1 = database.get(User.class, USER_ID);
		assertEquals("Test", u1.name);
		// fetch by ID:
		User u2 = database.fetch(User.class, USER_ID);
		assertEquals("Test", u2.name);
		// fetch by non-exist ID:
		User u3 = database.fetch(User.class, IdUtil.next());
		assertNull(u3);
	}

}
