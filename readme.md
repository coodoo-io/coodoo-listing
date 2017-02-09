# JavaEE JPA/JAX-RS Listing Framework

*A generic JPA/JAX-RS listing mechanism for easy paging, filtering, sorting and searching on entities*

There is no need anymore to implement separate services for every entity to just have sortable and filterable table listing on a web page.
This library gives you easy access to list your entities.


## Getting started

The central service is the `ListingService` that needs to get injected as a CDI or an EJB bean. It provides three ways to get listing data:
 * `getListing` gets a list of the desired data
 * `countListing` gets the count of resulting data
 * `getListingResult` gets an result object that contains the list and count of the resulting data

Every method takes the targeted entity class as a parameter.

## Listing query parameter
To control the listing there is a listing query parameter object `ListingFilterParams`. 

...

### Pagination
 * page
 * limit
 * index
...

### Filter
Filter attributes are stored in a map by attribute and value. A filter attribute corresponds to a entity attribute and it provides a string representation of the value.
To add a filter use `ListingFilterParams.addFilterAttributes(String filter, String value)`.

Disjunctive filtering is possible by adding a filter named by this constant: `ListingFilterParams.FILTER_TYPE_DISJUNCTION`

### Sort
Add the name of the desired `sort` attribute to the `ListingFilterParams` object and it will result in an ascending sort for the listing.
To get a descending sort, the `sort` attribute needs to get a "-" prefix added.

### Search (or global filter)
To put one filter on all the attributes of the entity there is the `filter` attribute.
To exclude attributes from this filter the entity attributs needs to get annotated with `@ListingFilterIgnore`.

## The usage with Jax-RS
...


## coodoo

[coodoo](http://coodoo.io/) is a German technology company developing next generation web applications by using Java EE and AngularJS.
