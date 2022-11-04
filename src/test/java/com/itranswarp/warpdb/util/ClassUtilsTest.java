package com.itranswarp.warpdb.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import jakarta.persistence.Entity;

import org.junit.jupiter.api.Test;

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
