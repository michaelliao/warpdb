package com.itranswarp.warpdb;

abstract class CriteriaQuery<T> {

	protected final Criteria<T> criteria;

	CriteriaQuery(Criteria<T> criteria) {
		this.criteria = criteria;
	}

	String sql() {
		return sql(null);
	}

	String sql(String aggregate) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("SELECT ").append(this.criteria.distinct ? "DISTINCT " : "")
				.append(aggregate == null
						? (this.criteria.select == null ? "*" : String.join(", ", this.criteria.select)) : aggregate)
				.append(" FROM ").append(this.criteria.table);
		if (this.criteria.where != null) {
			sb.append(" WHERE ").append(String.join(" ", this.criteria.where));
		}
		if (aggregate == null && this.criteria.orderBy != null) {
			sb.append(" ORDER BY ").append(String.join(", ", this.criteria.orderBy));
		}
		if (aggregate == null && this.criteria.offset >= 0 && this.criteria.maxResults > 0) {
			sb.append(" LIMIT ?, ?");
		}
		return sb.toString();
	}

}
