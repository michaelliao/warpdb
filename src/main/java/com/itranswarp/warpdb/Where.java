package com.itranswarp.warpdb;

import java.util.ArrayList;
import java.util.List;

import com.itranswarp.warpdb.entity.BaseEntity;

public final class Where<T extends BaseEntity> {

	final SelectInfo<T> info;

	Where(SelectInfo<T> info, String clause, Object... params) {
		this.info = info;
		info.where = new ArrayList<String>();
		info.whereParams = new ArrayList<Object>();
		append(null, clause, params);
	}

	public Where<T> and(String clause, Object... params) {
		append("AND", clause, params);
		return this;
	}

	public Where<T> or(String clause, Object... params) {
		append("OR", clause, params);
		return this;
	}

	Where<T> append(String type, String clause, Object... params) {
		if (type != null) {
			info.where.add(type);
		}
		info.where.add(clause);
		for (Object param : params) {
			info.whereParams.add(param);
		}
		return this;
	}

	public OrderBy<T> orderBy(String orderBy) {
		return new OrderBy<T>(this.info, orderBy);
	}

	/**
	 * Get all results as list.
	 * 
	 * @return list.
	 */
	public List<T> list() {
		return info.list();
	}

	/**
	 * Do page query using default items per page.
	 * 
	 * @param pageIndex
	 *            page index
	 * @return pagedResults
	 */
	public PagedResults<T> list(int pageIndex) {
		return info.list(pageIndex, Page.DEFAULT_ITEMS_PER_PAGE);
	}

	/**
	 * Do page query.
	 * 
	 * @param pageIndex
	 *            page index
	 * @param itemsPerPage
	 *            per page number
	 * @return pagedResults
	 */
	public PagedResults<T> list(int pageIndex, int itemsPerPage) {
		return info.list(pageIndex, itemsPerPage);
	}

	/**
	 * Get count as int.
	 * 
	 * @return count
	 */
	public int count() {
		return info.count();
	}

	/**
	 * Get first row of the query, or null if no result found.
	 * 
	 * @return first model instance
	 */
	public T first() {
		return info.first();
	}

	/**
	 * Get unique result of the query. Exception will throw if no result found
	 * or more than 1 results found.
	 * 
	 * @return modelInstance
	 */
	public T unique() {
		return info.unique();
	}
}
