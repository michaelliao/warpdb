package com.itranswarp.warpdb;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.itranswarp.warpdb.test.DecimalEntity;

public class WarpDbDecimalTest extends WarpDbTestBase {

    @Test
    public void testDecimal() throws Exception {
        DecimalEntity e = new DecimalEntity();
        e.id = DecimalEntity.nextId();
        e.name = "Bob";
        e.balance = new BigDecimal("12.345");
        warpdb.insert(e);
        // query:
        DecimalEntity bak = warpdb.fetch(DecimalEntity.class, e.id);
        assertNotNull(bak);
        // NOTE: compare BigDecimal by compareTo rather than equals:
        assertNotNull(bak.balance);
        assertTrue(e.balance.compareTo(bak.balance) == 0);
    }

    @Test
    public void testConvertNull() throws Exception {
        DecimalEntity e = new DecimalEntity();
        e.id = DecimalEntity.nextId();
        e.name = "Bob";
        e.balance = null;
        warpdb.insert(e);
        // query:
        DecimalEntity bak = warpdb.fetch(DecimalEntity.class, e.id);
        assertNotNull(bak);
        assertNull(bak.balance);
    }
}
