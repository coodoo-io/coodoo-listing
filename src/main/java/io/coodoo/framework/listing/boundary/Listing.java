package io.coodoo.framework.listing.boundary;

import java.util.List;

import javax.persistence.EntityManager;

import io.coodoo.framework.listing.control.ListingFilterQuery;

public class Listing {

    public static <T> ListingResult<T> getListingResult(EntityManager entityManager, Class<T> entityClass, ListingQueryParams queryParams) {
        return new ListingResult<T>(getListing(entityManager, entityClass, queryParams),
                        new Metadata(countListing(entityManager, entityClass, queryParams), queryParams));
    }

    public static <T> List<T> getListing(EntityManager entityManager, Class<T> entityClass, ListingQueryParams queryParams) {
        return assambleFilter(entityManager, entityClass, queryParams).list(queryParams.getIndex(), queryParams.getLimit());
    }

    public static <T> Long countListing(EntityManager entityManager, Class<T> entityClass, ListingQueryParams queryParams) {
        return assambleFilter(entityManager, entityClass, queryParams).count();
    }

    private static <T> ListingFilterQuery<T> assambleFilter(EntityManager entityManager, Class<T> entityClass, ListingQueryParams queryParams) {

        return new ListingFilterQuery<>(entityManager, entityClass)
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
        return getListingResult(entityManager, entityClass, new ListingQueryParams(page, limit, null));
    }

    public static <T> List<T> getListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit) {
        return getListing(entityManager, entityClass, new ListingQueryParams(page, limit, null));
    }

    public static <T> Long countListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit) {
        return countListing(entityManager, entityClass, new ListingQueryParams(page, limit, null));
    }

    public static <T> ListingResult<T> getListingResult(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return getListingResult(entityManager, entityClass, new ListingQueryParams(page, limit, sortAttribute));
    }

    public static <T> List<T> getListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return getListing(entityManager, entityClass, new ListingQueryParams(page, limit, sortAttribute));
    }

    public static <T> Long countListing(EntityManager entityManager, Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return countListing(entityManager, entityClass, new ListingQueryParams(page, limit, sortAttribute));
    }

}
