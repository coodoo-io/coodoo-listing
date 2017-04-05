# JavaEE JPA/JAX-RS Listing Framework

*A generic JPA/JAX-RS listing mechanism for easy paging, filtering, sorting and searching on entities.*

There is no need anymore to implement separate services for every entity to just have sortable and filterable table listings on a web page.
This library gives you easy access to list your entities by calling a JAX-RS API with just two lines of code.


## Getting started

1. Add the following dependency to your project ([published on Maven Central](http://search.maven.org/#artifactdetails%7Cio.coodoo%7Clisting%7C1.0.0%7Cjar)):

   ```xml
	<dependency>
	    <groupId>io.coodoo</groupId>
	    <artifactId>listing</artifactId>
	    <version>1.2.2</version>
	</dependency>
   ```

2. Usage in a JAX-RS Resource

   Inject `ListingService` located in the `io.coodoo.listing` package. The wine entity a standard JPA Entity, annotated with  `@Entity`.
   

   ```java
   
	@Path("/wines")
	@Stateless
	public void WineResource {
	    @Inject
	    ListingService listingService
	    
	    @GET
	    @Path("/")
	    @Produces(MediaType.APPLICATION_JSON)
	    public ListingResult getWines(@BeanParam ListingQueryParams listingQueryParams) {
	    
	    	// Just inject and invoke the listingService to page an entity.
	    	return listingService.getListingResult(Wine.class, listingQueryParams);
	    }
	}
 
   ```

   Just call this REST Resource: `curl http://localhost:8080/app-context/api/wines`
   
3. Usage in a Stateless EJB

   Inject `ListingService` located in the `io.coodoo.listing` package. The wine entity a standard JPA Entity, annotated with  `@Entity`.

   ```java

	@Stateless
	public void WineBusinessService {
    	private static Logger log = LoggerFactory.getLogger(WineBusinessService.class);
    	
	    @Inject
	    ListingService listingService
	    
	    public void doListing() {
	    
	    	// Just inject and invoke the listingService to page the wine entity.
	    	ListingResult<Wine> wineListingResult = listingService.getListingResult(Wine.class, 1, 50);
	    	
	    	log.info("Loaded page 1 with limit 50. Total wines count: {}", wineListingResult.getMetadata()getCount();
	    	
	    	for(Wine wine : wineListingResult.getResults()) {
	    		log.info("Loaded wine: {}", wine);
	    	}
	    }
	}
 
   ```


## Usage

The central service is the `ListingService` that needs to get injected as a CDI or an EJB bean. It provides three ways to get listing data:
 * `getListing` gets a list of the desired data
 * `countListing` gets the count of resulting data
 * `getListingResult` gets an result object that contains the list and metadata (total count, page, index, ...) of the resulting data

Every method takes the targeted entity class as a parameter.

### Listing query parameter
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


   
## Configuration

To provide own configuration you need to add a property file named `coodoo.listing.properties` to your project. This file gets read on JavaEE server startup if available or manually by calling `ListingConfig.loadProperties()`;

These are the properties to be defined on the file:

```properties
### coodoo listing configuration ###

## Default values ##

## Default index for pagination
coodoo.listing.default.index = 0
## Default current page number for pagination
coodoo.listing.default.page = 1
## Default limit of results per page for pagination
coodoo.listing.default.limit = 10

## filter conjunction trigger
coodoo.listing.filter.type.disjunction = Filter-Type-Disjunction

## Limit on OR operator separated predicated to handle it in an IN statement
coodoo.listing.or.to.in.imit = 10

## Operators ##

## NOT operator
coodoo.listing.operator.not = !
## NOT operator as word
coodoo.listing.operator.not.word = NOT
## OR operator
coodoo.listing.operator_or = |
## OR operator as word
coodoo.listing.operator.or.word = OR
## NULL operator
coodoo.listing.operator.null = NULL
```
*You can find a template [here](https://github.com/coodoo-io/listing/tree/master/src/main/resources/example.coodoo.listing.properties)*

