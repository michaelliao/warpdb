package com.itranswarp.warpdb;

import java.util.ArrayList;
import java.util.List;

/**
 * select ... from ... ORDER BY ...
 * 
 * @author liaoxuefeng
 * 
 * @param <T>
 */
public final class OrderBy<T> extends CriteriaQuery<T> {

	public OrderBy(Criteria<T> criteria, String orderBy) {
		super(criteria);
		orderBy(orderBy);
	}

	public OrderBy<T> orderBy(String orderBy) {
		if (criteria.orderBy == null) {
			criteria.orderBy = new ArrayList<>();
		}
		orderBy = checkProperty(orderBy);
		criteria.orderBy.add(orderBy);
		return this;
	}

	String checkProperty(String orderBy) {
		String prop = null;
		String upper = orderBy.toUpperCase();
		if (upper.endsWith(" DESC")) {
			prop = orderBy.substring(0, orderBy.length() - 5).trim();
			return propertyToField(prop) + " DESC";
		} else if (upper.endsWith(" ASC")) {
			prop = orderBy.substring(0, orderBy.length() - 4).trim();
			return propertyToField(prop) + " ASC";
		} else {
			prop = orderBy.trim();
			return propertyToField(prop);
		}
	}

	String propertyToField(String prop) {
		AccessibleProperty ap = this.criteria.mapper.allPropertiesMap.get(prop.toLowerCase());
		if (ap == null) {
			throw new IllegalArgumentException("Invalid property when use order by: " + prop);
		}
		return ap.columnName;
	}

	/**
	 * Make a desc order by.
	 */
	public OrderBy<T> desc() {
		int last = this.criteria.orderBy.size() - 1;
		String s = criteria.orderBy.get(last);
		if (!s.toUpperCase().endsWith(" DESC")) {
			s = s + " DESC";
		}
		criteria.orderBy.set(last, s);
		return this;
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
	 * @return pagedResults
	 */
	public PagedResults<T> list(int pageIndex) {
		return criteria.list(pageIndex, Page.DEFAULT_ITEMS_PER_PAGE);
	}

	/**
	 * Do page query.
	 * 
	 * @param pageIndex
	 * @param itemsPerPage
	 * @return pagedResults
	 */
	public PagedResults<T> list(int pageIndex, int itemsPerPage) {
		return criteria.list(pageIndex, itemsPerPage);
	}

	/**
	 * Get first row of the query, or null if no result found.
	 */
	public T first() {
		return criteria.first();
	}
}
