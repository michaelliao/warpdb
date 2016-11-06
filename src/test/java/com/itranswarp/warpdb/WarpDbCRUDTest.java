package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.junit.Test;

import com.itranswarp.warpdb.test.User;

public class WarpDbCRUDTest extends WarpDbTestBase {

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
