package com.itranswarp.warpdb;

import java.util.ArrayList;
import java.util.List;

public final class Where<T> extends CriteriaQuery<T> {

	Where(Criteria<T> criteria, String clause, Object... params) {
		super(criteria);
		this.criteria.where = new ArrayList<String>();
		this.criteria.whereParams = new ArrayList<Object>();
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
			this.criteria.where.add(type);
		}
		this.criteria.where.add(clause);
		for (Object param : params) {
			this.criteria.whereParams.add(param);
		}
		return this;
	}

	public Limit<T> limit(int maxResults) {
		return limit(0, maxResults);
	}

	public Limit<T> limit(int offset, int maxResults) {
		return new Limit<>(this.criteria, offset, maxResults);
	}

	public OrderBy<T> orderBy(String orderBy) {
		return new OrderBy<T>(this.criteria, orderBy);
	}

	/**
	 * Get all results as list.
	 * 
	 * @return list.
	 */
	public List<T> list() {
		return this.criteria.list();
	}

	/**
	 * Do page query using default items per page.
	 * 
	 * @param pageIndex
	 *            page index
	 * @return pagedResults
	 */
	public PagedResults<T> list(int pageIndex) {
		return this.criteria.list(pageIndex, Page.DEFAULT_ITEMS_PER_PAGE);
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
		return this.criteria.list(pageIndex, itemsPerPage);
	}

	/**
	 * Get count as int.
	 * 
	 * @return count
	 */
	public int count() {
		return this.criteria.count();
	}

	/**
	 * Get first row of the query, or null if no result found.
	 * 
	 * @return first model instance
	 */
	public T first() {
		return this.criteria.first();
	}

	/**
	 * Get unique result of the query. Exception will throw if no result found
	 * or more than 1 results found.
	 * 
	 * @return modelInstance
	 */
	public T unique() {
		return this.criteria.unique();
	}
}
