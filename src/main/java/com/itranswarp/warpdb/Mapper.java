package com.itranswarp.warpdb;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.itranswarp.warpdb.util.NameUtils;

final class Mapper<T> {

	final Class<T> entityClass;
	final String tableName;

	// @Id property:
	final AccessibleProperty id;
	// @Version property:
	final AccessibleProperty version;

	// all properties including @Id, key is property name (NOT column name)
	final List<AccessibleProperty> allProperties;

	// lower-case property name -> AccessibleProperty
	final Map<String, AccessibleProperty> allPropertiesMap;

	final List<AccessibleProperty> insertableProperties;
	final List<AccessibleProperty> updatableProperties;

	// lower-case property name -> AccessibleProperty
	final Map<String, AccessibleProperty> updatablePropertiesMap;

	final BeanRowMapper<T> rowMapper;

	final String querySQL;
	final String insertSQL;
	final String updateSQL;
	final String deleteSQL;

	final Listener prePersist;
	final Listener preUpdate;
	final Listener preRemove;
	final Listener postLoad;
	final Listener postPersist;
	final Listener postUpdate;
	final Listener postRemove;

	public Mapper(Class<T> clazz) {
		super();
		List<AccessibleProperty> all = getPropertiesIncludeHierarchy(clazz);
		// check duplicate name:
		Set<String> propertyNamesSet = new HashSet<>();
		for (String propertyName : all.stream().map((p) -> {
			return p.propertyName;
		}).toArray(String[]::new)) {
			if (!propertyNamesSet.add(propertyName.toLowerCase())) {
				throw new ConfigurationException(
						"Duplicate property name found: " + propertyName + " in class: " + clazz.getName());
			}
		}
		Set<String> columnNamesSet = new HashSet<>();
		for (String columnName : all.stream().map((p) -> {
			return p.columnName;
		}).toArray(String[]::new)) {
			if (!columnNamesSet.add(columnName.toLowerCase())) {
				throw new ConfigurationException("Duplicate column name found: " + columnName);
			}
		}
		// check @Id:
		AccessibleProperty[] ids = all.stream().filter((p) -> {
			return p.isId();
		}).toArray(AccessibleProperty[]::new);
		if (ids.length == 0) {
			throw new ConfigurationException("No @Id found.");
		}
		if (ids.length > 1) {
			throw new ConfigurationException("Multiple @Id found.");
		}
		// get @Version:
		AccessibleProperty[] versions = all.stream().filter((p) -> {
			return p.isVersion();
		}).toArray(AccessibleProperty[]::new);
		if (versions.length > 1) {
			throw new ConfigurationException("Multiple @Version found.");
		}
		this.version = versions.length == 0 ? null : versions[0];

		this.allProperties = all;
		this.allPropertiesMap = buildPropertiesMap(this.allProperties);

		this.insertableProperties = all.stream().filter((p) -> {
			if (p.isIdentityId()) {
				return false;
			}
			return p.isInsertable();
		}).collect(Collectors.toList());

		this.updatableProperties = all.stream().filter((p) -> {
			return p.isUpdatable();
		}).collect(Collectors.toList());

		this.updatablePropertiesMap = buildPropertiesMap(this.updatableProperties);

		// init:
		this.id = ids[0];
		this.entityClass = clazz;
		this.tableName = getTableName(clazz);

		this.querySQL = "SELECT * FROM " + this.tableName + " WHERE " + this.id.columnName + " = ?";

		this.insertSQL = "INSERT INTO " + this.tableName + " ("
				+ String.join(", ", this.insertableProperties.stream().map((p) -> {
					return p.columnName;
				}).toArray(String[]::new)) + ") VALUES (" + numOfQuestions(this.insertableProperties.size()) + ")";

		this.updateSQL = "UPDATE " + this.tableName + " SET "
				+ String.join(", ", this.updatableProperties.stream().map((p) -> {
					return p.columnName + " = ?";
				}).toArray(String[]::new)) + " WHERE " + this.id.columnName + " = ?";

		this.deleteSQL = "DELETE FROM " + this.tableName + " WHERE " + this.id.columnName + " = ?";

		this.rowMapper = new BeanRowMapper<>(this.entityClass, this.allProperties);

		List<Method> methods = this.findMethods(clazz);
		this.prePersist = findListener(methods, PrePersist.class);
		this.preUpdate = findListener(methods, PreUpdate.class);
		this.preRemove = findListener(methods, PreRemove.class);
		this.postLoad = findListener(methods, PostLoad.class);
		this.postPersist = findListener(methods, PostPersist.class);
		this.postUpdate = findListener(methods, PostUpdate.class);
		this.postRemove = findListener(methods, PostRemove.class);
	}

