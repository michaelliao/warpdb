package com.itranswarp.warpdb.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public final class ClassUtils {

	static final Log log = LogFactory.getLog(ClassUtils.class);

	/**
	 * Scan @Entity classes in base packages.
	 * 
	 * @param basePackages
	 *            base package names.
	 * @return List of entity class.
	 */
	public static List<Class<?>> scanEntities(String... basePackages) {
		List<Class<?>> classes = new ArrayList<>();
		for (String basePackage : basePackages) {
			classes.addAll(ClassUtils.scan(basePackage, c -> {
				return c.isAnnotationPresent(Entity.class);
			}));
		}
		return classes;
	}

	/**
	 * Scan classes that match the predicate.
	 * 
	 * @param basePackage
	 *            Base package name.
	 * @param predicate
	 *            Filter condition.
	 * @return List of classes.
	 */
	public static List<Class<?>> scan(String basePackage, Predicate<Class<?>> predicate) {
		log.info("Scan " + basePackage + "...");
		Reflections ref = new Reflections(basePackage, new SubTypesScanner(false));
		Set<Class<?>> set = ref.getSubTypesOf(Object.class);
		return set.stream().filter((c) -> c.getPackage().getName().startsWith(basePackage)).filter(predicate)
				.collect(Collectors.toList());
	}

	public static List<Type> getGenericInterfacesIncludeHierarchy(Class<?> clazz) {
		List<Type> list = new ArrayList<>();
		addGenericInterfaces(clazz, list);
		return list;
	}

	static void addGenericInterfaces(Class<?> clazz, List<Type> list) {
		Type[] types = clazz.getGenericInterfaces();
		for (Type type : types) {
			list.add(type);
		}
		if (clazz.getSuperclass() != Object.class) {
			addGenericInterfaces(clazz.getSuperclass(), list);
		}
	}
}
