package com.itranswarp.warpdb.util;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class DDLGenerator {

	final Log log = LogFactory.getLog(getClass());

	public void export(List<String> basePackages, Class<?> dialect, String outputFile) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (String basePackage : basePackages) {
			List<Class<?>> scanned = ClassUtils.scan(basePackage, c -> {
				return c.isAnnotationPresent(Entity.class);
			});
			for (Class<?> cls : scanned) {
				if (isAdded(classes, cls)) {
					log.warn("Duplicate class found: " + cls.getName());
				} else {
					classes.add(cls);
				}
			}
		}
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

	boolean isAdded(List<Class<?>> list, Class<?> test) {
		for (Class<?> cls : list) {
			if (cls.getName().equals(test.getName())) {
				return true;
			}
		}
		return false;
	}
}