	public String ddl() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("CREATE TABLE ").append(this.tableName).append(" (\n");
		sb.append(String.join(",\n", this.allProperties.stream().map((p) -> {
			return "  " + p.columnName + " " + p.columnDefinition // definition
					+ (p.isIdentityId() ? " AUTO_INCREMENT" : "") // identity
					+ (p.nullable ? " NULL" : " NOT NULL") // nullable?
					+ (!p.isId() && p.unique ? " UNIQUE" : ""); // unique?
		}).toArray(String[]::new)));
		sb.append(",\n");
		// add unique key:
		sb.append(getUniqueKey());
		// add index:
		sb.append(getIndex());
		// add primary key:
		sb.append("  PRIMARY KEY(").append(this.id.columnName).append(")\n");
		sb.append(");\n");
		return sb.toString();
	}

	String getUniqueKey() {
		Table table = this.entityClass.getAnnotation(Table.class);
		if (table != null) {
			return Arrays.stream(table.uniqueConstraints()).map((c) -> {
				String name = c.name().isEmpty() ? "UNI_" + String.join("_", c.columnNames()) : c.name();
				return "  CONSTRAINT " + name + " UNIQUE (" + String.join(", ", c.columnNames()) + "),\n";
			}).reduce("", (acc, s) -> {
				return acc + s;
			});
		}
		return "";
	}

	String getIndex() {
		Table table = this.entityClass.getAnnotation(Table.class);
		if (table != null) {
			return Arrays.stream(table.indexes()).map((c) -> {
				if (c.unique()) {
					String name = c.name().isEmpty() ? "UNI_" + c.columnList().replace(" ", "").replace(",", "_")
							: c.name();
					return "  CONSTRAINT " + name + " UNIQUE (" + c.columnList() + "),\n";
				} else {
					String name = c.name().isEmpty() ? "IDX_" + c.columnList().replace(" ", "").replace(",", "_")
							: c.name();
					return "  INDEX " + name + " (" + c.columnList() + "),\n";
				}
			}).reduce("", (acc, s) -> {
				return acc + s;
			});
		}
		return "";
	}

	Map<String, AccessibleProperty> buildPropertiesMap(List<AccessibleProperty> props) {
		Map<String, AccessibleProperty> map = new HashMap<>();
		for (AccessibleProperty prop : props) {
			map.put(prop.propertyName.toLowerCase(), prop);
		}
		return map;
	}

	Listener findListener(List<Method> methods, Class<? extends Annotation> anno) {
		Method target = null;
		for (Method m : methods) {
			if (m.isAnnotationPresent(anno)) {
				if (target == null) {
					target = m;
				} else {
					throw new ConfigurationException("Found multiple @" + anno.getSimpleName());
				}
			}
		}
		if (target == null) {
			return EMPTY_LISTENER;
		}
		// check target:
		if (target.getParameterTypes().length > 0) {
			throw new ConfigurationException("Invalid listener method: " + target.getName() + ". Expect zero args.");
		}
		if (Modifier.isStatic(target.getModifiers())) {
			throw new ConfigurationException("Invalid listener method: " + target.getName() + ". Cannot be static.");
		}
		target.setAccessible(true);
		Method listener = target;
		return (obj) -> {
			listener.invoke(obj);
		};
	}

	static final Listener EMPTY_LISTENER = new Listener() {
		@Override
		public void invoke(Object obj) throws IllegalAccessException, InvocationTargetException {
		}
	};

	List<Method> findMethods(Class<T> clazz) {
		List<Method> list = new ArrayList<>(50);
		findMethodsIncludeHierarchy(clazz, list);
		return list;
	}

	void findMethodsIncludeHierarchy(Class<?> clazz, List<Method> methods) {
		Method[] ms = clazz.getDeclaredMethods();
		for (Method m : ms) {
			methods.add(m);
		}
		if (clazz.getSuperclass() != Object.class) {
			findMethodsIncludeHierarchy(clazz.getSuperclass(), methods);
		}
	}

	String numOfQuestions(int n) {
		String[] qs = new String[n];
		return String.join(", ", Arrays.stream(qs).map((s) -> {
			return "?";
		}).toArray(String[]::new));
	}

	String getTableName(Class<?> clazz) {
		Table table = clazz.getAnnotation(Table.class);
		if (table != null && !table.name().isEmpty()) {
			return table.name();
		}
		return NameUtils.toCamelCaseName(clazz.getSimpleName());
	}

	List<AccessibleProperty> getPropertiesIncludeHierarchy(Class<?> clazz) {
		List<AccessibleProperty> properties = new ArrayList<>();
		addFieldPropertiesIncludeHierarchy(clazz, properties);
		// find methods:
		List<AccessibleProperty> foundMethods = Arrays.stream(clazz.getMethods()).filter((m) -> {
			int mod = m.getModifiers();
			// exclude @Transient:
			if (m.isAnnotationPresent(Transient.class)) {
				return false;
			}
			// exclude static:
			if (Modifier.isStatic(mod)) {
				return false;
			}
			// exclude getClass():
			if (m.getName().equals("getClass")) {
				return false;
			}
			// check if getter:
			if (m.getParameterTypes().length > 0) {
				return false;
			}
			if (m.getName().startsWith("get") && m.getName().length() >= 4) {
				return true;
			}
			if (m.getName().startsWith("is") && m.getName().length() >= 3
					&& (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
				return true;
			}
			return false;
		}).map((getter) -> {
			Class<?> type = getter.getReturnType();
			String name;
			if (getter.getName().startsWith("get")) {
				name = Character.toLowerCase(getter.getName().charAt(3)) + getter.getName().substring(4);
			} else {
				// isXxx()
				name = Character.toLowerCase(getter.getName().charAt(2)) + getter.getName().substring(3);
			}
			// find setter:
			String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
			Method setter;
			try {
				setter = clazz.getMethod(setterName, type);
			} catch (NoSuchMethodException e) {
				throw new ConfigurationException(e);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			}
			return new AccessibleProperty(name, getter, setter);
		}).collect(Collectors.toList());
		properties.addAll(foundMethods);
		return properties;
	}

	void addFieldPropertiesIncludeHierarchy(Class<?> clazz, List<AccessibleProperty> collector) {
		List<AccessibleProperty> foundFields = Arrays.stream(clazz.getDeclaredFields()).filter((f) -> {
			int mod = f.getModifiers();
			// exclude @Transient:
			if (f.isAnnotationPresent(Transient.class)) {
				return false;
			}
			// exclude final, static:
			if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
				return false;
			}
			return true;
		}).map((f) -> {
			return new AccessibleProperty(f);
		}).collect(Collectors.toList());
		collector.addAll(foundFields);
		if (clazz.getSuperclass() != Object.class) {
			addFieldPropertiesIncludeHierarchy(clazz.getSuperclass(), collector);
		}
	}

}
