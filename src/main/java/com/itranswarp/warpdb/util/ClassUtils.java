package com.itranswarp.warpdb.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
		List<Class<?>> classes = new ArrayList<>(100);
		loadClasses(basePackage, classes, predicate);
		return classes;
	}

	static void loadClasses(String basePackage, List<Class<?>> classes, Predicate<Class<?>> predicate) {
		ClassLoader cl = ClassUtils.class.getClassLoader();
		Enumeration<URL> resources = null;
		try {
			resources = cl.getResources(basePackage.replace('.', '/'));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		while (resources.hasMoreElements()) {
			String url = resources.nextElement().toString();
			if (url.startsWith("file:")) {
				File dir = new File(url.substring(5));
				if (dir.isDirectory()) {
					try {
						loadClassesInDir(dir, basePackage, classes, predicate);
					} catch (Exception e) {
						log.warn("error scan url: " + url, e);
					}
				}
			} else if (url.startsWith("jar:file:")) {
				String jar = url.substring(9, url.length() - basePackage.length() - 2);
				try {
					loadClassesInJar(new File(jar), basePackage, classes, predicate);
				} catch (Exception e) {
					log.warn("error scan url: " + url, e);
				}
			}
		}
	}

	static void loadClassesInDir(File dir, String basePackage, List<Class<?>> classes, Predicate<Class<?>> predicate)
			throws Exception {
		log.debug("Scan classes in dir: " + dir.getAbsolutePath());
		String[] subs = dir.list();
		for (String sub : subs) {
			File file = new File(dir.getAbsolutePath() + File.separator + sub);
			if (file.isDirectory()) {
				loadClassesInDir(file, basePackage + "." + sub, classes, predicate);
			} else if (sub.endsWith(".class") && file.isFile()) {
				String className = basePackage + "." + sub.substring(0, sub.length() - 6);
				Class<?> clazz = tryLoadClass(className);
				if (clazz != null && predicate.test(clazz)) {
					log.info("Found target class: " + className);
					classes.add(clazz);
				}
			}
		}
	}

	static void loadClassesInJar(File jarFile, String basePackage, List<Class<?>> classes,
			Predicate<Class<?>> predicate) throws Exception {
		log.info("Scan classes in jar: " + jarFile.getCanonicalPath());
		try (JarFile jar = new JarFile(jarFile.getCanonicalPath())) {
			Enumeration<JarEntry> e = jar.entries();
			while (e.hasMoreElements()) {
				JarEntry entry = e.nextElement();
				String name = entry.getName();
				if (name.endsWith(".class") && !entry.isDirectory()) {
					String className = name.substring(0, name.length() - 6).replace('/', '.');
					Class<?> clazz = tryLoadClass(className);
					if (clazz != null && predicate.test(clazz)) {
						classes.add(clazz);
					}
				}
			}
		}
	}

	static Class<?> tryLoadClass(String name) throws ClassNotFoundException {
		try {
			return Class.forName(name);
		} catch (NoClassDefFoundError e) {
			log.warn("Failed to load class: " + name);
			return null;
		}
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
