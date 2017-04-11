package io.coodoo.framework.listing.boundary;

import java.util.List;

import javax.persistence.EntityManager;

import io.coodoo.framework.listing.control.ListingQuery;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public class Listing {

    public static <T> ListingResult<T> getListingResult(EntityManager entityManager, Class<T> entityClass, ListingParameters parameters) {
        return new ListingResult<T>(getListing(entityManager, entityClass, parameters),
                        new Metadata(countListing(entityManager, entityClass, parameters), parameters));
    }

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
