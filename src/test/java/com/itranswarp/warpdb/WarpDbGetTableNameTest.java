package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import org.junit.Test;

import com.itranswarp.warpdb.test.TodoEntity;
import com.itranswarp.warpdb.test.User;

public class WarpDbGetTableNameTest extends WarpDbTestBase {

	@Test
	public void testGetTableNameByUser() throws Exception {
		String t = warpdb.getTable(User.class);
		assertEquals("user", t);
	}

	@Test
	public void testGetTableNameByTodo() throws Exception {
		String t = warpdb.getTable(TodoEntity.class);
		assertEquals("todos", t);
	}

}
