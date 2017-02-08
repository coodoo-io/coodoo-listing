package io.coodoo.framework.listing.boundary.dto;

import io.coodoo.framework.listing.boundary.ListingQueryParams;

public class ListingMetadataDTO {
	private long count;
	private int currentPage;
	private int numPages;
	private int limit;
	private String sort;
	private int startIndex;
	private int endIndex;

	public ListingMetadataDTO(Long count, ListingQueryParams listingQueryParams) {
		this(count, listingQueryParams.getPage(), listingQueryParams.getLimit(), null);
	}

	public ListingMetadataDTO(Long count, int currentPage, int itemsPerPage) {
		this(count, currentPage, itemsPerPage, null);
	}

	public ListingMetadataDTO(Long count, int currentPage, int limit, String sort) {
		this.count = count;
		this.currentPage = currentPage;
		this.limit = limit;

		numPages = count.intValue() / limit + (count.intValue() % limit != 0 ? 1 : 0);
		if (count < limit) {
			numPages = 1;
		}

		startIndex = (limit * currentPage) - limit + 1;
		endIndex = limit * currentPage;
		if (endIndex > count) {
			endIndex = count.intValue();
		}

	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getNumPages() {
		return numPages;
	}

	public void setNumPages(int numPages) {
		this.numPages = numPages;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}
}
