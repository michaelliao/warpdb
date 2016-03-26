package com.itranswarp.warpdb;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class ClassUtil {

	final Log log = LogFactory.getLog(getClass());

	public List<Class<?>> scan(String basePackage, Predicate<Class<?>> predicate) {
		List<Class<?>> classes = new ArrayList<Class<?>>(100);
		try {
			this.loadClasses(basePackage, classes, predicate);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return classes;
	}

	void loadClasses(String basePackage, List<Class<?>> classes, Predicate<Class<?>> predicate) throws Exception {
		ClassLoader cl = getClass().getClassLoader();
		Enumeration<URL> resources = cl.getResources(basePackage.replace('.', '/'));
		while (resources.hasMoreElements()) {
			String url = resources.nextElement().toString();
			if (url.startsWith("file:")) {
				File dir = new File(url.substring(5));
				if (dir.isDirectory()) {
					loadClassesInDir(dir, basePackage, classes, predicate);
				}
			} else if (url.startsWith("jar:file:")) {
				String jar = url.substring(9, url.length() - basePackage.length() - 2);
				loadClassesInJar(new File(jar), basePackage, classes, predicate);
			}
		}
	}

	void loadClassesInDir(File dir, String basePackage, List<Class<?>> classes, Predicate<Class<?>> predicate)
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

	void loadClassesInJar(File jarFile, String basePackage, List<Class<?>> classes, Predicate<Class<?>> predicate)
			throws Exception {
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

	Class<?> tryLoadClass(String name) throws ClassNotFoundException {
		try {
			return Class.forName(name);
		} catch (NoClassDefFoundError e) {
			return null;
		}
	}
}
