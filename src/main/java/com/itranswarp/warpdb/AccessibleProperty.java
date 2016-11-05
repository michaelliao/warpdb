package com.itranswarp.warpdb;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Represent a bean property.
 * 
 * @author liaoxuefeng
 */
class AccessibleProperty {

	// Field or Method:
	final AccessibleObject accessible;

	// java type:
	final Class<?> propertyType;

	// converter:
	final AttributeConverter<Object, Object> converter;

	// java bean property name:
	final String propertyName;

	// table column name:
	final String columnName;

	// column DDL:
	final String columnDefinition;

	// getter function:
	final PropertyGetter getter;

	// setter function:
	final PropertySetter setter;

	boolean isId() {
		return this.accessible.isAnnotationPresent(Id.class);
	}

	boolean isVersion() {
		boolean isVersion = this.accessible.isAnnotationPresent(Version.class);
		if (isVersion) {
			if (!VERSION_TYPES.contains(this.propertyType)) {
				throw new RuntimeException("Unsupported @Version type: " + this.propertyType.getName());
			}
		}
		return isVersion;
	}

	boolean isInsertable() {
		if (isId()) {
			GeneratedValue gv = this.accessible.getAnnotation(GeneratedValue.class);
			if (gv != null && gv.strategy() == GenerationType.IDENTITY) {
				return false;
			}
		}
		Column col = this.accessible.getAnnotation(Column.class);
		return col == null || col.insertable();
	}

	boolean isUpdatable() {
		if (isId()) {
			return false;
		}
		Column col = this.accessible.getAnnotation(Column.class);
		return col == null || col.updatable();
	}

	public AccessibleProperty(Field f) {
		this(f.getType(), f.getName(), f, (obj) -> {
			return f.get(obj);
		}, (obj, value) -> {
			f.set(obj, value);
		});
	}

	public AccessibleProperty(String name, Method getter, Method setter) {
		this(getter.getReturnType(), name, getter, (obj) -> {
			return getter.invoke(obj);
		}, (obj, value) -> {
			setter.invoke(obj, value);
		});
	}

	private AccessibleProperty(Class<?> type, String propertyName, AccessibleObject accessible, PropertyGetter getter,
			PropertySetter setter) {
		accessible.setAccessible(true);
		this.accessible = accessible;
		this.propertyType = checkPropertyType(type);
		this.converter = getConverter(accessible);
		this.propertyName = propertyName;
		this.columnName = getColumnName(accessible, propertyName);
		this.columnDefinition = getColumnDefinition(accessible, propertyType);
		this.getter = getter;
		this.setter = setter;
	}

	@SuppressWarnings("unchecked")
	private AttributeConverter<Object, Object> getConverter(AccessibleObject accessible) {
		Convert converter = accessible.getAnnotation(Convert.class);
		if (converter != null) {
			Class<?> converterClass = converter.converter();
			if (!converterClass.isAssignableFrom(AttributeConverter.class)) {
				throw new RuntimeException(
						"Converter class must be AttributeConverter rather than " + converterClass.getName());
			}
			try {
				return (AttributeConverter<Object, Object>) converterClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Cannot instantiate Converter: " + converterClass.getName(), e);
			}
		}
		return null;
	}

	private static Class<?> checkPropertyType(Class<?> type) {
		if (DEFAULT_COLUMN_TYPES.containsKey(type)) {
			return type;
		}
		throw new RuntimeException("Unsupported type: " + type);
	}

	private static String getColumnName(AccessibleObject ao, String defaultName) {
		Column col = ao.getAnnotation(Column.class);
		if (col == null || col.name().isEmpty()) {
			return defaultName;
		}
		return col.name();
	}

	private static String getColumnDefinition(AccessibleObject ao, Class<?> type) {
		Column col = ao.getAnnotation(Column.class);
		if (col == null || col.columnDefinition().isEmpty()) {
			return getDefaultColumnType(type, col);
		}
		return col.columnDefinition();
	}

	private static String getDefaultColumnType(Class<?> type, Column col) {
		String ddl = DEFAULT_COLUMN_TYPES.get(type);
		if (ddl.equals("VARCHAR($1)")) {
			ddl = ddl.replace("$1", String.valueOf(col == null ? 255 : col.length()));
		}
		if (ddl.equals("DECIMAL($1,$2)")) {
			int preci = col == null ? 0 : col.precision();
			int scale = col == null ? 0 : col.scale();
			if (preci == 0) {
				preci = 10;
			}
			ddl = ddl.replace("$1", String.valueOf(preci)).replace("$2", String.valueOf(scale));
		}
		return ddl;
	}

	static final Map<Class<?>, String> DEFAULT_COLUMN_TYPES = new HashMap<>();

	static final Set<Class<?>> VERSION_TYPES = new HashSet<>();

	static {
		DEFAULT_COLUMN_TYPES.put(String.class, "VARCHAR($1)");

		DEFAULT_COLUMN_TYPES.put(boolean.class, "BIT");
		DEFAULT_COLUMN_TYPES.put(Boolean.class, "BIT");

		DEFAULT_COLUMN_TYPES.put(byte.class, "TINYINT");
		DEFAULT_COLUMN_TYPES.put(Byte.class, "TINYINT");
		DEFAULT_COLUMN_TYPES.put(short.class, "SMALLINT");
		DEFAULT_COLUMN_TYPES.put(Short.class, "SMALLINT");
		DEFAULT_COLUMN_TYPES.put(int.class, "INTEGER");
		DEFAULT_COLUMN_TYPES.put(Integer.class, "INTEGER");
		DEFAULT_COLUMN_TYPES.put(long.class, "BIGINT");
		DEFAULT_COLUMN_TYPES.put(Long.class, "BIGINT");
		DEFAULT_COLUMN_TYPES.put(float.class, "REAL");
		DEFAULT_COLUMN_TYPES.put(Float.class, "REAL");
		DEFAULT_COLUMN_TYPES.put(double.class, "DOUBLE");
		DEFAULT_COLUMN_TYPES.put(Double.class, "DOUBLE");

		DEFAULT_COLUMN_TYPES.put(BigDecimal.class, "DECIMAL($1,$2)");
		DEFAULT_COLUMN_TYPES.put(java.sql.Date.class, "DATE");
		DEFAULT_COLUMN_TYPES.put(LocalDate.class, "DATE");
		DEFAULT_COLUMN_TYPES.put(LocalTime.class, "TIME");
		DEFAULT_COLUMN_TYPES.put(java.util.Date.class, "DATETIME");
		DEFAULT_COLUMN_TYPES.put(java.sql.Timestamp.class, "TIMESTAMP");

		DEFAULT_COLUMN_TYPES.put(java.sql.Blob.class, "BLOB");
		DEFAULT_COLUMN_TYPES.put(java.sql.Clob.class, "CLOB");

		VERSION_TYPES.addAll(Arrays.asList(long.class, Long.class, int.class, Integer.class, short.class, Short.class,
				java.sql.Timestamp.class));
	}

}
