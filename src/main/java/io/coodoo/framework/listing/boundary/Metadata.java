package io.coodoo.framework.listing.boundary;

/**
 * The meta data provides information for the use of a pagination presentation.
 * 
 * <br>
 * <code>count</code>: Count of the whole list <br>
 * <code>currentPage</code>: Current page as a sublist with the length of <code>limit</code> <br>
 * <code>numPages</code>: Number of pages <br>
 * <code>limit</code>: List elements per page <br>
 * <code>sort</code>: Name of the attribute, the result is sorter by (ascending by default, starts with "-" for descending)<br>
 * <code>startIndex</code>: Index of the first result for the current page<br>
 * <code>endIndex</code>: Index of the last result for the current page<br>
 * 
 * @author coodoo GmbH (coodoo.io)
 */
public class Metadata {

    private long count;
    private int currentPage;
    private int numPages;
    private int limit;
    private String sort;
    private int startIndex;
    private int endIndex;

    public Metadata(Long count, ListingParameters listingQueryParams) {
        this(count, listingQueryParams.getPage(), listingQueryParams.getLimit(), listingQueryParams.getSortAttribute());
    }

    public Metadata(Long count, int currentPage, int itemsPerPage) {
        this(count, currentPage, itemsPerPage, null);
    }

    public Metadata(Long count, int currentPage, int limit, String sort) {
        this.count = count;
        this.currentPage = currentPage;
        this.limit = limit;
        this.sort = sort;

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

    @Override
    public String toString() {
        return "Metadata [count=" + count + ", currentPage=" + currentPage + ", numPages=" + numPages + ", limit=" + limit + ", sort=" + sort + ", startIndex="
                        + startIndex + ", endIndex=" + endIndex + "]";
    }

}
