

<!--
### Bug Fixes
### Features
### BREAKING CHANGES
-->

<a name="1.6.2"></a>

## 1.6.2 (2021-09-15)

### Features
* Legacy support for systems that are unable to match `ListingParamters.addFilterAttributes(String, Object)` anymore.
* Happy 1st Birthday Simon! 🥳


<a name="1.6.1"></a>

## 1.6.1 (2021-07-30)

### Features
* `ListingParameters.addFilterAttributes()` isn't limited to `String` anymore. So you don't have to stringify numeric or enum values anymore.


<a name="1.6.0"></a>

## 1.6.0 (2020-04-20)

### Features

* Visual table summary? Want to know what's the most of something? No problem! You can acquire terms and statistics by just throwing attribute names into filter! Have a look at *Visual Table Summary* in this [UX Collective article](https://uxdesign.cc/design-better-data-tables-4ecc99d23356).
* Fetch size optimization can be applied by `ListingConfig.FETCHSIZE`, witch will add `typedQuery.setHint("org.hibernate.fetchSize", ListingConfig.FETCHSIZE);` to the query if set.
* Convenient new `ListingParameters` constructors with parameters `page` & `limit` and only `limit`

<a name="1.5.1"></a>

## 1.5.1 (2019-10-18)

### Features

* New filter operator AND ('&')
  * Ever wondered what German city has double A and double L in its name? Attribute: `city` Filter: `aa&ll` - it's Halle an der Saale
  * Lets see, what Eike did in December 2018! Attribute: `createdBy|createdAt` Filter: `Eike AND 12.2018`


<a name="1.5.0"></a>

## 1.5.0 (2019-08-17)

### Features

* Date filter values accept any character now, not only dots, e.g. `04-10-1983`
* Date filter values accept now also two digit years, e.g. `04-10-83`
* Date type now accepts current milliseconds as filter values

 
### BREAKING CHANGES
 
* `@ListingLikeOnNumber` got replaced by `@ListingFilterAsString` and is no more

### Bug Fixes

* Made date filter pattern `MM.YYYY` work


<a name="1.4.10"></a>

## 1.4.10 (2019-07-28)

### Features

* Collection fields are now accessible if annotated with `@ListingLikeOnNumber`. This only makes sense, if the database field can be string represented!


<a name="1.4.9"></a>

## 1.4.9 (2019-03-25)

### Features

* `@ListingFilterAsString` replaced now deprecated `@ListingLikeOnNumber`. This new annotation will cast any type to string to match a filter.
* Configurable boolean values `ListingConfig.BOOLEAN_TRUE` and `ListingConfig.BOOLEAN_FALSE` to customize the filter.
* New `ListingConfig.URI_DECODE` flag decides if string values that are provided by the URL (`sortAttribute`, `filter`, `filterAttributes`) gets URI decoded (Default: `false`).


<a name="1.4.8"></a>

## 1.4.8 (2018-12-12)

### Features

*  To avoid a limitation of the result list you can now set the limit to zero: `listingParameters.setLimit(0)`;

### Bug Fixes

* Keywords like `AND`, `LIKE`, `NOT`, etc gets escaped before being used in regular expressions. So there is more possibilities when using custom keywords.
* Using a page number without limit doesn't start at the first element of the total result list anymore
* URI character encoding configurable (default is UTF8)

<a name="1.4.7"></a>

## 1.4.7 (2018-10-19)

### Features

*  It is now possible to send FilterAttributes as Uri Encoded String

### Bug Fixes

* IllegalStateException ("Duplicate key...") in ListingQuery.addToWhereConstraint() fixed

<a name="1.4.6"></a>

## 1.4.6 (2018-01-29)

### Features

*  Ascending indicator "+" is removed by URL, so the sort attribute needed to get trimmed


<a name="1.4.5"></a>

## 1.4.5 (2018-01-28)

### Features

*  Multiple sort attributes when using semicolon as separator


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
