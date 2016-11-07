package com.itranswarp.warpdb;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.itranswarp.warpdb.util.ClassUtils;

/**
 * A Spring-JdbcTemplate wrapper.
 * 
 * @author Michael
 */
public class WarpDb {

	final Log log = LogFactory.getLog(getClass());

	JdbcTemplate jdbcTemplate;

	List<String> basePackages;

	// class -> Mapper:
	Map<Class<?>, Mapper<?>> classMapping;
	// tableName -> Mapper:
	Map<String, Mapper<?>> tableMapping;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource, false);
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setBasePackages(List<String> basePackages) {
		this.basePackages = basePackages;
	}

	@PostConstruct
	public void init() {
		List<Class<?>> classes = ClassUtils
				.scanEntities(this.basePackages.toArray(new String[this.basePackages.size()]));
		Map<Class<?>, Mapper<?>> classMapping = new HashMap<>();
		Map<String, Mapper<?>> tableMapping = new HashMap<>();
		for (Class<?> clazz : classes) {
			Mapper<?> mapper = new Mapper<>(clazz);
			classMapping.put(clazz, mapper);
			if (null != tableMapping.put(mapper.tableName.toLowerCase(), mapper)) {
				throw new ConfigurationException(
						"Duplicate table name: " + mapper.tableName + " defined in class: " + clazz.getName());
			}
		}
		this.classMapping = classMapping;
		this.tableMapping = tableMapping;
	}

	public String exportSchema() {
		return String.join("\n\n", this.tableMapping.values().stream().map((mapper) -> {
			return mapper.ddl();
		}).toArray(String[]::new));
	}

	/**
	 * Get a model instance by class type and id. EntityNotFoundException is
	 * thrown if not found.
	 * 
	 * @param <T>
	 *            Generic type.
	 * @param clazz
	 *            Entity class.
	 * @param id
	 *            Id value.
	 * @return Entity bean found by id.
	 */
	public <T> T get(Class<T> clazz, Serializable id) {
		T t = fetch(clazz, id);
		if (t == null) {
			throw new EntityNotFoundException("Entity not found: " + clazz.getName());
		}
		return t;
	}

	/**
	 * Get a model instance by class type and id. Return null if not found.
	 * 
	 * @param <T>
	 *            Generic type.
	 * @param clazz
	 *            Entity class.
	 * @param id
	 *            Id value.
	 * @return Entity bean found by id.
	 */
	public <T> T fetch(Class<T> clazz, Serializable id) {
		Mapper<T> mapper = getMapper(clazz);
		log.info("SQL: " + mapper.querySQL);
		List<T> list = (List<T>) jdbcTemplate.query(mapper.querySQL, new Object[] { id }, mapper.rowMapper);
		if (list.isEmpty()) {
			return null;
		}
		T t = list.get(0);
		try {
			mapper.postLoad.invoke(t);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
		return t;
	}

	public <T> void remove(@SuppressWarnings("unchecked") T... beans) {
		try {
			for (Object bean : beans) {
				Mapper<?> mapper = getMapper(bean.getClass());
				mapper.preRemove.invoke(bean);
				log.info("SQL: " + mapper.deleteSQL);
				jdbcTemplate.update(mapper.deleteSQL, mapper.id.convertGetter.get(bean));
				mapper.postRemove.invoke(bean);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	public Select select(String... selectFields) {
		return new Select(new Criteria(this), selectFields);
	}

	public <T> From<T> from(Class<T> entityClass) {
		Mapper<T> mapper = getMapper(entityClass);
		return new From<>(new Criteria<>(this), entityClass, mapper.tableName);
	}

	public <T> void update(@SuppressWarnings("unchecked") T... beans) {
		try {
			for (T bean : beans) {
				Mapper<?> mapper = getMapper(bean.getClass());
				mapper.preUpdate.invoke(bean);
				Object[] args = new Object[mapper.updatableProperties.size() + 1];
				int n = 0;
				for (AccessibleProperty prop : mapper.updatableProperties) {
					args[n] = prop.convertGetter.get(bean);
					n++;
				}
				args[n] = mapper.id.getter.get(bean);
				log.info("SQL: " + mapper.updateSQL);
				log.info("ARG :" + Arrays.toString(args));
				jdbcTemplate.update(mapper.updateSQL, args);
				mapper.postUpdate.invoke(bean);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
	}

	public <T> void updateProperties(T bean, String... properties) {
		if (properties.length == 0) {
			throw new IllegalArgumentException("No properties provided.");
		}
		Mapper<?> mapper = getMapper(bean.getClass());
		try {
			mapper.preUpdate.invoke(bean);
			Object[] args = new Object[properties.length + 1];
			StringBuilder sb = new StringBuilder(150);
			sb.append("UPDATE ").append(mapper.tableName).append(" SET ");
			int n = 0;
			for (String prop : properties) {
				AccessibleProperty ap = mapper.updatablePropertiesMap.get(prop);
				if (ap == null) {
					throw new IllegalArgumentException("Property " + prop + " not exist or un-updatable.");
				}
				sb.append(ap.columnName).append(" = ?, ");
				args[n] = ap.convertGetter.get(bean);
				n++;
			}
			args[n] = mapper.id.getter.get(bean);
			sb.delete(sb.length() - 2, sb.length());
			sb.append(" WHERE ").append(mapper.id.columnName).append(" = ?");
			String sql = sb.toString();
			log.info("SQL: " + sql);
			jdbcTemplate.update(sql, args);
			mapper.postUpdate.invoke(bean);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
	}

	public <T> void save(@SuppressWarnings("unchecked") T... beans) {
		try {
			for (T bean : beans) {
				Mapper<?> mapper = getMapper(bean.getClass());
				mapper.prePersist.invoke(bean);
				Object[] args = new Object[mapper.insertableProperties.size()];
				int n = 0;
				for (AccessibleProperty prop : mapper.insertableProperties) {
					args[n] = prop.convertGetter.get(bean);
					n++;
				}
				log.info("SQL: " + mapper.insertSQL);
				jdbcTemplate.update(mapper.insertSQL, args);
				mapper.postPersist.invoke(bean);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
	}

	public int update(String sql, Object... args) {
		return jdbcTemplate.update(sql, args);
	}

	public <T> List<T> list(String sql, Object... args) {
		log.info("SQL: " + sql);
		Mapper<T> mapper = getMapper(sql);
		List<T> list = (List<T>) jdbcTemplate.query(sql, args, mapper.rowMapper);
		try {
			for (T bean : list) {
				mapper.postLoad.invoke(bean);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
		return list;
	}

	/**
	 * Get list of entity.
	 * 
	 * @param clazz
	 *            Entity class.
	 * @param sql
	 *            Raw SQL.
	 * @param args
	 *            Arguments.
	 * @return List of entities.
	 */
	public <T> List<T> list(Class<T> clazz, String sql, Object... args) {
		log.info("SQL: " + sql);
		Mapper<T> mapper = getMapper(clazz);
		List<T> list = (List<T>) jdbcTemplate.query(sql, args, mapper.rowMapper);
		try {
			for (T bean : list) {
				mapper.postLoad.invoke(bean);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
		return list;
	}

	public int queryForInt(String sql, Object... args) {
		log.info("SQL: " + sql);
		Integer num = jdbcTemplate.queryForObject(sql, args, Integer.class);
		return num.intValue();
	}

	public <T> T unique(String sql, Object... args) {
		if (sql.indexOf(" limit ") == (-1)) {
			sql = sql + " limit 2";
		}
		List<T> list = list(sql, args);
		if (list.isEmpty()) {
			throw new NoResultException("Empty result from SQL: " + sql);
		}
		if (list.size() > 1) {
			throw new NonUniqueResultException("Non unique result from SQL: " + sql);
		}
		return list.get(0);
	}

	public <T> T fetch(String sql, Object... args) {
		if (sql.indexOf(" limit ") == (-1)) {
			sql = sql + " limit 2";
		}
		List<T> list = list(sql, args);
		if (list.isEmpty()) {
			return null;
		}
		if (list.size() > 1) {
			throw new NonUniqueResultException("Non unique result returned from SQL: " + sql);
		}
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	<T> Mapper<T> getMapper(Class<T> clazz) {
		Mapper<T> mapper = (Mapper<T>) this.classMapping.get(clazz);
		if (mapper == null) {
			throw new RuntimeException("Target class is not a registered entity: " + clazz.getName());
		}
		return mapper;
	}

	@SuppressWarnings("unchecked")
	<T> Mapper<T> getMapper(String sql) {
		String[] ss = sql.split("\\s+");
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].toLowerCase().equals("from")) {
				if (i < ss.length - 1) {
					String table = ss[i + 1];
					if (table.length() > 2 && table.startsWith("`") && table.endsWith("`")) {
						table = table.substring(1, table.length() - 1);
					}
					Mapper<T> mapper = (Mapper<T>) this.tableMapping.get(table.toLowerCase());
					if (mapper == null) {
						throw new RuntimeException("Cannot find mapping for table: " + table);
					}
					return mapper;
				}
			}
		}
		throw new RuntimeException("Cannot parse entity name from SQL: " + sql);
	}

}

class BeanRowMapper<T> implements RowMapper<T> {

	// columnName -> property:
	final Map<String, AccessibleProperty> mapping;
	final Function<Void, T> constructor;

	BeanRowMapper(Class<T> clazz, List<AccessibleProperty> properties) {
		Map<String, AccessibleProperty> mapping = new HashMap<>();
		for (AccessibleProperty p : properties) {
			mapping.put(p.columnName.toLowerCase(), p);
		}
		this.mapping = mapping;
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			this.constructor = (Void) -> {
				try {
					return constructor.newInstance();
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			};
		} catch (NoSuchMethodException e) {
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
				String name = meta.getColumnName(i);
				AccessibleProperty prop = this.mapping.get(name.toLowerCase());
				if (prop != null) {
					Object value = rs.getObject(i);
					prop.convertSetter.set(bean, value);
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return bean;
	}

}
