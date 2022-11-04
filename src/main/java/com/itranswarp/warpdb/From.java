package com.itranswarp.warpdb;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * select ... FROM ...
 * 
 * @author liaoxuefeng
 *
 * @param <T> Generic type.
 */
public final class From<T> extends CriteriaQuery<T> {

    From(Criteria<T> criteria, Mapper<T> mapper) {
        super(criteria);
        this.criteria.mapper = mapper;
        this.criteria.clazz = mapper.entityClass;
        this.criteria.table = mapper.tableName;
        checkSelect();
    }

    void checkSelect() {
        if (this.criteria.select == null) {
            return;
        }
        Map<String, AccessibleProperty> map = this.criteria.mapper.allPropertiesMap;
        this.criteria.select = this.criteria.select.stream().map((prop) -> {
            if ("*".equals(prop)) {
                return "*";
            }
            AccessibleProperty ap = map.get(prop.toLowerCase());
            if (ap == null) {
                throw new IllegalArgumentException("Invalid property in select: " + prop);
            }
            return ap.columnName;
        }).collect(Collectors.toList());
    }

    /**
     * Add where clause.
     * 
     * @param clause Clause like "name = ?".
     * @param args   Arguments to match clause.
     * @return CriteriaQuery object.
     */
    public Where<T> where(String clause, Object... args) {
        return new Where<>(this.criteria, clause, args);
    }

    /**
     * Add order by clause.
     * 
     * @param orderBy Field of order by.
     * @return CriteriaQuery object.
     */
    public OrderBy<T> orderBy(String orderBy) {
        return new OrderBy<>(this.criteria, orderBy);
    }

    /**
     * Add limit clause.
     * 
     * @param maxResults The max results.
     * @return CriteriaQuery object.
     */
    public Limit<T> limit(int maxResults) {
        return limit(0, maxResults);
    }

    /**
     * Add limit clause.
     * 
     * @param offset     The offset.
     * @param maxResults The max results.
     * @return CriteriaQuery object.
     */
    public Limit<T> limit(int offset, int maxResults) {
        return new Limit<>(this.criteria, offset, maxResults);
    }

    /**
     * Get all results as list.
     * 
     * @return List of object T.
     */
    public List<T> list() {
        return this.criteria.list();
    }

    /**
     * Do page query using default items per page.
     * 
     * @param pageIndex Page index, starts from 1.
     * @return pageResult PageResults object.
     */
    public PagedResults<T> list(int pageIndex) {
        return this.criteria.list(pageIndex, Page.DEFAULT_ITEMS_PER_PAGE);
    }

    /**
     * Do page query.
     * 
     * @param pageIndex
     * @param itemsPerPage
     * @return PagedResults object.
     */
    public PagedResults<T> list(int pageIndex, int itemsPerPage) {
        return this.criteria.list(pageIndex, itemsPerPage);
    }

    /**
     * Get count as int.
     * 
     * @return int count
     */
    public int count() {
        return this.criteria.count();
    }

    /**
     * Get first row of the query, or null if no result found.
     * 
     * @return Object T or null.
     */
    public T first() {
        return this.criteria.first();
    }

    /**
     * Get unique result of the query. Exception will throw if no result found or
     * more than 1 results found.
     * 
     * @return T modelInstance
     * @throws jakarta.persistence.NoResultException        If result set is empty.
     * @throws jakarta.persistence.NonUniqueResultException If more than 1 results
     *                                                      found.
     */
    public T unique() {
        return this.criteria.unique();
    }
}
