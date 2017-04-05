package io.coodoo.framework.listing.boundary;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import io.coodoo.framework.listing.control.ListingQuery;
import io.coodoo.framework.listing.boundary.annotation.ListingEntityManager;

public class ListingService {

  @Inject @ListingEntityManager EntityManager entityManager;

  public <T> ListingResult<T> getListingResult(Class<T> entityClass,
                                               ListingQueryParams queryParams) {
    return Listing.getListingResult(entityManager, entityClass, queryParams);
  }

  public <T> List<T> getListing(Class<T> entityClass,
                                ListingQueryParams queryParams) {
    return Listing.getListing(entityManager, entityClass, queryParams);
  }

  public <T> Long countListing(Class<T> entityClass,
                               ListingQueryParams queryParams) {
    return Listing.countListing(entityManager, entityClass, queryParams);
  }

  public <T> ListingResult<T> getListingResult(Class<T> entityClass,
                                               Integer page, Integer limit) {
    return Listing.getListingResult(entityManager, entityClass, page, limit);
  }

  public <T> List<T> getListing(Class<T> entityClass, Integer page,
                                Integer limit) {
    return Listing.getListing(entityManager, entityClass, page, limit);
  }

  public <T> Long countListing(Class<T> entityClass, Integer page,
                               Integer limit) {
    return Listing.countListing(entityManager, entityClass, page, limit);
  }

  public <T> ListingResult<T> getListingResult(Class<T> entityClass,
                                               Integer page, Integer limit,
                                               String sortAttribute) {
    return Listing.getListingResult(entityManager, entityClass, page, limit,
                                    sortAttribute);
  }

  public <T> List<T> getListing(Class<T> entityClass, Integer page,
                                Integer limit, String sortAttribute) {
    return Listing.getListing(entityManager, entityClass, page, limit,
                              sortAttribute);
  }

  public <T> Long countListing(Class<T> entityClass, Integer page,
                               Integer limit, String sortAttribute) {
    return Listing.countListing(entityManager, entityClass, page, limit,
                                sortAttribute);
  }
}
