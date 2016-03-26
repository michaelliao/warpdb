package com.itranswarp.warpdb;

import java.util.List;

import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class DDLGenerator {

	final Log log = LogFactory.getLog(getClass());

	public void export(String basePackage, Class<?> dialect, String outputFile) {
		List<Class<?>> classes = new ClassUtil().scan(basePackage, c -> {
			return c.isAnnotationPresent(Entity.class);
		});
		Configuration cfg = new Configuration();
		cfg.setProperty("hibernate.hbm2ddl.auto", "create");
		cfg.setProperty("hibernate.dialect", dialect.getName());
		for (Class<?> clazz : classes) {
			cfg.addAnnotatedClass(clazz);
		}
		SchemaExport se = new SchemaExport(cfg);
		se.setDelimiter(";");
		se.setOutputFile(outputFile);
		se.execute(true, false, false, false);
		log.info("DDL script was successfully exported to file: " + outputFile);
	}

}
