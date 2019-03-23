package com.itranswarp.warpdb;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represent a paged result list.
 * 
 * @author liaoxuefeng
 *
 * @param <T> Generic type.
 */
public class PagedResults<T> {

	public Page page;

	public List<T> results;

	public PagedResults() {
	}

	public PagedResults(Page page, List<T> results) {
		this.page = page;
		this.results = results;
	}

	public Page getPage() {
		return page;
	}

	public List<T> getResults() {
		return results;
	}

	public <R> PagedResults<R> map(Function<? super T, ? extends R> mapper) {
		List<R> newResults = results.stream().map(mapper).collect(Collectors.toList());
		return new PagedResults<>(this.page, newResults);
	}

}
