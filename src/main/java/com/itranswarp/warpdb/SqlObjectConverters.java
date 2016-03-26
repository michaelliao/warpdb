package com.itranswarp.warpdb;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class SqlObjectConverters {

	Map<String, SqlObjectConverter> sql2java = new HashMap<String, SqlObjectConverter>();
	Map<String, SqlObjectConverter> java2sql = new HashMap<String, SqlObjectConverter>();

	public SqlObjectConverters() {
		final Boolean BOOLEAN_NULL = false;
		final Integer INTEGER_NULL = 0;
		final Long LONG_NULL = 0L;
		final Short SHORT_NULL = 0;
		final Float FLOAT_NULL = 0.0f;
		final Double DOUBLE_NULL = 0.0;
		// java -> sql:
		registerJavaToSqlConverters(LocalDate.class, (obj) -> {
			if (obj == null) {
				return null;
			}
			if (obj instanceof LocalDate) {
				return java.sql.Date.valueOf((LocalDate) obj);
			}
			throw new IllegalArgumentException("Cannot convert " + obj.getClass().getName() + " to java.sql.Date.");
		});
		registerJavaToSqlConverters(LocalDateTime.class, (obj) -> {
			if (obj == null) {
				return null;
			}
			if (obj instanceof LocalDateTime) {
				return java.util.Date
						.from(((LocalDateTime) obj).toInstant(ZoneOffset.of(ZoneId.systemDefault().getId())));
			}
			throw new IllegalArgumentException("Cannot convert " + obj.getClass().getName() + " to java.sql.Date.");
		});
		// sql -> java:
		registerSqlToJavaConverters(int.class, (obj) -> {
			if (obj == null) {
				return INTEGER_NULL;
			}
			if (obj instanceof Integer) {
				return obj;
			}
			throw new IllegalArgumentException("Cannot convert " + obj.getClass().getName() + " to int.");
		});
		registerSqlToJavaConverters(long.class, (obj) -> {
			if (obj == null) {
				return LONG_NULL;
			}
			if (obj instanceof Long) {
				return obj;
			}
			throw new IllegalArgumentException("Cannot convert " + obj.getClass().getName() + " to long.");
		});
		registerSqlToJavaConverters(float.class, (obj) -> {
			if (obj == null) {
				return FLOAT_NULL;
			}
			if (obj instanceof Float) {
				return obj;
			}
			throw new IllegalArgumentException("Cannot convert " + obj.getClass().getName() + " to float.");
		});
		registerSqlToJavaConverters(double.class, (obj) -> {
			if (obj == null) {
				return DOUBLE_NULL;
			}
			if (obj instanceof Double) {
				return obj;
			}
			throw new IllegalArgumentException("Cannot convert " + obj.getClass().getName() + " to double.");
		});
		registerSqlToJavaConverters(short.class, (obj) -> {
			if (obj == null) {
				return SHORT_NULL;
			}
			if (obj instanceof Short) {
				return obj;
			}
			throw new IllegalArgumentException("Cannot convert " + obj.getClass().getName() + " to short.");
		});
		registerSqlToJavaConverters(boolean.class, (obj) -> {
			if (obj == null) {
				return BOOLEAN_NULL;
			}
			if (obj instanceof Boolean) {
				return obj;
			}
			throw new IllegalArgumentException("Cannot convert " + obj.getClass().getName() + " to boolean.");
		});
		registerSqlToJavaConverters(LocalDate.class, (obj) -> {
			if (obj == null) {
				return null;
			}
			if (obj instanceof LocalDate) {
				return obj;
			}
			if (obj instanceof java.sql.Date) {
				return ((java.sql.Date) obj).toLocalDate();
			}
			if (obj instanceof java.util.Date) {
				return LocalDate.from(((java.util.Date) obj).toInstant());
			}
			throw new IllegalArgumentException(
					"Cannot convert " + obj.getClass().getName() + " to java.time.LocalDate.");
		});
		registerSqlToJavaConverters(LocalTime.class, (obj) -> {
			if (obj == null) {
				return null;
			}
			if (obj instanceof LocalTime) {
				return obj;
			}
			if (obj instanceof java.sql.Time) {
				return ((java.sql.Time) obj).toLocalTime();
			}
			throw new IllegalArgumentException(
					"Cannot convert " + obj.getClass().getName() + " to java.time.LocalTime.");
		});
		registerSqlToJavaConverters(LocalDateTime.class, (obj) -> {
			if (obj == null) {
				return null;
			}
			if (obj instanceof LocalDateTime) {
				return obj;
			}
			if (obj instanceof java.sql.Timestamp) {
				return ((java.sql.Timestamp) obj).toLocalDateTime();
			}
			throw new IllegalArgumentException(
					"Cannot convert " + obj.getClass().getName() + " to java.time.LocalDateTime.");
		});
	}

	public void registerSqlToJavaConverters(Class<?> targetClass, SqlObjectConverter converter) {
		sql2java.put(targetClass.getName(), converter);
	}

	public void registerJavaToSqlConverters(Class<?> targetClass, SqlObjectConverter converter) {
		java2sql.put(targetClass.getName(), converter);
	}

	public Object sqlObjectToJavaObject(Class<?> clazz, Object sqlObject) {
		String name = clazz.getName();
		SqlObjectConverter converter = this.sql2java.get(name);
		if (converter == null) {
			return sqlObject;
		}
		return converter.convert(sqlObject);
	}

	public Object javaObjectToSqlObject(Class<?> clazz, Object javaObject) {
		String name = clazz.getName();
		SqlObjectConverter converter = this.java2sql.get(name);
		if (converter == null) {
			return javaObject;
		}
		return converter.convert(javaObject);
	}
}
