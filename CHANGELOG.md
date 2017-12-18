

<!--
### Bug Fixes
### Features
### BREAKING CHANGES
-->




<a name="1.4.4"></a>

## 1.4.4 (2017-12-18)

### Bug Fixes

 *  Removed useless but problematic null condition in `ListingParameters.getLimit()`


<a name="1.4.3"></a>

## 1.4.3 (2017-04-25)

### Bug Fixes

 * Removed rebel nature


<a name="1.4.2"></a>

## 1.4.2 (2017-04-18)

### Bug Fixes

 * Removed silly sort from count statement (only MySQL was able to proceed with this bug)
 * LIKE statements are now always compared by both sides in lower case
 * NOT-condition back for duty
 * Since `@Id` marked attributes are also columns, they will be not longer ignored by `filterAllAttributes()`
 * Design related problems in case limit = 0

### Features

 * New supported types: Float & Double
 * New operators LT, GT, TO & LIKE ('<', '>', '-' & '~') for the usage at numerical fields
 * Sort direction indicators configurable: `ListingConfig.SORT_ASC` & `ListingConfig.SORT_DESC`
 * Wildcards in LIKE statements ('*' '?') configurable: `ListingConfig.WILDCARD_MANY` & `ListingConfig.WILDCARD_ONE`

### BREAKING CHANGES

 * Configuration key `ListingConfig.OR_TO_IN_LIMIT` "coodoo.listing.or.to.in.limit" reduced to `ListingConfig.OR_LIMIT` "coodoo.listing.or.limit"


<a name="1.4.1"></a>

## 1.4.1 (2017-04-10)

### Bug Fixes

 * Reducing the page number in case less results are found to prevent empty pages

### Features

 * Attribute order swaped in ListingResult class so the meta data appear first (e.g. in Rest response) to simplify debugging 

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
