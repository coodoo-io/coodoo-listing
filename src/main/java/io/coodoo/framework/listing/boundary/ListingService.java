package io.coodoo.framework.listing.boundary;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import io.coodoo.framework.listing.control.ListingFilterQuery;

@Stateless
public class ListingService {

    @PersistenceContext
    EntityManager entityManager;

    public <T> ListingResult<T> getListingResult(Class<T> entityClass, ListingQueryParams queryParams) {
        return new ListingResult<T>(getListing(entityClass, queryParams), new Metadata(countListing(entityClass, queryParams), queryParams));
    }

    public <T> List<T> getListing(Class<T> entityClass, ListingQueryParams queryParams) {
        return assambleFilter(entityClass, queryParams).list(queryParams.getIndex(), queryParams.getLimit());
    }

    public <T> Long countListing(Class<T> entityClass, ListingQueryParams queryParams) {
        return assambleFilter(entityClass, queryParams).count();
    }

    private <T> ListingFilterQuery<T> assambleFilter(Class<T> entityClass, ListingQueryParams queryParams) {
        return new ListingFilterQuery<>(entityManager, entityClass)
                        // apply sorting
                        .sort(queryParams.getSortAttribute(), queryParams.isSortAsc())
                        // disjunctive filter on the whole table
                        .filterAllAttributes(queryParams.getFilter())
                        // column specific conjunctive filter
                        .filterByAttributes(queryParams.getFilterAttributes())
                        // additional filters
                        .filterByPredicates(queryParams.getPredicates());
    }

    public <T> ListingResult<T> getListingResult(Class<T> entityClass, Integer page, Integer limit) {
        return getListingResult(entityClass, new ListingQueryParams(page, limit, null));
    }

    public <T> List<T> getListing(Class<T> entityClass, Integer page, Integer limit) {
        return getListing(entityClass, new ListingQueryParams(page, limit, null));
    }

    public <T> Long countListing(Class<T> entityClass, Integer page, Integer limit) {
        return countListing(entityClass, new ListingQueryParams(page, limit, null));
    }

    public <T> ListingResult<T> getListingResult(Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return getListingResult(entityClass, new ListingQueryParams(page, limit, sortAttribute));
    }

    public <T> List<T> getListing(Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return getListing(entityClass, new ListingQueryParams(page, limit, sortAttribute));
    }

    public <T> Long countListing(Class<T> entityClass, Integer page, Integer limit, String sortAttribute) {
        return countListing(entityClass, new ListingQueryParams(page, limit, sortAttribute));
    }

}
