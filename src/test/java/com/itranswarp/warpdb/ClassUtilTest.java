package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.itranswarp.warpdb.context.UserContext;

public class ClassUtilTest {

	@Test
	public void testScanInDir() throws Exception {
		ClassUtil cs = new ClassUtil();
		List<Class<?>> list = cs.scan("com.itranswarp.warpdb.context", c -> {
			return c.getSimpleName().equals("UserContext");
		});
		assertEquals(1, list.size());
		assertEquals(UserContext.class, list.get(0));
	}

	@Test
	public void testScanInJar() {
		ClassUtil cs = new ClassUtil();
		List<Class<?>> list = cs.scan("org.apache.commons", c -> {
			return c.getName().startsWith("org.apache.commons.logging.Log");
		});
		assertTrue(list.size() >= 3);
		assertTrue(list.contains(Log.class));
		assertTrue(list.contains(LogConfigurationException.class));
		assertTrue(list.contains(LogFactory.class));
	}

}
