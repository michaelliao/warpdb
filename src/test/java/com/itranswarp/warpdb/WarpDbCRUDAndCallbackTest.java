package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.junit.Test;

import com.itranswarp.warpdb.test.User;

public class WarpDbCRUDAndCallbackTest extends WarpDbTestBase {

	@Test
	public void testInsert() throws Exception {
		User user = new User();
		user.name = "Michael";
		user.email = "michael@somewhere.org";
		warpdb.save(user);
		assertTrue(user.callbacks.contains(PrePersist.class));
		assertTrue(user.callbacks.contains(PostPersist.class));
		assertEquals("0001", user.id);
		assertEquals(user.createdAt, user.updatedAt);
		assertEquals(System.currentTimeMillis(), user.createdAt, 500);
	}

	@Test
	public void testUpdate() throws Exception {
		User user = new User();
		user.name = "Michael";
		user.email = "michael@somewhere.org";
		warpdb.save(user);
		Thread.sleep(100);
		user.name = "Changed";
		user.email = "changed@somewhere.org";
		warpdb.update(user);
		assertTrue(user.callbacks.contains(PreUpdate.class));
		assertTrue(user.callbacks.contains(PostUpdate.class));
		assertNotEquals(user.createdAt, user.updatedAt);
		assertEquals(System.currentTimeMillis(), user.updatedAt, 500);
		// fetch:
		User bak = warpdb.fetch(User.class, user.id);
		assertNotNull(bak);
		assertEquals(user.id, bak.id);
		assertEquals("Changed", bak.name);
		// email is set updatable=false:
		assertEquals("michael@somewhere.org", bak.email);
		assertEquals(user.createdAt, bak.createdAt);
		assertEquals(user.updatedAt, bak.updatedAt);
		assertEquals(user.version, bak.version);
	}

	@Test
	public void testLoad() throws Exception {
		String[] ids = { User.nextId(), User.nextId(), User.nextId(), User.nextId() };
		for (int i = 0; i < ids.length; i++) {
			User user = new User();
			user.id = ids[i];
			user.name = "Mr No." + i;
			user.email = "no." + i + "@somewhere.org";
			warpdb.save(user);
		}
		// test get & fetch:
		User u1 = warpdb.get(User.class, ids[0]);
		assertTrue(u1.callbacks.contains(PostLoad.class));
		User u2 = warpdb.fetch(User.class, ids[1]);
		assertTrue(u2.callbacks.contains(PostLoad.class));
		// test list:
		List<User> us = warpdb.list(User.class, "SELECT * FROM User where id>?", ids[1]);
		assertEquals(2, us.size());
		assertTrue(us.get(0).callbacks.contains(PostLoad.class));
		assertTrue(us.get(1).callbacks.contains(PostLoad.class));
		// test criteria:
		List<User> users = warpdb.from(User.class).where("id>?", ids[1]).list();
		assertEquals(2, users.size());
		assertTrue(users.get(0).callbacks.contains(PostLoad.class));
		assertTrue(users.get(1).callbacks.contains(PostLoad.class));
	}

	@Test
	public void testUpdateProperties() throws Exception {
		User user = new User();
		user.name = "Michael";
		user.email = "michael@somewhere.org";
		warpdb.save(user);
		Thread.sleep(100);
		user.name = "Changed";
		user.version = 99;
		warpdb.updateProperties(user, "name", "version", "updatedAt");
		assertTrue(user.callbacks.contains(PreUpdate.class));
		assertTrue(user.callbacks.contains(PostUpdate.class));
		assertNotEquals(user.createdAt, user.updatedAt);
		assertEquals(System.currentTimeMillis(), user.updatedAt, 500);
		// fetch:
		User bak = warpdb.fetch(User.class, user.id);
		assertNotNull(bak);
		assertEquals(user.id, bak.id);
		assertEquals("Changed", bak.name);
		assertEquals("Changed", bak.name);
		assertEquals(99, bak.version);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdatePropertiesFailedForNonUpdatable() throws Exception {
		User user = new User();
		user.name = "Michael";
		user.email = "michael@somewhere.org";
		warpdb.save(user);
		user.name = "Changed";
		user.version = 99;
		// createdAt is not updatable:
		warpdb.updateProperties(user, "name", "version", "createdAt");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdatePropertiesFailedForNonExist() throws Exception {
		User user = new User();
		user.name = "Michael";
		user.email = "michael@somewhere.org";
		warpdb.save(user);
		user.name = "Changed";
		user.version = 99;
		// role is not exist:
		warpdb.updateProperties(user, "name", "version", "role");
	}

	@Test
	public void testRemove() throws Exception {
		User user = new User();
		user.name = "Michael";
		user.email = "michael@somewhere.org";
		warpdb.save(user);
		warpdb.remove(user);
		assertTrue(user.callbacks.contains(PreRemove.class));
		assertTrue(user.callbacks.contains(PostRemove.class));
		assertNull(warpdb.fetch(User.class, user.id));
	}

}
