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

    public ListingResult<T> getListingResult(Class<T> entityClass, Integer limit, Integer page, Integer index, String sortAttribute, String filter) {

        ListingQueryParams queryParams = new ListingQueryParams();
        queryParams.setLimit(limit);
        queryParams.setPage(page);
        queryParams.setIndex(index);
        queryParams.setSortAttribute(sortAttribute);
        queryParams.setFilter(filter);

        return getListingResult(entityClass, queryParams);
    }

    public ListingResult<T> getListingResult(Class<T> entityClass, ListingQueryParams queryParams) {

        ListingResult<T> listingResult = new ListingResult<T>();
        listingResult.setResult(getListing(entityClass, queryParams));
        listingResult.setCount(countListing(entityClass, queryParams));

        return listingResult;

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

}
