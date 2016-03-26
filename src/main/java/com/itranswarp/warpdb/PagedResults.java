package com.itranswarp.warpdb;

import java.util.List;

public class PagedResults<T> {

	public final Page page;

	public final List<T> results;

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

//	public <R> PagedResults<R> map(Function<? super T, ? extends R> mapper){
//		List<R> newResults = results.stream().map(mapper).collect(Collectors.toList());
//		return new PagedResults<R>(this.page, newResults);
//	}

}
