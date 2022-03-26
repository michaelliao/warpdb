package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.warpdb.test.User;

public class WarpDbQueryTest extends WarpDbTestBase {

    @Before
    public void prepareData() {
        String[] ids = { "A0", "A1", "A2", "A3", "A4", "B0", "B1", "B2", "B3", "B4" };
        for (String id : ids) {
            User u = new User();
            u.id = id;
            u.name = "Mr " + id;
            u.email = id.toLowerCase() + "@somewhere.org";
            // tag = "A" or "B":
            u.tag = id.substring(0, 1);
            warpdb.insert(u);
        }
    }

    @Test
    public void testQuery1() throws Exception {
        List<User> list1 = warpdb.from(User.class).list();
        assertEquals(10, list1.size());
        assertEquals("A0", list1.get(0).id);
        assertEquals("B4", list1.get(9).id);
        List<User> list2 = warpdb.from(User.class).limit(5).list();
        assertEquals(5, list2.size());
        assertEquals("A0", list2.get(0).id);
        assertEquals("A4", list2.get(4).id);
        List<User> list3 = warpdb.from(User.class).limit(5, 3).list();
        assertEquals(3, list3.size());
        assertEquals("B0", list3.get(0).id);
        assertEquals("B2", list3.get(2).id);
    }

    @Test
    public void testQuery2() throws Exception {
        List<User> list1 = warpdb.from(User.class).where("id>=?", "B0").list();
        assertEquals(5, list1.size());
        assertEquals("B0", list1.get(0).id);
        assertEquals("B4", list1.get(4).id);
        List<User> list2 = warpdb.from(User.class).where("id>=?", "B0").limit(2, 2).list();
        assertEquals(2, list2.size());
        assertEquals("B2", list2.get(0).id);
        assertEquals("B3", list2.get(1).id);
    }

    @Test
    public void testQuery3() throws Exception {
        List<User> list1 = warpdb.from(User.class).where("id>=?", "B0").orderBy("name").list();
        assertEquals(5, list1.size());
        assertEquals("Mr B0", list1.get(0).name);
        assertEquals("Mr B4", list1.get(4).name);
        List<User> list2 = warpdb.from(User.class).where("id>=?", "B0").orderBy("name").desc().list();
        assertEquals(5, list2.size());
        assertEquals("Mr B4", list2.get(0).name);
        assertEquals("Mr B0", list2.get(4).name);
        List<User> list3 = warpdb.from(User.class).where("id>=?", "B0").orderBy("name").desc().limit(2, 2).list();
        assertEquals(2, list3.size());
        assertEquals("Mr B2", list3.get(0).name);
        assertEquals("Mr B1", list3.get(1).name);
    }

    @Test
    public void testQuery4() throws Exception {
        List<User> list1 = warpdb.select("name").from(User.class).where("id>=?", "B3").list();
        assertEquals(2, list1.size());
        assertEquals("Mr B3", list1.get(0).name);
        assertEquals("Mr B4", list1.get(1).name);
        assertNull(list1.get(0).id);
        assertNull(list1.get(1).id);
        assertNull(list1.get(0).email);
        assertNull(list1.get(1).email);
        List<User> list2 = warpdb.select("tag").distinct().from(User.class).list();
        assertEquals(2, list2.size());
    }

    @Test
    public void testQuery5() throws Exception {
        List<User> list1 = warpdb.select().from(User.class).where("id>?", "A3").and("id<?", "B1").list();
        assertEquals(2, list1.size());
        assertEquals("A4", list1.get(0).id);
        assertEquals("B0", list1.get(1).id);
    }

    @Test
    public void testCount() throws Exception {
        assertEquals(10, warpdb.from(User.class).count());
        assertEquals(2, warpdb.from(User.class).where("id>=?", "B3").count());
        assertEquals(0, warpdb.from(User.class).where("id=?", "X").count());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadWhereClause() {
        warpdb.select().from(User.class).where("id>? or id<?", "A3").list();
    }
}
