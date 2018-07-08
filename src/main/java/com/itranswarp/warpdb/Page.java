package com.itranswarp.warpdb;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a Page object using by limit(offset, maxResults).
 * 
 * @author liaoxuefeng
 */
public class Page {

	public static final int DEFAULT_ITEMS_PER_PAGE = 20;

	public final int pageIndex;
	public final int itemsPerPage;
	public final int totalPages;
	public final int totalItems;
	public final boolean isEmpty;

	public Page(int pageIndex, int itemsPerPage, int totalPages, int totalItems) {
		this.pageIndex = pageIndex;
		this.itemsPerPage = itemsPerPage;
		this.totalPages = totalPages;
		this.totalItems = totalItems;
		this.isEmpty = totalItems == 0;
	}

	public List<Integer> list(int currentIndex) {
		int edge = 4;
		int start = currentIndex - edge;
		int end = currentIndex + edge;
		if (start < 1) {
			start = 1;
		}
		if (end > totalPages) {
			end = totalPages;
		}
		List<Integer> list = new ArrayList<>(end - start + 3);
		if (start >= 2) {
			list.add(1);
		}
		if (start >= 3) {
			list.add(0);
		}
		for (int n = start; n <= end; n++) {
			list.add(n);
		}
		if (end < totalPages) {
			list.add(0);
		}
		return list;
	}
}
