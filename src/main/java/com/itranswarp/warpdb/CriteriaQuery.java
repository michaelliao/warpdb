package com.itranswarp.warpdb;

/**
 * Base criteria query.
 * 
 * @author liaoxuefeng
 * 
 * @param <T> Generic type.
 */
abstract class CriteriaQuery<T> {

    protected final Criteria<T> criteria;

    CriteriaQuery(Criteria<T> criteria) {
        this.criteria = criteria;
    }

    String sql() {
        return criteria.sql(null);
    }

    String sql(String aggregate) {
        return criteria.sql(aggregate);
    }
}
