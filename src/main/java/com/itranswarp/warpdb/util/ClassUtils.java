package com.itranswarp.warpdb.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public final class ClassUtils {

	/**
	 * Scan @Entity classes in base packages.
	 * 
	 * @param basePackages
	 *            base package names.
	 * @return List of entity class.
	 */
	public static List<Class<?>> scanEntities(String... basePackages) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		List<Class<?>> classes = new ArrayList<>();
		for (String basePackage : basePackages) {
			Set<BeanDefinition> beans = provider.findCandidateComponents(basePackage);
			for (BeanDefinition bean : beans) {
				try {
					classes.add(Class.forName(bean.getBeanClassName()));
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return classes;
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
