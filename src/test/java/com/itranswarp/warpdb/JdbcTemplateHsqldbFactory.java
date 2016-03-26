package com.itranswarp.warpdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.HSQLDialect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.mysql.jdbc.StringUtils;

/**
 * Create a in-memory hsqldb and return JdbcTemplate.
 * 
 * @author michael
 */
public class JdbcTemplateHsqldbFactory {

	static final Log log = LogFactory.getLog(JdbcTemplateHsqldbFactory.class);

	public static JdbcTemplate createJdbcTemplate() {
		try {
			DataSource dataSource = new DriverManagerDataSource("jdbc:hsqldb:mem:testdb", "SA", "");
			// init database:
			String[] sqls = generateDDL().split(";");
			Connection conn = dataSource.getConnection();
			Statement stmt = conn.createStatement();
			for (String sql : sqls) {
				if (!StringUtils.isEmptyOrWhitespaceOnly(sql)) {
					log.info("Execute SQL: " + sql.trim());
					// hsqldb do not support text, mediumtext:
					stmt.executeUpdate(sql.trim().replace("mediumtext", "longvarchar").replace("text", "longvarchar"));
				}
			}
			stmt.close();
			conn.close();
			return new JdbcTemplate(dataSource);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static String ddl = null;

	static String generateDDL() throws Exception {
		if (ddl != null) {
			return ddl;
		}
		File file = new File(".").getAbsoluteFile();
		String schemaOutput = file.getCanonicalPath() + File.separator + "target" + File.separator + "ddl4test.sql";
		DDLGenerator generator = new DDLGenerator();
		generator.export("com.itranswarp.warpdb", HSQLDialect.class, schemaOutput);
		// read file:
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(schemaOutput))) {
			for (;;) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				sb.append(line);
			}
		}
		ddl = sb.toString();
		return ddl;
	}
}
