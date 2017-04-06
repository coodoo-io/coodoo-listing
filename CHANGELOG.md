

<!--
### Bug Fixes
### Features
### BREAKING CHANGES
-->






<a name="1.4.0"></a>

## 1.4.0 (2017-04-06)

### BREAKING CHANGES

 * CDI producer `@ListingEntityManager` removed
 * `ListingService` removed (use `Listing` class with static method and pass the entity manager)


<a name="1.3.0"></a>

## 1.3.0 (2017-04-05)

### Features

 * Central `Listing` class with static method
 * No EJB components anymore, just plain CDI
 * Configuration via properties file (coodoo.listing.properties)
 * Configuration static loader
 * Limitless results if limit is set to 0


### BREAKING CHANGES

 * Renamed the project/Maven artifactId from "listing" to "coodoo-listing"
 * Renamed `ListingQueryParams` to `ListingParameters`
 * To provide the EntityManager you have to implement a `@ListingEntityManager` CDI producer


<a name="1.2.2"></a>

## 1.2.2 (2017-03-13)

### Bug Fixes

 * Multi column filter via pipe restored (go broken in 1.2.1)

<a name="1.2.1"></a>

## 1.2.1 (2017-03-11)

### Bug Fixes

 * Clean ups and refactoring


<a name="1.2.0"></a>

## 1.2.0 (2017-03-01)

### Features

 * Custom predicate tree with builder pattern style constructors

<a name="1.1.0"></a>

## 1.1.0 (2017-03-01)

### Features

 * Additional custom predicates for the filter query 

<a name="1.0.3"></a>

## 1.0.3 (2017-02-24)

### Bug Fixes

 * Preventing stack overflow if there are to many disjunction in an filter value


<a name="1.0.2"></a>

## 1.0.2 (2017-02-23)

### Bug Fixes

 * Empty attribute filter


<a name="1.0.0"></a>

## 1.0.0 (2017-02-21)

### Features

Initial release:

* Listing service
* Listing model
* Listing criteria builder
* Listing entity annotations
