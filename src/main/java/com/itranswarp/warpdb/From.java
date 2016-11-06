package com.itranswarp.warpdb;

import java.util.List;

/**
 * select ... FROM ...
 * 
 * @author liaoxuefeng
 *
 * @param <T>
 */
public final class From<T> extends CriteriaQuery<T> {

	From(Criteria<T> criteria, Class<T> clazz, String table) {
		super(criteria);
		this.criteria.clazz = clazz;
		this.criteria.table = table;
	}

	/**
	 * Add where clause.
	 * 
	 * @param clause
	 *            clause like "name = ?".
	 * @param args
	 *            Arguments to match clause.
	 * @return CriteriaQuery object.
	 */
	public Where<T> where(String clause, Object... args) {
		return new Where<T>(this.criteria, clause, args);
	}

	public OrderBy<T> orderBy(String orderBy) {
		return new OrderBy<T>(this.criteria, orderBy);
	}

	public Limit<T> limit(int maxResults) {
		return limit(0, maxResults);
	}

	public Limit<T> limit(int offset, int maxResults) {
		return new Limit<>(this.criteria, offset, maxResults);
	}

	/**
	 * Get all results as list.
	 * 
	 * @return list.
	 */
	public List<T> list() {
		return criteria.list();
	}

	/**
	 * Do page query using default items per page.
	 * 
	 * @param pageIndex
	 * @return pageResult
	 */
	public PagedResults<T> list(int pageIndex) {
		return criteria.list(pageIndex, Page.DEFAULT_ITEMS_PER_PAGE);
	}

	/**
	 * Do page query.
	 * 
	 * @param pageIndex
	 * @param itemsPerPage
	 * @return
	 */
	public PagedResults<T> list(int pageIndex, int itemsPerPage) {
		return criteria.list(pageIndex, itemsPerPage);
	}

	/**
	 * Get count as int.
	 * 
	 * @return int count
	 */
	public int count() {
		return criteria.count();
	}

	/**
	 * Get first row of the query, or null if no result found.
	 */
	public T first() {
		return criteria.first();
	}

	/**
	 * Get unique result of the query. Exception will throw if no result found
	 * or more than 1 results found.
	 * 
	 * @return T modelInstance
	 */
	public T unique() {
		return criteria.unique();
	}
}
