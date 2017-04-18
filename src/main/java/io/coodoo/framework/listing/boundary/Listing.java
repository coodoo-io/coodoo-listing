package io.coodoo.framework.listing.boundary;

import java.util.List;

import javax.persistence.EntityManager;

import io.coodoo.framework.listing.control.ListingQuery;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public class Listing {

    /**
     * Gets the listing result
     * 
     * <h3>URL query parameters</h3>
     * <ul>
     * <li><strong>filter</strong>: The filter value gets applied to every column of the table. Every row where a column matches this filter will be part of the
     * result (disjunctive). It can be used as a sort of global search on a Table.</li>
     * <li><strong>filter-<i>xxx</i></strong>: filter attributes where <i>xxx</i> is the row name (attribute of the target entity) and the filter value the
     * filter for that row. Every row where all filter attributes matches will be part of the result (conjunctive).</li>
     * <li><strong>sort</strong>: Given a row name will sort the result in ascending order, to get a descending sorted result a the row name must start with
     * "-"</li>
     * </ul>
     * <h3>Pagination</h3>
     * <ul>
     * <li><strong>limit</strong>: Amount of maximal expected results per request to fit on a page (default = 10)</li>
     * <li><strong>page</strong>: Current page (default = 1)</li>
     * <li><strong>index</strong>: Index (default = 0)</li>
     * </ul>
     * 
     * @param <T> type of target entity class
     * @param entityManager entity manager of designated persistence unit
     * @param entityClass target entity class
     * @param parameters defines the listing queue. It contains optional query parameters as described above
     * @return a {@link ListingResult} object containing metadata and the resulting list of the target entity instances (sublist in case of pagination)
     *         <h3>{@link Metadata}</h3>
     *         <ul>
     *         <li><strong>count</strong>: Amount of matching results</li>
     *         <li><strong>currentPage</strong>: Current page (pagination)</li>
     *         <li><strong>numPages</strong>: Amount of available pages (pagination)</li>
     *         <li><strong>limit</strong>: Amount of given result for the current page, see list results (pagination, if 0 then there is no limit)</li>
     *         <li><strong>sort</strong>: Current order by this row (ascending except if it starts with "-" = descending)</li>
     *         <li><strong>startIndex</strong>: Index of the first result in this page (pagination)</li>
     *         <li><strong>endIndex</strong>: Index of the last result in this page (pagination)</li>
     *         </ul>
     */
    public static <T> ListingResult<T> getListingResult(EntityManager entityManager, Class<T> entityClass, ListingParameters parameters) {
        return new ListingResult<T>(getListing(entityManager, entityClass, parameters),
                        new Metadata(countListing(entityManager, entityClass, parameters), parameters));
    }

    /**
     * Gets the list of found instances
     * 
     * @param <T> type of target entity class
     * @param entityManager entity manager of designated persistence unit
     * @param entityClass target entity class
     * @param parameters defines the listing queue. It contains optional query parameters as described above
     * @return generic list of found instances
     */
    public static <T> List<T> getListing(EntityManager entityManager, Class<T> entityClass, ListingParameters parameters) {
        // List<T> list = assambleFilter(entityManager, entityClass, parameters).list(parameters.getIndex(), parameters.getLimit());

        List<T> list = new ListingQuery<>(entityManager, entityClass)
                        // apply sorting
                        .sort(parameters.getSortAttribute(), parameters.isSortAsc())
                        // disjunctive filter on the whole table
                        .filterAllAttributes(parameters.getFilter())
                        // column specific conjunctive filter
                        .filterByAttributes(parameters.getFilterAttributes())
                        // additional filters
                        .filterByPredicate(parameters.getPredicate())
                        // get all matching entries in the given range
                        .list(parameters.getIndex(), parameters.getLimit());

        if (list.isEmpty() && parameters.getPage() > 1) {
            // Reducing the page number in case less results are found to prevent empty pages
            parameters.setPage(parameters.getPage() - 1);
            return getListing(entityManager, entityClass, parameters);
        }
        return list;
    }

    /**
     * Gets the count of the found instances of the target entity
     * 
     * @param <T> type of target entity class
     * @param entityManager entity manager of designated persistence unit
     * @param entityClass target entity class
     * @param parameters defines the listing queue. It contains optional query parameters as described above
     * @return the amount of found instances
     */
    public static <T> Long countListing(EntityManager entityManager, Class<T> entityClass, ListingParameters parameters) {
        // return assambleFilter(entityManager, entityClass, parameters).count();
        return new ListingQuery<>(entityManager, entityClass)
                        // disjunctive filter on the whole table
                        .filterAllAttributes(parameters.getFilter())
                        // column specific conjunctive filter
                        .filterByAttributes(parameters.getFilterAttributes())
                        // additional filters
                        .filterByPredicate(parameters.getPredicate())
                        // count all matching entries
                        .count();
    }

    /**
     * Gets the listing result
     * 
     * @param <T> type of target entity class
     * @param entityManager entity manager of designated persistence unit
     * @param entityClass target entity class
     * @param page Current page (pagination)
     * @param limit Amount of given result for the current page, see list results (pagination, if 0 then there is no limit)
     * @return a {@link ListingResult} object containing metadata and the resulting list of the target entity instances (sublist in case of pagination)
     */
    public static <T> ListingResult<T> getListingResult(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit) {
        return getListingResult(entityManager, entityClass, new ListingParameters(page, limit, null));
    }

    /**
     * Gets the list of found instances
     * 
     * @param <T> type of target entity class
     * @param entityManager entity manager of designated persistence unit
     * @param entityClass target entity class
     * @param page Current page (pagination)
     * @param limit Amount of given result for the current page, see list results (pagination, if 0 then there is no limit)
     * @return generic list of found instances
     */
    public static <T> List<T> getListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit) {
        return getListing(entityManager, entityClass, new ListingParameters(page, limit, null));
    }

    /**
     * Gets the count of the found instances of the target entity
     * 
     * @param <T> type of target entity class
     * @param entityManager entity manager of designated persistence unit
     * @param entityClass target entity class
     * @param page Current page (pagination)
     * @param limit Amount of given result for the current page, see list results (pagination, if 0 then there is no limit)
     * @return the amount of found instances
     */
    public static <T> Long countListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit) {
        return countListing(entityManager, entityClass, new ListingParameters(page, limit, null));
    }

    /**
     * Gets the listing result
     * 
     * @param <T> type of target entity class
     * @param entityManager entity manager of designated persistence unit
     * @param entityClass target entity class
     * @param page Current page (pagination)
     * @param limit Amount of given result for the current page, see list results (pagination, if 0 then there is no limit)
     * @param sortAttribute Current order by this row (ascending except if it starts with "-" = descending)
     * @return a {@link ListingResult} object containing metadata and the resulting list of the target entity instances (sublist in case of pagination)
     */
    public static <T> ListingResult<T> getListingResult(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return getListingResult(entityManager, entityClass, new ListingParameters(page, limit, sortAttribute));
    }

    /**
     * Gets the list of found instances
     * 
     * @param <T> type of target entity class
     * @param entityManager entity manager of designated persistence unit
     * @param entityClass target entity class
     * @param page Current page (pagination)
     * @param limit Amount of given result for the current page, see list results (pagination, if 0 then there is no limit)
     * @param sortAttribute Current order by this row (ascending except if it starts with "-" = descending)
     * @return generic list of found instances
     */
    public static <T> List<T> getListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return getListing(entityManager, entityClass, new ListingParameters(page, limit, sortAttribute));
    }

    /**
     * Gets the count of the found instances of the target entity
     * 
     * @param <T> type of target entity class
     * @param entityManager entity manager of designated persistence unit
     * @param entityClass target entity class
     * @param page Current page (pagination)
     * @param limit Amount of given result for the current page, see list results (pagination, if 0 then there is no limit)
     * @param sortAttribute Current order by this row (ascending except if it starts with "-" = descending)
     * @return the amount of found instances
     */
    public static <T> Long countListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return countListing(entityManager, entityClass, new ListingParameters(page, limit, sortAttribute));
    }

}
