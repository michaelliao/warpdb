package com.itranswarp.warpdb;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * select ... from ... WHERE ...
 * 
 * @author liaoxuefeng
 * 
 * @param <T>
 */
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
		// check clause:
		int n = 0;
		for (int i = 0; i < clause.length(); i++) {
			if (clause.charAt(i) == '?') {
				n++;
			}
		}
		if (n != params.length) {
			throw new IllegalArgumentException("Arguments not match the placeholder.");
		}
		// convert params:
		Mapper<T> mapper = this.criteria.mapper;
		String[] names = extractWords(clause);
		n = 0;
		for (String name : names) {
			AccessibleProperty ap = mapper.allPropertiesMap.get(name.toLowerCase());
			if (ap != null) {
				if (ap.converter != null) {
					params[n] = ap.converter.convertToDatabaseColumn(params[n]);
				}
				n++;
			}
		}
		// add:
		if (type != null) {
			this.criteria.where.add(type);
		}
		this.criteria.where.add(clause);
		for (Object param : params) {
			this.criteria.whereParams.add(param);
		}
		return this;
	}

	static Pattern WORD_PATTERN = Pattern.compile("[^a-zA-Z0-9\\_]+");

	static String[] extractWords(String s) {
		String ss = WORD_PATTERN.matcher(s).replaceAll(" ");
		return ss.split("\\s+");
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
