package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.itranswarp.warpdb.test.User;

public class WarpDbBatchUpdateTest extends WarpDbTestBase {

	@Test
	public void testBatchUpdate() throws Exception {
		// insert batch with id=0~26:
		User[] users = new User[27];
		for (int i = 0; i < users.length; i++) {
			User user = new User();
			user.name = "Name-" + i;
			user.email = "name" + i + "@somewhere.org";
			users[i] = user;
		}
		warpdb.save(Arrays.asList(users));
		long ts = System.currentTimeMillis();
		for (int i = 0; i < users.length; i++) {
			User user = users[i];
			assertEquals(String.format("%04d", i + 1), user.id);
			assertEquals(user.createdAt, user.updatedAt);
			assertEquals(ts, user.createdAt, 500.0);
		}
		Thread.sleep(600);
		// update:
		for (int i = 0; i < users.length; i++) {
			User user = users[i];
			user.name = "Updated-" + i;
			user.email = "updated" + i + "@new.org";
		}
		warpdb.update(Arrays.asList(users));
		ts = System.currentTimeMillis();
		// check:
		List<User> us = warpdb.from(User.class).orderBy("id").list();
		for (int i = 0; i < us.size(); i++) {
			User user = us.get(i);
			assertEquals(String.format("%04d", i + 1), user.id);
			assertEquals("Updated-" + i, user.name);
			assertEquals("name" + i + "@somewhere.org", user.email); // not updated
			assertNotEquals(user.createdAt, user.updatedAt);
			assertEquals(ts, user.updatedAt, 500.0);
			assertNotEquals(ts, user.createdAt, 500.0);
		}
	}

}
