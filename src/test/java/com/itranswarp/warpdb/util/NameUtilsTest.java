package com.itranswarp.warpdb.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class NameUtilsTest {

	@Test
	public void testToCamelCaseName() {
		assertEquals("hello", NameUtils.toCamelCaseName("Hello"));
		assertEquals("hello", NameUtils.toCamelCaseName("hello"));
		assertEquals("helloWorld", NameUtils.toCamelCaseName("helloWorld"));
		assertEquals("helloWorld", NameUtils.toCamelCaseName("HelloWorld"));
		assertEquals("helloWorldWide", NameUtils.toCamelCaseName("HelloWorldWide"));
		assertEquals("helloWD", NameUtils.toCamelCaseName("HelloWD"));
	}

	@Test
	public void testToUnderscoreName() {
		assertEquals("hello", NameUtils.toUnderscoreName("hello"));
		assertEquals("hello", NameUtils.toUnderscoreName("Hello"));
		assertEquals("hello_world", NameUtils.toUnderscoreName("helloWorld"));
		assertEquals("hello_world", NameUtils.toUnderscoreName("HelloWorld"));
		assertEquals("hello_world_wide", NameUtils.toUnderscoreName("HelloWorldWide"));
	}

}
