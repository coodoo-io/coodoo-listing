# coodoo-listing

> A generic JPA/JAX-RS listing mechanism for easy paging, filtering, sorting and searching on entities.

## Table of Contents

- [Background](#background)
- [Install](#install)
  - [Usage in a JAX-RS resource](#usage-in-a-jax-rs-resource)
  - [Usage in a stateless EJB](#usage-in-a-stateless-ejb)
- [Usage](#usage)
<!--
- [Filter options](#filter-options)
  - [Keywords](#keywords)
  - [Numbers](#numbers)
  - [Dates](#dates)
  - [Enums](#enums)
-->
- [API](#api)
  - [Listing](#listing)
  - [ListingParameters](#listingparameters)
    - [Filter (search on whole table)](#filter-search-on-whole-table)
    - [Filter attributes](#filter-attributes)
    - [Pagination](#pagination)
    - [Sort](#sort)
    - [ListingPredicate](#listingpredicate)
- [Configuration](#configuration)
- [Maintainers](#maintainers)
- [Contribute](#contribute)
- [License](#license)

## Background

Every application contains lists and to make them convenient you need pagination, sorting, filtering and perhaps a search. So you need to implement this functionality for all the attributes a list item has. After that you repeat the same for the next list, over and over again.
The code may remain the same, but the attributes and requirements changes and you end up with a lot of code that needs to get maintained for every little change on the list.

This library gives you easy access to list your JPA entities. All you need to do is to pass the entity manager, the entity class and some optional paramters.


## Install

Add the following dependency to your project ([published on Maven Central](http://search.maven.org/#artifactdetails%7Cio.coodoo%7Ccoodoo-listing%7C1.4.1%7Cjar)):

```xml
<dependency>
    <groupId>io.coodoo</groupId>
    <artifactId>coodoo-listing</artifactId>
    <version>1.4.1</version>
</dependency>
```

## Usage

### Usage in a JAX-RS resource

```java
@Path("/wines")
@Stateless
public void WineResource {

    @PersistenceContext
    private EntityManager entityManager;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public ListingResult getWines(@BeanParam ListingParameters listingParameters) {
  
        return Listing.getListingResult(entityManager, Wine.class, listingParameters);
    }
}
```

Just call this REST Resource: `curl http://localhost:8080/app-context/api/wines`


### Usage in a stateless EJB

```java
@Stateless
public void WineBusinessService {
    private static Logger log = LoggerFactory.getLogger(WineBusinessService.class);

    @PersistenceContext
    private EntityManager entityManager;

    public void doListing() {

        ListingResult<Wine> wineListingResult = Listing.getListingResult(entityManager, Wine.class, 1, 50);

        log.info("Loaded page 1 with limit 50. Total wines count: {}", wineListingResult.getMetadata()getCount();

        for(Wine wine : wineListingResult.getResults()) {
            log.info("Loaded wine: {}", wine);
        }
    }
}
```

<!--
## Filter options

### Keywords

### Numbers

### Dates

### Enums
-->

## API

### Listing

The central `Listing` class provides following static methods that will query the database:
 * `getListing` gets a list of the desired data
 * `countListing` gets the count of resulting data
 * `getListingResult` gets an result object that contains the list and metadata (total count, page, index, ...) of the resulting data




Every method takes at least the entity manager and targeted entity class as parameters. It's up to you if you provide a `ListingParameters` object or plain values for page, limit and sort.

### ListingParameters

This is where the magic happens. The `ListingParameters` class defines the query for the request. Therefore it has following parameters: 

#### Filter (search on whole table)

The filter gets applied to every column of the table. Every row where a column matches this filter will be part of the result (disjunctive).
It can be used as a sort of global search on a Table.

```java
ListingParameters listingParameters = ListingParameters();
listingParameters.setFilter("rouge");
return Listing.getListingResult(entityManager, Wine.class, listingParameters);
}
```
Or via REST Resource: `curl http://localhost:8080/app-context/api/wines?filter=rouge`

#### Filter attributes

The `ListingParameters` object contains a map for filter attributes where the key is the row name (attribute of the target entity) and the value the filter for that row.
Every row where all filter attributes matches will be part of the result (conjunctive).

```java
ListingParameters listingParameters = ListingParameters();
listingParameters.addFilterAttributes("year", "1983");
listingParameters.addFilterAttributes("name", "rouge");
return Listing.getListingResult(entityManager, Wine.class, listingParameters);
}
```
Or via REST Resource: `curl http://localhost:8080/app-context/api/wines?filter-year=1983&filter-name=rouge`

In case the presentation combines two ore more columns so they share just one filter input you can with the filter attributes.

```java
ListingParameters listingParameters = ListingParameters();
listingParameters.addFilterAttributes("year|name", "83");
return Listing.getListingResult(entityManager, Wine.class, listingParameters);
}
```
Or via REST Resource: `curl http://localhost:8080/app-context/api/wines?filter-year|name=83`


#### Pagination

#### Sort

#### ListingPredicate


## Configuration

To provide own configuration you need to add a property file named `coodoo.listing.properties` to your project. This file gets read on JavaEE server startup if available or manually by calling `ListingConfig.loadProperties()`;

You can find a template [here](https://github.com/coodoo-io/coodoo-listing/tree/master/src/main/resources/example.coodoo.listing.properties)


## Maintainers

[Markus Kühle](https://github.com/mkuehle)

[Arend Kühle](https://github.com/laugen)

## Contribute

Pull requests and [issues](https://github.com/coodoo-io/coodoo-listing/issues) are welcome.

## License

[MIT © coodoo GmbH](./LICENSE)
