# coodoo-listing

> A generic JPA/JAX-RS listing mechanism for easy paging, filtering, sorting and searching on entities.

## Table of Contents

- [Background](#background)
- [Install](#install)
  - [Usage in a JAX-RS resource](#usage-in-a-jax-rs-resource)
  - [Usage in a stateless EJB](#usage-in-a-stateless-ejb)
- [Usage](#usage)
- [Filter options](#filter-options)
- [API](#api)
  - [Listing](#listing)
  - [ListingParameters](#listingparameters)
    - [Filter (search on whole table)](#filter-search-on-whole-table)
    - [Filter attributes](#filter-attributes)
    - [Pagination](#pagination)
    - [Sort](#sort)
    - [Additional predicates](additional-predicates)
  - [Metadata](#metadata)
- [Configuration](#configuration)
- [Changelog](#changelog)
- [Maintainers](#maintainers)
- [Contribute](#contribute)
- [License](#license)

## Background

Every application contains lists and to make them convenient you need pagination, sorting, filtering and perhaps a search. So you need to implement this functionality for all the attributes a list item has. After that you repeat the same for the next list, over and over again.
The code may remain the same, but the attributes and requirements changes and you end up with a lot of code that needs to get maintained for every little change on the list.

This library gives you easy access to list your JPA entities. All you need to do is to pass the entity manager, the entity class and some optional paramters.


## Install

Add the following dependency to your project ([published on Maven Central](http://search.maven.org/#artifactdetails%7Cio.coodoo%7Ccoodoo-listing%7C1.4.9%7Cjar)):

```xml
<dependency>
    <groupId>io.coodoo</groupId>
    <artifactId>coodoo-listing</artifactId>
    <version>1.4.9</version>
</dependency>
```

## Usage

### Usage in a JAX-RS resource

```java
@Stateless
@Path("/cars")
public class ListingResource {

    @GET
    public ListingResult<Car> getCarsListing(@BeanParam ListingParameters listingParameters) {

        return Listing.getListingResult(entityManager, Car.class, listingParameters);
    }
}
```

Just call this REST Resource: `curl http://localhost:8080/showcase/api/cars`

[Example implementation](https://github.com/coodoo-io/coodoo-framework-showcase/blob/master/src/main/java/io/coodoo/framework/showcase/listing/boundary/ListingResource.java)


### Usage in a stateless EJB

```java
@Stateless
public void CarService {

    private static Logger log = LoggerFactory.getLogger(CarService.class);

    @PersistenceContext
    private EntityManager entityManager;

    public void doListing() {

        ListingResult<Car> carListingResult = Listing.getListingResult(entityManager, Car.class, 1, 50);

        log.info("Loaded page 1 with limit 50. Total cars count: {}", carListingResult.getMetadata().getCount();

        for(Car car : carListingResult.getResults()) {
            log.info("Loaded car: {}", car);
        }
    }
}
```

[Example implementation](https://github.com/coodoo-io/coodoo-framework-showcase/blob/master/src/main/java/io/coodoo/framework/showcase/listing/boundary/ListingService.java)


## Filter options

| Option                   | Word      | Example            | Character           | Example                    | Limitation     |
|--------------------------|-----------|--------------------|---------------------|----------------------------|----------------|
| Negation                 | `NOT`     | `NOT BMW`          | `!`                 | `!BMW`                     |                |
| Value disjunction        | `OR`      | `BMW OR Audi`      | <code>&#124;</code> | <code>BMW&#124;Audi</code> |                |
| Less than                | `LT`      | `LT 200`           | `<`                 | `<200`                     | Numbers, Dates |
| Greater than             | `GT`      | `GT 200`           | `>`                 | `>200`                     | Numbers, Dates |
| Range                    | `TO`      | `200 TO 400`       | `-`                 | `200-400`                  | Numbers, Dates |
| No value                 | `NULL`    | `NULL`             |                     |                            |                |
| Value only               | `NOT NULL` | `NOT NULL`        |                     |                            |                |
| Like comparison*         | `LIKE`    | `LIKE 200`         | `~`                 | `~200`                     | Texts, Numbers        |
| Exact match              |           |                    | `"`                 | `"W124"`                   | Texts        |
| Wildcard one character   |           |                    | `?`                 | `A?di`                     | Texts, Numbers |
| Wildcard many characters |           |                    | `*`                 | `A*`                       | Texts, Numbers |

*Default for Strings

### Date filter options
| Option          | Example                 | Description                                              |
|-----------------|-------------------------|----------------------------------------------------------|
|One whole day    | `23.03.2015`            | 23.03.2015 (Time range 00:00:00.000 - 23:59:59.999)      |
|One whole month  | `03.2015`               | From 01.03.2015 to 31.03.2015                            |
|One whole year   | `2015`                  | From 01.01.2015 to 31.12.2015                            |
|Period           | `15.01.2018-05.02.2018` | From 15.01.2018 to 05.02.2018                            |
|Period           | `10.10.2010 TO 2012`    | Specific day up to all of the year 2012                  |
|Negation         | `!23.03.2015`           | All but that one day                                     |
|Negated period   | `NOT 2011 TO 2014`      | Everything but no date between 31.12.2013 and 01.01.2015 |
|After a day      | `>04.10.1983`           | 05.10.1983 and all after                                 |
|Before a year    | `LT 2000`               | 31.12.1999 and all before                                |

[See examples here](https://github.com/coodoo-io/coodoo-framework-showcase/tree/master/src/main/java/io/coodoo/framework/showcase/listing/boundary/examples)


## API

### Suported data types

- Texts
  - String
  - Enum
- Numbers
  - Long / long
  - Integer / int
  - Short / short
  - Float / float
  - Double / double
- Dates
  - Date
  - LocalDateTime
- States
  Boolean / boolean

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
ListingParameters listingParameters = new ListingParameters();
listingParameters.setFilter("150");
return Listing.getListingResult(entityManager, Car.class, listingParameters);
```
Or via Rest Resource: `curl http://localhost:8080/showcase/api/listing?filter=150`

[Examples](https://github.com/coodoo-io/coodoo-framework-showcase/blob/master/src/main/java/io/coodoo/framework/showcase/listing/boundary/examples/ListingFilterService.java)


#### Filter attributes

The `ListingParameters` object contains a map for filter attributes where the key is the row name (attribute of the target entity) and the value the filter for that row.
Every row where all filter attributes matches will be part of the result (conjunctive).

```java
ListingParameters listingParameters = new ListingParameters();
listingParameters.addFilterAttributes("seats", "2");
return Listing.getListingResult(entityManager, Car.class, listingParameters);
```
Or via Rest Resource: `curl http://localhost:8080/showcase/api/listing?filter-seats=2`

[Examples](https://github.com/coodoo-io/coodoo-framework-showcase/blob/master/src/main/java/io/coodoo/framework/showcase/listing/boundary/examples/ListingFilterAttributesService.java)


#### Pagination

The `ListingParameters` object accepts a page number and a limit of results per page to provide you a the current page sublist. With the result list in the `ListingResult`object there comes `Metadata` object for all the other details. 

```java
ListingParameters listingParameters = new ListingParameters();
listingParameters.setPage(3);
listingParameters.setLimit(50);
return Listing.getListingResult(entityManager, Car.class, listingParameters);
```
Or via Rest Resource: `curl http://localhost:8080/showcase/api/listing?page=3&limit=50`

[Examples](https://github.com/coodoo-io/coodoo-framework-showcase/blob/master/src/main/java/io/coodoo/framework/showcase/listing/boundary/examples/ListingPaginationService.java)


#### Sort

You can sort the resulting list by the attribute name and specify the order by prefixing the attribute name with '+' for ascending or '-' for descending order.

```java
ListingParameters listingParameters = new ListingParameters();
listingParameters.setSortAttribute("-hp");
return Listing.getListingResult(entityManager, Car.class, listingParameters);
```
Or via Rest Resource: `curl http://localhost:8080/showcase/api/listing?sort=-hp`

[Examples](https://github.com/coodoo-io/coodoo-framework-showcase/blob/master/src/main/java/io/coodoo/framework/showcase/listing/boundary/examples/ListingSortService.java)


#### Additional predicates

Listing predicates enhances filtering by basic SQL elements compiled in a tree data structure. This can be useful to provided different views on the same data, or the enforce access permissions.

*Predicate are an addition to filters given in the `ListingParameters`*

```java
ListingPredicate predicate = new ListingPredicate().filter("fuel", "Diesel");
ListingParameters listingParameters = new ListingParameters();
listingParameters.setPredicate(predicate);
return Listing.getListingResult(entityManager, Car.class, listingParameters);
```

[Examples](https://github.com/coodoo-io/coodoo-framework-showcase/blob/master/src/main/java/io/coodoo/framework/showcase/listing/boundary/examples/ListingPredicateService.java)


### Metadata

The `Metadata`-object provides information for the use of a pagination presentation and is part of the `ListingResult`-object.

- count *Count of the whole list*
- currentPage *Current page as a sublist with the length of limit*
- numPages *Number of pages*
- limit *List elements per page*
- sort *Name of the attribute, the result is sorter by (ascending by default, starts with "-" for descending)*
- startIndex *Index of the first result for the current page*
- endIndex *Index of the last result for the current page*


## Configuration

To provide own configuration you need to add a property file named `coodoo.listing.properties` to your project. This file gets read on JavaEE server startup if available or manually by calling `ListingConfig.loadProperties()`;

You can find a template [here](https://github.com/coodoo-io/coodoo-listing/tree/master/src/main/resources/example.coodoo.listing.properties)

### Order by - Nulls last

A descending sort will show null values on top. To avoid this you could change this behaviour by adding this hibernate propertiy to your project:
`hibernate.order_by.default_null_ordering=last`

## Changelog

All release changes can be viewed on our [changelog](./CHANGELOG.md).

## Maintainers

[coodoo](https://github.com/orgs/coodoo-io/people)

## Contribute

Pull requests and [issues](https://github.com/coodoo-io/coodoo-listing/issues) are welcome.

## License

[MIT © coodoo GmbH](./LICENSE)
