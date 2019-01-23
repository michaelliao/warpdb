package com.itranswarp.warpdb.util;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.Entity;

import org.junit.Test;

public class ClassUtilsTest {

	@Test
	public void testScanInDir() throws Exception {
		List<Class<?>> list = ClassUtils.scanEntities("com.itranswarp.warpdb.test");
		assertEquals(6, list.size());
		for (Class<?> clazz : list) {
			assertNotNull(clazz.getAnnotation(Entity.class));
		}
	}

}
