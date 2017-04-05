package io.coodoo.framework.listing.boundary;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import io.coodoo.framework.listing.control.ListingQuery;

@Stateless
public class ListingService {

    @PersistenceContext
    EntityManager entityManager;

    public <T> ListingResult<T> getListingResult(Class<T> entityClass, ListingParameters queryParams) {
        return new ListingResult<T>(getListing(entityClass, queryParams), new Metadata(countListing(entityClass, queryParams), queryParams));
    }

    public <T> List<T> getListing(Class<T> entityClass, ListingParameters queryParams) {
        return assambleFilter(entityClass, queryParams).list(queryParams.getIndex(), queryParams.getLimit());
    }

    public <T> Long countListing(Class<T> entityClass, ListingParameters queryParams) {
        return assambleFilter(entityClass, queryParams).count();
    }

    private <T> ListingQuery<T> assambleFilter(Class<T> entityClass, ListingParameters queryParams) {
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

    public <T> ListingResult<T> getListingResult(Class<T> entityClass, Integer page, Integer limit) {
        return getListingResult(entityClass, new ListingParameters(page, limit, null));
    }

    public <T> List<T> getListing(Class<T> entityClass, Integer page, Integer limit) {
        return getListing(entityClass, new ListingParameters(page, limit, null));
    }

    public <T> Long countListing(Class<T> entityClass, Integer page, Integer limit) {
        return countListing(entityClass, new ListingParameters(page, limit, null));
    }

    public <T> ListingResult<T> getListingResult(Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return getListingResult(entityClass, new ListingParameters(page, limit, sortAttribute));
    }

    public <T> List<T> getListing(Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return getListing(entityClass, new ListingParameters(page, limit, sortAttribute));
    }

    public <T> Long countListing(Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return countListing(entityClass, new ListingParameters(page, limit, sortAttribute));
    }

}
