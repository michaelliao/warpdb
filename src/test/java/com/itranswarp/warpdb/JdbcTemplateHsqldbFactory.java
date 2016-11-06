package com.itranswarp.warpdb;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Create a in-memory hsqldb and return JdbcTemplate.
 * 
 * @author michael
 */
public class JdbcTemplateHsqldbFactory {

	static final Log log = LogFactory.getLog(JdbcTemplateHsqldbFactory.class);

	public static JdbcTemplate createJdbcTemplate() {
		try {
			DataSource dataSource = new DriverManagerDataSource("jdbc:hsqldb:mem:" + getDbName(), "SA", "");
			// init database:
			String[] sqls = generateDDL().split(";");
			Connection conn = dataSource.getConnection();
			Statement stmt = conn.createStatement();
			for (String sql : sqls) {
				if (sql != null && !sql.trim().isEmpty()) {
					log.info("Execute SQL: " + sql.trim());
					// hsqldb do not support text, mediumtext, longtext:
					stmt.executeUpdate(sql.trim().toLowerCase().replace(" longtext ", " longvarchar ")
							.replace(" mediumtext ", " longvarchar ").replace(" text ", " longvarchar "));
				}
			}
			stmt.close();
			conn.close();
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
			return warpdb.exportSchema();
		}
		return ddl;
	}

	static int dbindex = 0;

	static String ddl = null;

}
