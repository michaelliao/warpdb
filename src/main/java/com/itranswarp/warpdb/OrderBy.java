package com.itranswarp.warpdb;

import java.util.ArrayList;
import java.util.List;

/**
 * select ... from ... ORDER BY ...
 * 
 * @author liaoxuefeng
 * 
 * @param <T> Generic type.
 */
public final class OrderBy<T> extends CriteriaQuery<T> {

    public OrderBy(Criteria<T> criteria, String orderBy) {
        super(criteria);
        orderBy(orderBy);
    }

    /**
     * Order by field name.
     * 
     * @param orderBy The field name.
     * @return Criteria query object.
     */
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
     * 
     * @return Criteria query object.
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

    /**
     * Add limit clause.
     * 
     * @param maxResults The max results.
     * @return Criteria query object.
     */
    public Limit<T> limit(int maxResults) {
        return limit(0, maxResults);
    }

    /**
     * Add limit clause.
     * 
     * @param offset     Offset.
     * @param maxResults The max results.
     * @return Criteria query object.
     */
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
     * @param pageIndex Page index, starts from 1.
     * @return pagedResults PagedResults object.
     */
    public PagedResults<T> list(int pageIndex) {
        return criteria.list(pageIndex, Page.DEFAULT_ITEMS_PER_PAGE);
    }

    /**
     * Do page query.
     * 
     * @param pageIndex    Page index, starts from 1.
     * @param itemsPerPage Page size.
     * @return pagedResults PagedResults object.
     */
    public PagedResults<T> list(int pageIndex, int itemsPerPage) {
        return criteria.list(pageIndex, itemsPerPage);
    }

    /**
     * Get first row of the query, or null if no result found.
     * 
     * @return Object T or null.
     */
    public T first() {
        return criteria.first();
    }
}
