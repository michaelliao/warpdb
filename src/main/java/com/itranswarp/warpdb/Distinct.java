package com.itranswarp.warpdb;

/**
 * select DISTINCT ... from ...
 * 
 * @author liaoxuefeng
 */
@SuppressWarnings("rawtypes")
public final class Distinct extends CriteriaQuery {

    @SuppressWarnings("unchecked")
    Distinct(Criteria<?> criteria) {
        super(criteria);
        criteria.distinct = true;
    }

    /**
     * Continue with FROM clause.
     * 
     * @param entityClass The entity class.
     * @return Criteria query object.
     */
    @SuppressWarnings("unchecked")
    public <T> From<T> from(Class<T> entityClass) {
        return new From<T>(this.criteria, this.criteria.warpdb.getMapper(entityClass));
    }
}
