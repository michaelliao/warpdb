package com.itranswarp.warpdb;

import java.util.ArrayList;
import java.util.List;

import com.itranswarp.warpdb.entity.BaseEntity;

public final class OrderBy<T extends BaseEntity> {

	final SelectInfo<T> info;

	public OrderBy(SelectInfo<T> info, String orderBy) {
		this.info = info;
		orderBy(orderBy);
	}

	public OrderBy<T> orderBy(String orderBy) {
		if (info.orderBy == null) {
			info.orderBy = new ArrayList<String>();
		}
		info.orderBy.add(orderBy);
		return this;
	}

	public Limit<T> limit(int maxResults) {
		return limit(0, maxResults);
	}

	public Limit<T> limit(int offset, int maxResults) {
		return new Limit<T>(this.info, offset, maxResults);
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
	 * @return pagedResults
	 */
	public PagedResults<T> list(int pageIndex) {
		return info.list(pageIndex, Page.DEFAULT_ITEMS_PER_PAGE);
	}

	/**
	 * Do page query.
	 * 
	 * @param pageIndex
	 * @param itemsPerPage
	 * @return pagedResults
	 */
	public PagedResults<T> list(int pageIndex, int itemsPerPage) {
		return info.list(pageIndex, itemsPerPage);
	}

	/**
	 * Get first row of the query, or null if no result found.
	 */
	public T first() {
		return info.first();
	}
}
