package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Test;

import com.itranswarp.warpdb.test.Address;
import com.itranswarp.warpdb.test.TodoEntity;

public class WarpDbConverterTest extends WarpDbTestBase {

	@Test
	public void testInsert() throws Exception {
		TodoEntity todo = new TodoEntity();
		todo.id = TodoEntity.nextId();
		todo.name = "Unit Test";
		todo.targetDate = LocalDate.of(2016, 10, 20);
		todo.targetDateTime = LocalDateTime.of(2016, 10, 20, 11, 12, 13);
		todo.address = new Address("Beijing", "No.1 Road", "100101");
		warpdb.save(todo);
	}

}
