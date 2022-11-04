package com.itranswarp.warpdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.itranswarp.warpdb.test.User;

public class WarpDbPageQueryTest extends WarpDbTestBase {

    @BeforeEach
    public void prepareData() {
        for (int i = 0; i < 99; i++) {
            User u = new User();
            u.id = String.format("A-%02d", i);
            u.name = "Mr " + i;
            u.email = "A" + i + "@somewhere.org";
            warpdb.insert(u);
        }
    }

    @Test
    public void testPageQuery() throws Exception {
        // page 1:
        PagedResults<User> pr1 = warpdb.from(User.class).orderBy("id").list(1, 10);
        assertEquals(99, pr1.page.totalItems);
        assertEquals(10, pr1.page.totalPages);
        assertEquals(10, pr1.page.itemsPerPage);
        assertEquals(1, pr1.page.pageIndex);
        assertEquals(10, pr1.results.size());
        assertEquals("A-00", pr1.results.get(0).id);
        assertEquals("A-09", pr1.results.get(9).id);
        // page 2:
        PagedResults<User> pr2 = warpdb.from(User.class).orderBy("id").list(2, 10);
        assertEquals(99, pr2.page.totalItems);
        assertEquals(10, pr2.page.totalPages);
        assertEquals(10, pr2.page.itemsPerPage);
        assertEquals(2, pr2.page.pageIndex);
        assertEquals(10, pr2.results.size());
        assertEquals("A-10", pr2.results.get(0).id);
        assertEquals("A-19", pr2.results.get(9).id);
        // page 10:
        PagedResults<User> pr10 = warpdb.from(User.class).orderBy("id").list(10, 10);
        assertEquals(99, pr10.page.totalItems);
        assertEquals(10, pr10.page.totalPages);
        assertEquals(10, pr10.page.itemsPerPage);
        assertEquals(10, pr10.page.pageIndex);
        assertEquals(9, pr10.results.size());
        assertEquals("A-90", pr10.results.get(0).id);
        assertEquals("A-98", pr10.results.get(8).id);
        // page 11:
        PagedResults<User> pr11 = warpdb.from(User.class).orderBy("id").list(11, 10);
        assertEquals(99, pr11.page.totalItems);
        assertEquals(10, pr11.page.totalPages);
        assertEquals(10, pr11.page.itemsPerPage);
        assertEquals(11, pr11.page.pageIndex);
        assertEquals(0, pr11.results.size());
    }

    @Test
    public void testPagedQueryWithInvalidPageIndex() {
        assertThrows(IllegalArgumentException.class, () -> {
            warpdb.from(User.class).list(0, 10);
        });
    }

    @Test
    public void testPagedQueryWithInvalidPageSizeTooSmall() {
        assertThrows(IllegalArgumentException.class, () -> {
            warpdb.from(User.class).list(1, 0);
        });
    }

    @Test
    public void testPagedQueryWithInvalidPageSizeTooLarge() {
        assertThrows(IllegalArgumentException.class, () -> {
            warpdb.from(User.class).list(1, 1001);
        });
    }
}
