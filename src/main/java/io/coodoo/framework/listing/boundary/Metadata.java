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

    private Long count;
    private Integer currentPage;
    private Integer numPages;
    private Integer limit;
    private String sort;
    private Integer startIndex;
    private Integer endIndex;

    public Metadata(Long count, ListingParameters listingQueryParams) {
        this(count, listingQueryParams.getPage(), listingQueryParams.getLimit(), listingQueryParams.getSortAttribute());
    }

    public Metadata(Long count, Integer currentPage, Integer itemsPerPage) {
        this(count, currentPage, itemsPerPage, null);
    }

    public Metadata(Long count, Integer currentPage, Integer limit, String sort) {

        this.count = count;
        this.currentPage = currentPage;
        this.sort = sort;

        if (count == null || count == 0) {

            this.limit = limit;
            this.numPages = 1;
            this.startIndex = 1;
            this.endIndex = 1;

        } else if (limit == null || limit == 0) {

            this.limit = 0;
            this.numPages = 1;
            this.startIndex = 1;
            this.endIndex = count.intValue();

        } else {

            this.limit = limit;
            if (count < limit) {
                this.numPages = 1;
            } else {
                this.numPages = count.intValue() / limit + (count.intValue() % limit != 0 ? 1 : 0);
            }
            if (currentPage == null) {
                this.currentPage = 1;
            }
            this.startIndex = (limit * this.currentPage) - limit + 1;
            this.endIndex = limit * this.currentPage;
            if (endIndex > count) {
                this.endIndex = count.intValue();
            }
        }
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getNumPages() {
        return numPages;
    }

    public void setNumPages(Integer numPages) {
        this.numPages = numPages;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public Integer getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
    }

    @Override
    public String toString() {
        return "Metadata [count=" + count + ", currentPage=" + currentPage + ", numPages=" + numPages + ", limit=" + limit + ", sort=" + sort + ", startIndex="
                        + startIndex + ", endIndex=" + endIndex + "]";
    }

}
