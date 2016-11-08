package com.itranswarp.warpdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hold criteria query information.
 * 
 * @author Michael Liao
 * 
 * @param <T>
 *            Entity type.
 */
final class Criteria<T> {

	static final Log log = LogFactory.getLog(Criteria.class);

	final WarpDb warpdb;

	Mapper<T> mapper;
	Class<T> clazz;
	List<String> select = null;
	boolean distinct = false;
	String table = null;
	List<String> where = null;
	List<Object> whereParams = null;
	List<String> orderBy = null;
	int offset = 0;
	int maxResults = 0;

	Criteria(WarpDb db) {
		this.warpdb = db;
	}

	String sql(String aggregate) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("SELECT ");
		if (aggregate == null) {
			if (distinct) {
				sb.append("DISTINCT ");
			}
			sb.append((select == null ? "*" : String.join(", ", select)));
		} else {
			sb.append(aggregate);
		}
		sb.append(" FROM ").append(clazz.getSimpleName());
		if (where != null) {
			sb.append(" WHERE ").append(String.join(" ", where));
		}
		if (aggregate == null && orderBy != null) {
			sb.append(" ORDER BY ").append(String.join(", ", orderBy));
		}
		if (aggregate == null && offset >= 0 && maxResults > 0) {
			sb.append(" LIMIT ?, ?");
		}
		String s = sb.toString();
		log.info("SQL: " + s);
		return s;
	}

	Object[] params(String aggregate) {
		List<Object> params = new ArrayList<Object>();
		if (where != null) {
			for (Object obj : whereParams) {
				if (obj == null) {
					params.add(null);
				} else {
					params.add(obj);
				}
			}
		}
		if (aggregate == null && offset >= 0 && maxResults > 0) {
			params.add(offset);
			params.add(maxResults);
		}
		return params.toArray();
	}

	List<T> list() {
		String selectSql = sql(null);
		Object[] selectParams = params(null);
		return warpdb.list(selectSql, selectParams);
	}

	PagedResults<T> list(int pageIndex, int itemsPerPage) {
		if (pageIndex < 1) {
			throw new IllegalArgumentException("Invalid page index.");
		}
		if (itemsPerPage < 1 || itemsPerPage > 1000) {
			throw new IllegalArgumentException("Invalid items per page.");
		}
		String countSql = sql("count(id)");
		Object[] countParams = params("count(id)");
		int totalItems = warpdb.queryForInt(countSql, countParams);
		int totalPages = 0;
		if (totalItems > 0) {
			totalPages = totalItems / itemsPerPage + (totalItems % itemsPerPage > 0 ? 1 : 0);
		}
		Page page = new Page(pageIndex, itemsPerPage, totalPages, totalItems);
		if (totalItems == 0 || pageIndex > totalPages) {
			return new PagedResults<T>(page, Collections.emptyList());
		}
		this.offset = (pageIndex - 1) * itemsPerPage;
		this.maxResults = itemsPerPage;
		String selectSql = sql(null);
		Object[] selectParams = params(null);
		return new PagedResults<T>(page, warpdb.list(selectSql, selectParams));
	}

	int count() {
		String selectSql = sql("count(id)");
		Object[] selectParams = params("count(id)");
		return warpdb.queryForInt(selectSql, selectParams);
	}

	T first() {
		this.offset = 0;
		this.maxResults = 1;
		String selectSql = sql(null);
		Object[] selectParams = params(null);
		List<T> list = warpdb.list(selectSql, selectParams);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	T unique() {
		this.offset = 0;
		this.maxResults = 2;
		String selectSql = sql(null);
		Object[] selectParams = params(null);
		List<T> list = warpdb.list(selectSql, selectParams);
		if (list.isEmpty()) {
			throw new RuntimeException("Expected unique row but nothing found.");
		}
		if (list.size() > 1) {
			throw new RuntimeException("Expected unique row but more than 1 rows found.");
		}
		return list.get(0);
	}
}
