package com.itranswarp.warpdb;

import org.junit.jupiter.api.Test;

public class WarpDbDDLTest extends WarpDbTestBase {

    @Test
    public void testDDL() throws Exception {
        System.out.println(warpdb.getDDL());
    }
}
