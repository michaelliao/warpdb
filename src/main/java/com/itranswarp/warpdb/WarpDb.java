package com.itranswarp.warpdb;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.itranswarp.warpdb.util.ClassUtils;

/**
 * A lightweight Spring-JdbcTemplate wrapper using DSL-style operation.
 * 
 * @author liaoxuefeng
 */
public class WarpDb {

	final Log log = LogFactory.getLog(getClass());

	JdbcTemplate jdbcTemplate;

	List<String> basePackages;

	// class -> Mapper:
	Map<Class<?>, Mapper<?>> classMapping;

	// tableName -> Mapper:
	Map<String, Mapper<?>> tableMapping;

	/**
	 * Set DataSource instance.
	 * 
	 * @param dataSource DataSource instance.
	 */
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource, false);
	}

	/**
	 * Set JdbcTemplate instance.
	 * 
	 * @param jdbcTemplate JdbcTemplate instance
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Get JdbcTemplate instance.
	 * 
	 * @return JdbcTemplate instance
	 */
	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
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
			log.info("Found class: " + clazz.getName());
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

	public String getDDL(Class<?> clazz) {
		Mapper<?> mapper = this.classMapping.get(clazz);
		if (mapper == null) {
			throw new PersistenceException("Cannot find entity class: " + clazz.getName());
		}
		return mapper.ddl();
	}

	/**
	 * Export schema as DDL.
	 * 
	 * @return DDL as SQL string.
	 */
	public String exportSchema() {
		return String.join("\n\n", this.tableMapping.values().stream().map((mapper) -> {
			return mapper.ddl();
		}).toArray(String[]::new));
	}

	/**
	 * Get a model instance by class type and id. EntityNotFoundException is thrown
	 * if not found.
	 * 
	 * @param       <T> Generic type.
	 * @param clazz Entity class.
	 * @param id    Id value.
	 * @return Entity bean found by id.
	 */
	public <T> T get(Class<T> clazz, Serializable id) {
		T t = fetch(clazz, id);
		if (t == null) {
			throw new EntityNotFoundException(clazz.getSimpleName());
		}
		return t;
	}

	/**
	 * Get a model instance by class type and id. Return null if not found.
	 * 
	 * @param       <T> Generic type.
	 * @param clazz Entity class.
	 * @param id    Id value.
	 * @return Entity bean found by id.
	 */
	public <T> T fetch(Class<T> clazz, Serializable id) {
		Mapper<T> mapper = getMapper(clazz);
		log.debug("SQL: " + mapper.selectSQL);
		List<T> list = (List<T>) jdbcTemplate.query(mapper.selectSQL, new Object[] { id }, mapper.rowMapper);
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

	/**
	 * Remove bean by id.
	 * 
	 * @param bean The entity.
	 */
	public <T> void remove(T bean) {
		try {
			Mapper<?> mapper = getMapper(bean.getClass());
			mapper.preRemove.invoke(bean);
			log.debug("SQL: " + mapper.deleteSQL);
			jdbcTemplate.update(mapper.deleteSQL, mapper.id.convertGetter.get(bean));
			mapper.postRemove.invoke(bean);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Remove beans by id.
	 * 
	 * @param beans The entities.
	 */
	public <T> void remove(List<T> beans) {
		if (beans.isEmpty()) {
			return;
		}
		try {
			for (Object bean : beans) {
				Mapper<?> mapper = getMapper(bean.getClass());
				mapper.preRemove.invoke(bean);
				log.debug("SQL: " + mapper.deleteSQL);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Select selectForUpdate(String... selectFields) {
		Criteria c = new Criteria(this);
		c.forUpdate = true;
		return new Select(c, selectFields);
	}

	public <T> From<T> from(Class<T> entityClass) {
		Mapper<T> mapper = getMapper(entityClass);
		return new From<>(new Criteria<>(this), mapper);
	}

	/**
	 * Update entity's updatable properties by id.
	 * 
	 * @param      <T> Generic type.
	 * @param bean Entity object.
	 */
	public <T> void update(T bean) {
		try {
			Mapper<?> mapper = getMapper(bean.getClass());
			mapper.preUpdate.invoke(bean);
			Object[] args = new Object[mapper.updatableProperties.size() + 1];
			int n = 0;
			for (AccessibleProperty prop : mapper.updatableProperties) {
				args[n] = prop.convertGetter.get(bean);
				n++;
			}
			args[n] = mapper.id.getter.get(bean);
			log.debug("SQL: " + mapper.updateSQL);
			jdbcTemplate.update(mapper.updateSQL, args);
			mapper.postUpdate.invoke(bean);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Update entities' updatable properties by id.
	 * 
	 * @param       <T> Generic type.
	 * @param beans List of objects.
	 */
	public <T> void update(List<T> beans) {
		if (beans.isEmpty()) {
			return;
		}
		Mapper<?> mapper = getMapper(beans.iterator().next().getClass());
		jdbcTemplate.execute(new ConnectionCallback<Object>() {
			@Override
			public Object doInConnection(Connection con) throws SQLException, DataAccessException {
				try (PreparedStatement ps = con.prepareStatement(mapper.updateSQL)) {
					for (T bean : beans) {
						mapper.preUpdate.invoke(bean);
						int n = 0;
						for (AccessibleProperty prop : mapper.updatableProperties) {
							Object arg = prop.convertGetter.get(bean);
							n++;
							ps.setObject(n, arg);
						}
						n++;
						ps.setObject(n, mapper.id.getter.get(bean)); // where id = ?
						ps.addBatch();
					}
					ps.executeBatch();
					for (T bean : beans) {
						mapper.postUpdate.invoke(bean);
					}
					return null;
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	/**
	 * Update entity's specified properties.
	 * 
	 * @param            <T> Generic type.
	 * @param bean       Entity object.
	 * @param properties Properties' names.
	 */
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
				AccessibleProperty ap = mapper.updatablePropertiesMap.get(prop.toLowerCase());
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
			log.debug("SQL: " + sql);
			jdbcTemplate.update(sql, args);
			mapper.postUpdate.invoke(bean);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Alias of insert().
	 * 
	 * @param      <T> Generic type.
	 * @param beans List of objects.
	 */
	@Deprecated
	public <T> void save(List<T> beans) {
		insert(beans);
	}

	/**
	 * Persist entity objects.
	 * 
	 * @param       <T> Generic type.
	 * @param beans List of objects.
	 */
	public <T> void insert(List<T> beans) {
		if (beans.isEmpty()) {
			return;
		}
		doInsert(getMapper(beans.iterator().next().getClass()), false, beans);
	}

	/**
	 * Persist entity objects, ignore exist rows.
	 * 
	 * @param       <T> Generic type.
	 * @param beans List of objects.
	 */
	public <T> void insertIgnore(List<T> beans) {
		if (beans.isEmpty()) {
			return;
		}
		doInsert(getMapper(beans.iterator().next().getClass()), true, beans);
	}

	private <T> void doInsert(Mapper<?> mapper, boolean ignore, List<T> beans) {
		final String sql = ignore ? mapper.insertIgnoreSQL : mapper.insertSQL;
		log.debug("SQL: " + sql);
		try {
			jdbcTemplate.execute(new ConnectionCallback<Object>() {
				@Override
				public Object doInConnection(Connection con) throws SQLException, DataAccessException {
					try (PreparedStatement ps = mapper.id.isIdentityId()
							? con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
							: con.prepareStatement(sql)) {
						for (T bean : beans) {
							mapper.prePersist.invoke(bean);
							int n = 0;
							for (AccessibleProperty prop : mapper.insertableProperties) {
								Object arg = prop.convertGetter.get(bean);
								n++;
								ps.setObject(n, arg);
							}
							ps.addBatch();
						}
						int[] results = ps.executeBatch();
						if (mapper.id.isIdentityId()) {
							// using identityId:
							try (ResultSet rs = ps.getGeneratedKeys()) {
								int n = 0;
								for (T bean : beans) {
									int result = results[n];
									n++;
									if (result == Statement.RETURN_GENERATED_KEYS) {
										if (rs.next()) {
											mapper.id.convertSetter.set(bean, rs.getObject(1));
										} else {
											throw new RuntimeException("Not enough id returned.");
										}
									}
								}
							}
						}
						return null;
					} catch (IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			});
			for (T bean : beans) {
				mapper.postPersist.invoke(bean);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Alias of insert().
	 * 
	 * @param      <T> Generic type.
	 * @param bean Entity object.
	 */
	@Deprecated
	public <T> void save(T bean) {
		insert(bean);
	}

	/**
	 * Persist entity object.
	 * 
	 * @param      <T> Generic type.
	 * @param bean Entity object.
	 */
	public <T> void insert(T bean) {
		doInsert(false, bean);
	}

	/**
	 * Persist entity object. Ignore if exist. Return true if insert ok, otherwise
	 * ignored.
	 * 
	 * @param      <T> Generic type.
	 * @param bean Entity object.
	 */
	public <T> boolean insertIgnore(T bean) {
		return doInsert(true, bean);
	}

	private <T> boolean doInsert(boolean ignore, T bean) {
		try {
			int rows;
			final Mapper<?> mapper = getMapper(bean.getClass());
			final String sql = ignore ? mapper.insertIgnoreSQL : mapper.insertSQL;
			mapper.prePersist.invoke(bean);
			Object[] args = new Object[mapper.insertableProperties.size()];
			int n = 0;
			for (AccessibleProperty prop : mapper.insertableProperties) {
				args[n] = prop.convertGetter.get(bean);
				n++;
			}
			log.debug("SQL: " + sql);
			if (mapper.id.isIdentityId()) {
				// using identityId:
				KeyHolder keyHolder = new GeneratedKeyHolder();
				rows = jdbcTemplate.update(new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
						for (int i = 0; i < args.length; i++) {
							ps.setObject(i + 1, args[i]);
						}
						return ps;
					}
				}, keyHolder);
				if (rows == 1) {
					mapper.id.convertSetter.set(bean, keyHolder.getKey());
				}
			} else {
				// id is specified:
				rows = jdbcTemplate.update(sql, args);
			}
			mapper.postPersist.invoke(bean);
			return rows == 1;
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Execute update SQL.
	 * 
	 * @param sql  The update SQL.
	 * @param args The arguments that match the SQL.
	 * @return int result of update.
	 */
	public int updateSql(String sql, Object... args) {
		return jdbcTemplate.update(sql, args);
	}

	/**
	 * Execute query.
	 * 
	 * @param      <T> Generic type.
	 * @param sql  The select SQL.
	 * @param args The arguments that match the SQL.
	 * @return List of object T.
	 */
	public <T> List<T> list(String sql, Object... args) {
		log.debug("SQL: " + sql);
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
	 * @param       <T> Generic type.
	 * @param clazz Entity class.
	 * @param sql   Raw SQL.
	 * @param args  Arguments.
	 * @return List of entities.
	 */
	public <T> List<T> list(Class<T> clazz, String sql, Object... args) {
		log.debug("SQL: " + sql);
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

	/**
	 * Query for list by JdbcTemplate's RowMapper.
	 * 
	 * @param rowMapper Spring JdbcTemplate's RowMapper.
	 * @param sql       Raw SQL.
	 * @param args      Arguments.
	 * @return List of entities.
	 */
	public <T> List<T> queryForList(RowMapper<T> rowMapper, String sql, Object... args) {
		log.debug("SQL: " + sql);
		return jdbcTemplate.query(sql, args, rowMapper);
	}

	public Optional<Number> queryForNumber(String sql, Object... args) {
		log.debug("SQL: " + sql);
		Number number = jdbcTemplate.query(sql, args, NUMBER_RESULT_SET);
		return Optional.ofNullable(number);
	}

	public OptionalLong queryForLong(String sql, Object... args) {
		log.debug("SQL: " + sql);
		Number number = jdbcTemplate.query(sql, args, NUMBER_RESULT_SET);
		if (number == null) {
			return OptionalLong.empty();
		}
		return OptionalLong.of(number.longValue());
	}

	public OptionalInt queryForInt(String sql, Object... args) {
		log.debug("SQL: " + sql);
		Number number = jdbcTemplate.query(sql, args, NUMBER_RESULT_SET);
		if (number == null) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(number.intValue());
	}

	/**
	 * Get unique object.
	 * 
	 * @param      <T> Generic type.
	 * @param sql  The SQL.
	 * @param args The arguments.
	 * @return Object.
	 */
	public <T> T unique(String sql, Object... args) {
		if (sql.toLowerCase().indexOf(" limit ") == (-1)) {
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
		if (sql.toLowerCase().indexOf(" limit ") == (-1)) {
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

	/**
	 * Get entity classes as List.
	 *
	 * @return List of entity classes.
	 */
	public List<Class<?>> getEntities() {
		return new ArrayList<>(this.classMapping.keySet());
	}

	/**
	 * Get table name by entity class.
	 * 
	 * @param clazz Entity class.
	 * @return Table name.
	 */
	public String getTable(Class<?> clazz) {
		Mapper<?> mapper = (Mapper<?>) this.classMapping.get(clazz);
		if (mapper == null) {
			throw new RuntimeException("Target class is not a registered entity: " + clazz.getName());
		}
		return mapper.tableName;
	}

	// get mapper by class:
	@SuppressWarnings("unchecked")
	<T> Mapper<T> getMapper(Class<T> clazz) {
		Mapper<T> mapper = (Mapper<T>) this.classMapping.get(clazz);
		if (mapper == null) {
			throw new RuntimeException("Target class is not a registered entity: " + clazz.getName());
		}
		return mapper;
	}

	// get Mapper from SQL like "select * from abc where ..."
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

	static final RowMapper<Number> NUMBER_ROW_MAPPER = new RowMapper<Number>() {
		@Override
		public Number mapRow(ResultSet rs, int rowNum) throws SQLException {
			return (Number) rs.getObject(1);
		}
	};

	static final ResultSetExtractor<Number> NUMBER_RESULT_SET = new ResultSetExtractor<Number>() {
		@Override
		public Number extractData(ResultSet rs) throws SQLException, DataAccessException {
			if (rs.next()) {
				return (Number) rs.getObject(1);
			}
			return null;
		}
	};
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
