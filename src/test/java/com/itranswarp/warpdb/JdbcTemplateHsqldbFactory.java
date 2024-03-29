package com.itranswarp.warpdb;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Create a in-memory hsqldb and return JdbcTemplate.
 * 
 * @author liaoxuefeng
 */
public class JdbcTemplateHsqldbFactory {

    static final Logger logger = LoggerFactory.getLogger(JdbcTemplateHsqldbFactory.class);

    public static JdbcTemplate createJdbcTemplate() {
        try {
            DataSource dataSource = new DriverManagerDataSource("jdbc:hsqldb:mem:" + getDbName(), "SA", "");
            // init database:
            String[] sqls = generateDDL().split(";");
            try (Connection conn = dataSource.getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    for (String sql : sqls) {
                        if (sql != null && !sql.trim().isEmpty()) {
                            logger.info("Generated SQL: {}", sql);
                            sql = sql.trim().toLowerCase();
                            // remove index NAME (column list)
                            for (;;) {
                                int n = sql.indexOf("  index ");
                                if (n >= 0) {
                                    int n2 = sql.indexOf("),", n);
                                    sql = sql.substring(0, n) + sql.substring(n2 + 3);
                                } else {
                                    break;
                                }
                            }
                            sql = sql.replace(" auto_increment not null", " GENERATED BY DEFAULT AS IDENTITY (START WITH 1)")
                                    .replace(" longtext ", " longvarchar ").replace(" mediumtext ", " longvarchar ").replace(" text ", " longvarchar ");
                            logger.info("Execute SQL: {}", sql);
                            // hsqldb do not support text, mediumtext, longtext:
                            stmt.executeUpdate(sql);
                        }
                    }
                }
            }
            return new JdbcTemplate(dataSource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getDbName() {
        dbindex++;
        return "testdb" + dbindex;
    }

    static String generateDDL() throws Exception {
        if (ddl == null) {
            WarpDb warpdb = new WarpDb();
            warpdb.setBasePackages(Arrays.asList("com.itranswarp.warpdb.test"));
            warpdb.init();
            return warpdb.getDDL();
        }
        return ddl;
    }

    static int dbindex = 0;

    static String ddl = null;

}
