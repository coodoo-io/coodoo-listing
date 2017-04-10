package io.coodoo.framework.listing.boundary;

import java.util.List;

import javax.persistence.EntityManager;

import io.coodoo.framework.listing.control.ListingQuery;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public class Listing {

    public static <T> ListingResult<T> getListingResult(EntityManager entityManager, Class<T> entityClass, ListingParameters queryParams) {
        return new ListingResult<T>(getListing(entityManager, entityClass, queryParams),
                        new Metadata(countListing(entityManager, entityClass, queryParams), queryParams));
    }

    public static <T> List<T> getListing(EntityManager entityManager, Class<T> entityClass, ListingParameters queryParams) {
        List<T> list = assambleFilter(entityManager, entityClass, queryParams).list(queryParams.getIndex(), queryParams.getLimit());
        if (list.isEmpty() && queryParams.getPage() > 1) {
            queryParams.setPage(queryParams.getPage() - 1);
            return getListing(entityManager, entityClass, queryParams);
        }
        return list;
    }

    public static <T> Long countListing(EntityManager entityManager, Class<T> entityClass, ListingParameters queryParams) {
        return assambleFilter(entityManager, entityClass, queryParams).count();
    }

    private static <T> ListingQuery<T> assambleFilter(EntityManager entityManager, Class<T> entityClass, ListingParameters queryParams) {

        return new ListingQuery<>(entityManager, entityClass)
                        // apply sorting
                        .sort(queryParams.getSortAttribute(), queryParams.isSortAsc())
                        // disjunctive filter on the whole table
                        .filterAllAttributes(queryParams.getFilter())
                        // column specific conjunctive filter
                        .filterByAttributes(queryParams.getFilterAttributes())
                        // additional filters
                        .filterByPredicate(queryParams.getPredicate());
    }

    public static <T> ListingResult<T> getListingResult(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit) {
        return getListingResult(entityManager, entityClass, new ListingParameters(page, limit, null));
    }

    public static <T> List<T> getListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit) {
        return getListing(entityManager, entityClass, new ListingParameters(page, limit, null));
    }

    public static <T> Long countListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit) {
        return countListing(entityManager, entityClass, new ListingParameters(page, limit, null));
    }

    public static <T> ListingResult<T> getListingResult(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return getListingResult(entityManager, entityClass, new ListingParameters(page, limit, sortAttribute));
    }

    public static <T> List<T> getListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return getListing(entityManager, entityClass, new ListingParameters(page, limit, sortAttribute));
    }

    public static <T> Long countListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return countListing(entityManager, entityClass, new ListingParameters(page, limit, sortAttribute));
    }

}
