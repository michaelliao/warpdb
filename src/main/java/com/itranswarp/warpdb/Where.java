package com.itranswarp.warpdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.AttributeConverter;

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
		this.criteria.where = new ArrayList<>();
		this.criteria.whereParams = new ArrayList<>();
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
		Mapper<T> mapper = this.criteria.mapper;
		CompiledClause cc = CompiledClause.compile(mapper, clause);
		if (cc.converters.length != params.length) {
			throw new IllegalArgumentException("Arguments not match the placeholder.");
		}
		// convert params:
		int n = 0;
		for (AttributeConverter<Object, Object> converter : cc.converters) {
			if (converter != null) {
				params[n] = converter.convertToDatabaseColumn(params[n]);
			}
			n++;
		}
		// add:
		if (type != null) {
			this.criteria.where.add(type);
		}
		this.criteria.where.add(cc.clause);
		for (Object param : params) {
			this.criteria.whereParams.add(param);
		}
		return this;
	}

	static final Pattern WORD_PATTERN = Pattern.compile("[^a-zA-Z0-9\\_]+");

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
		return new OrderBy<>(this.criteria, orderBy);
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
	 */
	public T first() {
		return this.criteria.first();
	}

	/**
	 * Get unique result of the query. Exception will throw if no result found
	 * or more than 1 results found.
	 * 
	 * @return T modelInstance
	 * @throws javax.persistence.NoResultException
	 *             If result set is empty.
	 * @throws javax.persistence.NonUniqueResultException
	 *             If more than 1 results found.
	 */
	public T unique() {
		return this.criteria.unique();
	}
}

class CompiledClause {

	static final Map<String, CompiledClause> CACHE = new ConcurrentHashMap<>();

	static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("and", "or", "like", "in", "is", "not"));

	static final Pattern p = Pattern.compile("[a-z\\_][a-z0-9\\_]*");

	final String clause;
	final AttributeConverter<Object, Object>[] converters;

	CompiledClause(String clause, AttributeConverter<Object, Object>[] converters) {
		this.clause = clause;
		this.converters = converters;
	}

	static CompiledClause compile(Mapper<?> mapper, String clause) {
		String key = mapper.entityClass.getName() + "\n" + clause;
		CompiledClause cc = CACHE.get(key);
		if (cc == null) {
			cc = doCompile(mapper, clause);
			CACHE.put(key, cc);
		}
		return cc;
	}

	@SuppressWarnings("unchecked")
	static CompiledClause doCompile(Mapper<?> mapper, String clause) {
		Map<String, AccessibleProperty> properties = mapper.allPropertiesMap;
		StringBuilder sb = new StringBuilder(clause.length() + 10);
		List<AttributeConverter<Object, Object>> list = new ArrayList<>();
		int start = 0;
		Matcher m = p.matcher(clause.toLowerCase());
		while (m.find()) {
			sb.append(clause.substring(start, m.start()));
			String s = clause.substring(m.start(), m.end());
			if (properties.containsKey(s.toLowerCase())) {
				AccessibleProperty ap = properties.get(s.toLowerCase());
				sb.append(ap.columnName);
				list.add(ap.converter);
			} else {
				if (s.toLowerCase().equals("between")) {
					list.add(list.get(list.size() - 1));
				} else if (s.toLowerCase().equals("null")) {
					list.remove(list.size() - 1);
				} else {
					if (!KEYWORDS.contains(s.toLowerCase())) {
						throw new IllegalArgumentException("Invalid string \"" + s + "\" found in clause: " + clause);
					}
				}
				sb.append(s);
			}
			start = m.end();
		}
		sb.append(clause.substring(start));
		if (list.size() != numOfPlaceholder(clause)) {
			throw new IllegalArgumentException("Invalid number of placeholder.");
		}
		return new CompiledClause(sb.toString(), list.toArray(new AttributeConverter[0]));
	}

	static int numOfPlaceholder(String s) {
		int n = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '?') {
				n++;
			}
		}
		return n;
	}
}
