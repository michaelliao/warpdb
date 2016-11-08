package com.itranswarp.warpdb;

import java.util.Arrays;

/**
 * SELECT ... from ...
 * 
 * @author liaoxuefeng
 */
@SuppressWarnings("rawtypes")
public final class Select extends CriteriaQuery {

	@SuppressWarnings("unchecked")
	Select(Criteria criteria, String... selectFields) {
		super(criteria);
		if (selectFields.length > 0) {
			this.criteria.select = Arrays.asList(selectFields);
		}
	}

	public Distinct distinct() {
		return new Distinct(this.criteria);
	}

	@SuppressWarnings("unchecked")
	public <T> From<T> from(Class<T> entityClass) {
		return new From<T>(this.criteria, this.criteria.warpdb.getMapper(entityClass));
	}

}
