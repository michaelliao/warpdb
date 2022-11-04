package com.itranswarp.warpdb;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;

import com.itranswarp.warpdb.test.BaseEntity;

public class WarpDbTestBase {

    protected WarpDb warpdb = null;

    @BeforeEach
    public void setUpDatabase() {
        warpdb = new WarpDb();
        warpdb.setBasePackages(Arrays.asList("com.itranswarp.warpdb.test"));
        warpdb.setJdbcTemplate(JdbcTemplateHsqldbFactory.createJdbcTemplate());
        warpdb.init();
        BaseEntity.resetId();
    }
}
