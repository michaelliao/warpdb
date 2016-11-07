package com.itranswarp.warpdb.util;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.itranswarp.warpdb.converter.EnumToStringConverter;

public class ClassUtilsTest {

	@Test
	public void testScanInDir() throws Exception {
		List<Class<?>> list = ClassUtils.scan("com.itranswarp.warpdb.converter", c -> {
			return c.getSimpleName().equals("EnumToStringConverter");
		});
		assertEquals(1, list.size());
		assertEquals(EnumToStringConverter.class, list.get(0));
	}

	@Test
	public void testScanInJar() {
		List<Class<?>> list = ClassUtils.scan("org.apache.commons", c -> {
			return c.getName().startsWith("org.apache.commons.logging.Log");
		});
		assertTrue(list.size() >= 3);
		assertTrue(list.contains(Log.class));
		assertTrue(list.contains(LogConfigurationException.class));
		assertTrue(list.contains(LogFactory.class));
	}

}
