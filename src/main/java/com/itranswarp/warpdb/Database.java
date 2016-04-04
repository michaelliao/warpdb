package com.itranswarp.warpdb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.itranswarp.warpdb.context.UserContext;
import com.itranswarp.warpdb.entity.BaseEntity;

/**
 * A Spring-JdbcTemplate wrapper.
 */
public class Database {

	final Log log = LogFactory.getLog(getClass());

	JdbcTemplate jdbcTemplate;

	SqlObjectConverters converters = new SqlObjectConverters();

	List<String> basePackages;

	private Map<String, Mapper<?>> mapping;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setConverters(SqlObjectConverters converters) {
		this.converters = converters;
	}

	public void setBasePackages(List<String> basePackages) {
		this.basePackages = basePackages;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init() {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (String basePackage : basePackages) {
			classes.addAll(new ClassUtil().scan(basePackage, c -> {
				return c.isAnnotationPresent(Entity.class);
			}));
		}
		Map<String, Mapper<?>> map = new HashMap<String, Mapper<?>>();
		for (Class<?> clazz : classes) {
			String name = clazz.getSimpleName().toLowerCase();
			if (map.containsKey(name)) {
				log.error("Duplicate table name: " + name + ", defined in class: " + map.get(name).clazz.getName()
						+ " and " + clazz.getName());
				throw new RuntimeException("Duplicate table name: " + name + " for class: " + clazz.getName());
			}
			map.put(name, new Mapper(clazz, converters));
		}
		this.mapping = map;
	}

	/**
	 * Get a model instance by class type and id. APIEntityNotFoundException is
	 * thrown if not found.
	 * 
	 * @param clazz
	 * @param id
	 * @return modelInstance
	 */
	public <T extends BaseEntity> T get(Class<T> clazz, String id) {
		T t = fetch(clazz, id);
		if (t == null) {
			throw new EntityNotFoundException(clazz.getSimpleName(), "Entity not found.");
		}
		return t;
	}

	public <T extends BaseEntity> T fetch(Class<T> clazz, String id) {
		String name = clazz.getSimpleName();
		Mapper<T> mapper = getMapper(clazz);
		String sql = "select * from " + name + " where id=?";
		log.info("SQL: " + sql);
		List<T> list = (List<T>) jdbcTemplate.query(sql, new Object[] { id }, mapper.rowMapper);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	public void remove(BaseEntity... beans) {
		for (BaseEntity bean : beans) {
			String name = bean.getClass().getSimpleName();
			Mapper<?> mapper = this.mapping.get(name.toLowerCase());
			Map<String, Field> fields = mapper.fields;
			Object idValue = null;
			try {
				idValue = fields.get("id").get(bean);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			log.info("SQL: " + mapper.deleteSQL);
			jdbcTemplate.update(mapper.deleteSQL, idValue);
		}
	}

	/**
	 * Start a select-query.
	 * 
	 * @param clazz
	 * @return Model
	 */
	public <T extends BaseEntity> From<T> from(Class<T> clazz) {
		return new From<T>(this, clazz);
	}

	public void update(BaseEntity... beans) {
		long current = System.currentTimeMillis();
		final String USER_ID = UserContext.getRequiredCurrentUserId();
		for (BaseEntity bean : beans) {
			// set updatedBy and updatedAt:
			bean.updatedAt = current;
			if (bean.updatedBy == null) {
				bean.updatedBy = USER_ID;
			}
			String name = bean.getClass().getSimpleName();
			Mapper<?> mapper = this.mapping.get(name.toLowerCase());
			Map<String, Field> fields = mapper.fields;
			Object[] args = new Object[mapper.updateFields.size() + 1];
			int n = 0;
			try {
				for (String field : mapper.updateFields) {
					Field f = fields.get(field);
					args[n] = this.converters.javaObjectToSqlObject(f.getType(), f.get(bean));
					n++;
				}
				args[n] = fields.get("id").get(bean);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			log.info("SQL: " + mapper.updateSQL);
			jdbcTemplate.update(mapper.updateSQL, args);
		}
	}

	public void updateProperties(BaseEntity bean, String... properties) {
		long current = System.currentTimeMillis();
		final String USER_ID = UserContext.getRequiredCurrentUserId();
		// set updatedBy and updatedAt:
		bean.updatedAt = current;
		if (bean.updatedBy == null) {
			bean.updatedBy = USER_ID;
		}
		String name = bean.getClass().getSimpleName();
		Mapper<?> mapper = this.mapping.get(name.toLowerCase());
		Map<String, Field> fields = mapper.fields;
		Object[] args = new Object[properties.length + 3];

		StringBuilder sb = new StringBuilder(150);
		sb.append("update ").append(name).append(" set ");
		int n = 0;
		try {
			for (String field : properties) {
				Field f = fields.get(field.toLowerCase());
				if (f == null) {
					throw new IllegalArgumentException("Property " + field + " not found.");
				}
				sb.append(field).append(" = ?, ");
				args[n] = this.converters.javaObjectToSqlObject(f.getType(), f.get(bean));
				n++;
			}
			sb.append("updatedBy = ?, updatedAt = ?, version = version + 1 where id = ?");
			args[n] = bean.updatedBy;
			n++;
			args[n] = bean.updatedAt;
			n++;
			args[n] = fields.get("id").get(bean);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		String sql = sb.toString();
		log.info("SQL: " + sql);
		jdbcTemplate.update(sql, args);
	}

	public void save(BaseEntity... beans) {
		long current = System.currentTimeMillis();
		final String USER_ID = UserContext.getRequiredCurrentUserId();
		for (BaseEntity bean : beans) {
			// automatically set primary key:
			if (bean.id == null) {
				bean.id = IdUtil.next();
			}
			// set user:
			if (bean.createdBy == null) {
				bean.createdBy = USER_ID;
			}
			if (bean.updatedBy == null) {
				bean.updatedBy = USER_ID;
			}
			// set time:
			bean.createdAt = current;
			bean.updatedAt = current;
			String name = bean.getClass().getSimpleName();
			Mapper<?> mapper = this.mapping.get(name.toLowerCase());
			Map<String, Field> fields = mapper.fields;
			Object[] args = new Object[mapper.insertFields.size()];
			int n = 0;
			try {
				for (String field : mapper.insertFields) {
					Field f = fields.get(field);
					args[n] = this.converters.javaObjectToSqlObject(f.getType(), f.get(bean));
					n++;
				}
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			log.info("SQL: " + mapper.insertSQL);
			jdbcTemplate.update(mapper.insertSQL, args);
		}
	}

	public int update(String sql, Object... args) {
		return jdbcTemplate.update(sql, args);
	}

	public <T extends BaseEntity> List<T> list(String sql, Object... args) {
		log.info("SQL: " + sql);
		Mapper<T> mapper = getMapper(sql);
		List<T> results = (List<T>) jdbcTemplate.query(sql, args, mapper.rowMapper);
		return Collections.unmodifiableList(results);
	}

	public <T extends BaseEntity> List<T> list(Class<T> clazz, String sql, Object... args) {
		log.info("SQL: " + sql);
		Mapper<T> mapper = getMapper(clazz);
		List<T> results = (List<T>) jdbcTemplate.query(sql, args, mapper.rowMapper);
		return Collections.unmodifiableList(results);
	}

	public int queryForInt(String sql, Object... args) {
		log.info("SQL: " + sql);
		Integer num = jdbcTemplate.queryForObject(sql, args, Integer.class);
		return num.intValue();
	}

	public <T extends BaseEntity> T unique(String sql, Object... args) {
		if (sql.indexOf(" limit ") == (-1)) {
			sql = sql + " limit 2";
		}
		List<T> list = list(sql, args);
		if (list.isEmpty()) {
			throw new RuntimeException();
		}
		if (list.size() > 1) {
			throw new RuntimeException();
		}
		return list.get(0);
	}

	public <T extends BaseEntity> T fetch(String sql, Object... args) {
		if (sql.indexOf(" limit ") == (-1)) {
			sql = sql + " limit 2";
		}
		List<T> list = list(sql, args);
		if (list.isEmpty()) {
			return null;
		}
		if (list.size() > 1) {
			throw new RuntimeException();
		}
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	<T extends BaseEntity> Mapper<T> getMapper(Class<T> clazz) {
		String name = clazz.getSimpleName();
		return (Mapper<T>) this.mapping.get(name.toLowerCase());
	}

	@SuppressWarnings("unchecked")
	<T extends BaseEntity> Mapper<T> getMapper(String sql) {
		String[] ss = sql.split("\\s+");
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].toLowerCase().equals("from")) {
				if (i < ss.length - 1) {
					String table = ss[i + 1];
					if (table.length() > 2 && table.startsWith("`") && table.endsWith("`")) {
						table = table.substring(1, table.length() - 1);
					}
					return (Mapper<T>) this.mapping.get(table.toLowerCase());
				}
			}
		}
		throw new RuntimeException("Cannot find entity.");
	}

}

class Mapper<T> {
	final Class<T> clazz;
	final Map<String, Field> fields;
	final BeanRowMapper<T> rowMapper;
	final String insertSQL;
	final String updateSQL;
	final String deleteSQL;
	final List<String> insertFields;
	final List<String> updateFields;

	public Mapper(Class<T> clazz, SqlObjectConverters converters) {
		super();
		this.clazz = clazz;
		this.fields = getAllFields(clazz);
		this.rowMapper = new BeanRowMapper<T>(clazz, fields, converters);
		this.insertFields = new ArrayList<String>(fields.keySet());
		this.insertSQL = buildInsertSQL(clazz, this.insertFields);
		this.updateFields = getUpdateFields(this.fields);
		this.updateSQL = buildUpdateSQL(clazz, this.updateFields);
		this.deleteSQL = buildDeleteSQL(clazz);
	}

	String buildDeleteSQL(Class<T> clazz) {
		return new StringBuilder().append("delete from ").append(clazz.getSimpleName()).append(" where id = ?")
				.toString();
	}

	List<String> getUpdateFields(Map<String, Field> allFields) {
		List<String> updates = allFields.keySet().stream().filter((s) -> {
			if ("id".equals(s)) {
				return false;
			}
			Field f = allFields.get(s);
			if (f.isAnnotationPresent(Column.class)) {
				return f.getAnnotation(Column.class).updatable();
			}
			return true;
		}).collect(Collectors.toList());
		// add "id" at last for "where id = ?"
		List<String> all = new ArrayList<String>(updates);
		all.add("id");
		return all;
	}

	String buildUpdateSQL(Class<T> clazz, List<String> fields) {
		return new StringBuilder().append("update ").append(clazz.getSimpleName()).append(" set ")
				.append(String.join(", ", fields.stream().map((s) -> {
					return s + " = ?";
				}).collect(Collectors.toList()))).append(" where id = ?").toString();
	}

	String buildInsertSQL(Class<T> clazz, List<String> fields) {
		return new StringBuilder().append("insert into ").append(clazz.getSimpleName()).append(" (")
				.append(String.join(", ", fields)).append(") values (")
				.append(String.join(", ", fields.stream().map((s) -> {
					return "?";
				}).collect(Collectors.toList()))).append(")").toString();
	}

	Map<String, Field> getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();
		addFields(clazz, fields);
		Map<String, Field> map = new HashMap<String, Field>();
		for (Field f : fields) {
			if (!f.isAnnotationPresent(Transient.class) && !Modifier.isStatic(f.getModifiers())
					&& !Modifier.isFinal(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())) {
				f.setAccessible(true);
				map.put(f.getName().toLowerCase(), f);
			}
		}
		return map;
	}

	void addFields(Class<?> clazz, List<Field> fields) {
		if (clazz.equals(Object.class)) {
			return;
		}
		addFields(clazz.getSuperclass(), fields);
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
	}
}

class BeanRowMapper<T> implements RowMapper<T> {

	static final Set<String> SUPPORTED_TYPES = new HashSet<String>(Arrays.asList(boolean.class.getName(),
			Boolean.class.getName(), short.class.getName(), Short.class.getName(), int.class.getName(),
			Integer.class.getName(), long.class.getName(), Long.class.getName(), float.class.getName(),
			Float.class.getName(), double.class.getName(), Double.class.getName(), String.class.getName(),
			LocalDate.class.getName(), LocalTime.class.getName(), LocalDateTime.class.getName()));

	final Map<String, Field> fields;
	final Function<T, T> constructor;
	final SqlObjectConverters converters;

	BeanRowMapper(Class<T> clazz, Map<String, Field> fields, SqlObjectConverters converters) {
		this.fields = fields;
		this.converters = converters;
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			this.constructor = (t) -> {
				try {
					return constructor.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		T bean = this.constructor.apply(null);
		ResultSetMetaData meta = rs.getMetaData();
		int count = meta.getColumnCount();
		try {
			for (int i = 1; i <= count; i++) {
				Object obj = rs.getObject(i);
				String name = meta.getColumnName(i);
				Field f = this.fields.get(name.toLowerCase());
				if (f != null) {
					f.set(bean, this.converters.sqlObjectToJavaObject(f.getType(), obj));
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return bean;
	}

}
