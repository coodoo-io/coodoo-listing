package io.coodoo.framework.listing.boundary;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import io.coodoo.framework.listing.control.ListingFilterQuery;

@Stateless
public class ListingService<T> {

    @PersistenceContext
    EntityManager entityManager;


    public ListingResult<T> getListingResult(Class<T> entityClass, ListingQueryParams queryParams) {

        return new ListingResult<T>(getListing(entityClass, queryParams), new Metadata(countListing(entityClass, queryParams), queryParams));
    }

    public List<T> getListing(Class<T> entityClass, ListingQueryParams queryParams) {

        return new ListingFilterQuery<>(entityManager, entityClass).sort(queryParams.getSortAttribute(), queryParams.isSortAsc())
                        .filterAllAttributes(queryParams.getFilter()).filterByAttributes(queryParams.getFilterAttributes())
                        .list(queryParams.getIndex(), queryParams.getLimit());
    }

    public Long countListing(Class<T> entityClass, ListingQueryParams queryParams) {

        return new ListingFilterQuery<>(entityManager, entityClass).sort(queryParams.getSortAttribute(), queryParams.isSortAsc())
                        .filterAllAttributes(queryParams.getFilter()).filterByAttributes(queryParams.getFilterAttributes()).count();
    }

    public ListingResult<T> getListingResult(Class<T> entityClass, Integer limit, Integer page, Integer index, String sortAttribute, String filter) {

        return getListingResult(entityClass, new ListingQueryParams(limit, page, index, sortAttribute, filter));
    }

    public List<T> getListing(Class<T> entityClass, Integer limit, Integer page, Integer index, String sortAttribute, String filter) {

        return getListing(entityClass, new ListingQueryParams(limit, page, index, sortAttribute, filter));
    }

    public Long countListing(Class<T> entityClass, Integer limit, Integer page, Integer index, String sortAttribute, String filter) {

        return countListing(entityClass, new ListingQueryParams(limit, page, index, sortAttribute, filter));
    }

}
