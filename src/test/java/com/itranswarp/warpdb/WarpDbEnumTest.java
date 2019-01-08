package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import org.junit.Test;

import com.itranswarp.warpdb.test.EnumEntity;
import com.itranswarp.warpdb.test.Role;

public class WarpDbEnumTest extends WarpDbTestBase {

	@Test
	public void testEnum() throws Exception {
		EnumEntity ee = new EnumEntity();
		ee.id = EnumEntity.nextId();
		ee.name = "Bob";
		ee.roleName = Role.ADMIN;
		warpdb.insert(ee);
		// query:
		EnumEntity bak = warpdb.fetch(EnumEntity.class, ee.id);
		assertNotNull(bak);
		assertEquals(Role.ADMIN, bak.roleName);
	}

	@Test
	public void testConvertNull() throws Exception {
		EnumEntity ee = new EnumEntity();
		ee.id = EnumEntity.nextId();
		ee.name = "Bob";
		ee.roleName = null;
		warpdb.insert(ee);
		// query:
		EnumEntity bak = warpdb.fetch(EnumEntity.class, ee.id);
		assertNotNull(bak);
		assertNull(bak.roleName);
	}

	@Test
	public void testConvertWhere() throws Exception {
		EnumEntity ee = new EnumEntity();
		ee.id = EnumEntity.nextId();
		ee.name = "Bob";
		ee.roleName = Role.ADMIN;
		warpdb.insert(ee);
		// query:
		EnumEntity bak = warpdb.from(EnumEntity.class).where("roleName = ?", Role.ADMIN).first();
		assertNotNull(bak);
		assertEquals(Role.ADMIN, bak.roleName);
	}
}
