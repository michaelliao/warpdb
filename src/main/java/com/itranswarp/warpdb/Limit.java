package com.itranswarp.warpdb;

import java.util.List;

import com.itranswarp.warpdb.entity.BaseEntity;

public final class Limit<T extends BaseEntity> {

	final SelectInfo<T> info;

	Limit(SelectInfo<T> info, int offset, int maxResults) {
		if (offset < 0) {
			throw new IllegalArgumentException("offset must be >= 0.");
		}
		if (maxResults <= 0) {
			throw new IllegalArgumentException("maxResults must be > 0.");
		}
		this.info = info;
		info.offset = offset;
		info.maxResults = maxResults;
	}

	/**
	 * Get all results as list.
	 * 
	 * @return list.
	 */
	public List<T> list() {
		return info.list();
	}
}
