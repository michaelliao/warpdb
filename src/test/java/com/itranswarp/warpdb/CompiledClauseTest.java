package com.itranswarp.warpdb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.itranswarp.warpdb.test.TodoEntity;

public class CompiledClauseTest extends WarpDbTestBase {

    Mapper<?> mapper;

    @BeforeEach
    public void setUp() throws Exception {
        mapper = new Mapper<>(TodoEntity.class);
    }

    @Test
    public void testName() {
        CompiledClause cc = CompiledClause.doCompile(mapper, "name=?");
        assertEquals("f_name=?", cc.clause);
        assertEquals(1, cc.converters.length);
    }

    @Test
    public void testAnd() {
        CompiledClause cc = CompiledClause.doCompile(mapper, " NAME = ? And TargetDate LIKE ? ");
        assertEquals(" f_name = ? And f_Target_date LIKE ? ", cc.clause);
        assertEquals(2, cc.converters.length);
    }

    @Test
    public void testIsNull() {
        CompiledClause cc = CompiledClause.doCompile(mapper, " NAME = ? And TargetDate is NULL ");
        assertEquals(" f_name = ? And f_Target_date is NULL ", cc.clause);
        assertEquals(1, cc.converters.length);
    }

    @Test
    public void testBetween() {
        CompiledClause cc = CompiledClause.doCompile(mapper, " NAME = ? And TargetDate between(?,?)");
        assertEquals(" f_name = ? And f_Target_date between(?,?)", cc.clause);
        assertEquals(3, cc.converters.length);
    }

}
