package com.itranswarp.warpdb;

import java.util.List;

import com.itranswarp.warpdb.entity.BaseEntity;

public final class From<T extends BaseEntity> {

	final SelectInfo<T> info;

	From(Database database, Class<T> clazz) {
		this.info = new SelectInfo<T>(database, clazz);
	}

	public Where<T> where(String clause, Object... args) {
		return new Where<T>(this.info, clause, args);
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
	 * @return pageResult
	 */
	public PagedResults<T> list(int pageIndex) {
		return info.list(pageIndex, Page.DEFAULT_ITEMS_PER_PAGE);
	}

	/**
	 * Do page query.
	 * 
	 * @param pageIndex
	 * @param itemsPerPage
	 * @return
	 */
	public PagedResults<T> list(int pageIndex, int itemsPerPage) {
		return info.list(pageIndex, itemsPerPage);
	}

	/**
	 * Get count as int.
	 * 
	 * @return int count
	 */
	public int count() {
		return info.count();
	}

	/**
	 * Get first row of the query, or null if no result found.
	 */
	public T first() {
		return info.first();
	}

	/**
	 * Get unique result of the query. Exception will throw if no result found
	 * or more than 1 results found.
	 * 
	 * @return T modelInstance
	 */
	public T unique() {
		return info.unique();
	}
}
