package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import org.junit.Test;

import com.itranswarp.warpdb.test.AutoIncreamentEntity;

public class WarpDbIdentityIdTest extends WarpDbTestBase {

	@Test
	public void testInsertIdentityId() throws Exception {
		AutoIncreamentEntity[] es = new AutoIncreamentEntity[10];
		for (int i = 0; i < es.length; i++) {
			es[i] = new AutoIncreamentEntity();
			es[i].name = "Mr No." + i;
			es[i].createdAt = 12300000 + i;
		}
		int n = 0;
		for (AutoIncreamentEntity e : es) {
			warpdb.insert(e);
			n++;
			assertEquals(n, e.id);
		}
	}

}
