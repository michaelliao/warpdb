package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import javax.persistence.PostPersist;
import javax.persistence.PrePersist;

import org.junit.Test;

import com.itranswarp.warpdb.test.AutoIncreamentEntity;
import com.itranswarp.warpdb.test.User;

public class WarpDbBatchSaveTest extends WarpDbTestBase {

	@Test
	public void testInsertBatchWithoutAutoId() throws Exception {
		User[] users = new User[27];
		for (int i = 0; i < users.length; i++) {
			User user = new User();
			user.name = "Name-" + i;
			user.email = "name" + i + "@somewhere.org";
			users[i] = user;
		}
		warpdb.save(users);
		for (int i = 0; i < users.length; i++) {
			User user = users[i];
			assertTrue(user.callbacks.contains(PrePersist.class));
			assertTrue(user.callbacks.contains(PostPersist.class));
			assertEquals(String.format("%04d", i + 1), user.id);
			assertEquals(user.createdAt, user.updatedAt);
			assertEquals(System.currentTimeMillis(), user.createdAt, 500);
		}
	}

	@Test
	public void testInsertBatchWithAutoId() throws Exception {
		AutoIncreamentEntity[] ais = new AutoIncreamentEntity[27];
		for (int i = 0; i < ais.length; i++) {
			AutoIncreamentEntity ai = new AutoIncreamentEntity();
			ai.name = "Name-" + i;
			ai.createdAt = System.currentTimeMillis();
			ais[i] = ai;
		}
		warpdb.save(ais);
		for (int i = 0; i < ais.length; i++) {
			AutoIncreamentEntity ai = ais[i];
			assertEquals(i + 1, ai.id);
			assertEquals(System.currentTimeMillis(), ai.createdAt, 500);
		}
	}

}
