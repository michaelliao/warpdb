package com.itranswarp.warpdb;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class WhereTest {

    @Test
    public void testExtractWords() {
        String[] words = Where.extractWords("name=? AND age>0 OR created_at<?");
        assertArrayEquals(new String[] { "name", "AND", "age", "0", "OR", "created_at" }, words);
    }
}
