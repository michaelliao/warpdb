package com.itranswarp.warpdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.itranswarp.warpdb.test.CompositeIdEntity;

public class WarpDbCompositeIdTest extends WarpDbTestBase {

    @BeforeEach
    public void prepareData() {
        for (int i = 0; i < 10; i++) {
            CompositeIdEntity c = new CompositeIdEntity();
            c.uid = "u" + i;
            c.sid = "s" + i;
            c.name = "Mr " + i;
            c.balance = 1000 + i;
            warpdb.insert(c);
        }
    }

    @Test
    public void testQuery() throws Exception {
        CompositeIdEntity c = warpdb.get(CompositeIdEntity.class, "s1", "u1");
        assertEquals("Mr 1", c.name);
        assertNull(warpdb.fetch(CompositeIdEntity.class, "b", "a"));
    }

    @Test
    public void testQueryInvalid() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            warpdb.get(CompositeIdEntity.class, "u1");
        });
    }

    @Test
    public void testInsertAndDelete() throws Exception {
        CompositeIdEntity c1 = new CompositeIdEntity();
        c1.uid = "ab";
        c1.sid = "cde";
        c1.name = "abcde";
        c1.balance = 0;
        CompositeIdEntity c2 = new CompositeIdEntity();
        c2.uid = "abc";
        c2.sid = "de";
        c2.name = "abcde";
        c2.balance = 0;
        warpdb.insert(c1);
        warpdb.insert(c2);
        OptionalInt n = warpdb.queryForInt("select count(*) from compositeIdEntity");
        assertTrue(n.isPresent());
        assertEquals(12, n.getAsInt());
        // delete:
        warpdb.remove(c2);
        // query:
        n = warpdb.queryForInt("select count(*) from compositeIdEntity");
        assertEquals(11, n.getAsInt());
    }

    @Test
    public void testUpdate() throws Exception {
        CompositeIdEntity c = new CompositeIdEntity();
        c.uid = "u0";
        c.sid = "s0";
        c.name = "Updated";
        c.balance = 2000;
        warpdb.update(c);
        // query:
        c = warpdb.get(CompositeIdEntity.class, "s0", "u0");
        assertEquals("Updated", c.name);
        assertEquals(2000, c.balance);
    }
}
