package io.coodoo.framework.listing.boundary;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import io.coodoo.framework.listing.boundary.annotation.ListingFilterIgnore;
import io.coodoo.framework.listing.control.ListingConfig;

/**
 * Listing query parameters and settings
 * 
 * @author coodoo GmbH (coodoo.io)
 */
public class ListingParameters {

    @QueryParam("index")
    private Integer index;

    @QueryParam("page")
    private Integer page;

    @QueryParam("limit")
    private Integer limit;

    @QueryParam("sort")
    private String sortAttribute;

    @QueryParam("filter")
    private String filter;

    private Map<String, String> filterAttributes = new HashMap<>();

    private Map<String, String> termsAttributes = new HashMap<>();

    private ListingPredicate predicate;

    @Context
    private UriInfo uriInfo;

    public ListingParameters() {}

    /**
     * @param page number for pagination
     * @param limit of results per page for pagination
     * @param sortAttribute name of the attribute the result list gets sorted by
     */
    public ListingParameters(Integer page, Integer limit, String sortAttribute) {
        super();
        this.page = page;
        this.limit = limit;
        this.sortAttribute = sortAttribute;
    }

    /**
     * @return index for pagination (position in whole list where current pagination page starts)
     */
    public Integer getIndex() {
        // the index can be calculated if page and limit are given
        if (index == null && page != null) {
            return (page - 1) * getLimit(); // getLimit() finds the given limit or takes the default limit as fallback
        }
        // could not calculate the index -> use default
        if (index == null || index < 0) {
            return ListingConfig.DEFAULT_INDEX;
        }
        return index;
    }

    /**
     * @param index for pagination (position in whole list where current pagination page starts)
     */
    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * @return page number for pagination
     */
    public Integer getPage() {
        // current page can be calculated if limit and index are given
        if (page == null && limit != null && index != null && index > 0 && limit > 0) {
            return index % limit == 0 ? index / limit : (index / limit) + 1;
        }
        // no valid page number given -> use default
        if (page == null || page < 1) {
            return ListingConfig.DEFAULT_PAGE;
        }
        return page;
    }

    /**
     * @param page number for pagination
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * @return limit of results per page for pagination
     */
    public Integer getLimit() {
        // no limit given -> use default
        if (limit == null) {
            return ListingConfig.DEFAULT_LIMIT;
        }
        return limit;
    }

    /**
     * @param limit of results per page for pagination (use 0 to get the whole list)
     */
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * @return name of the attribute the result list gets sorted by (prefix '+' for ascending (default) or '-' for descending order. E.g. '-creationDate')
     */
    public String getSortAttribute() {
        return prepare(sortAttribute);
    }

    /**
     * @param sortAttribute name of the attribute the result list gets sorted by (prefix '+' for ascending (default) or '-' for descending order. E.g.
     *        '-creationDate')
     */
    public void setSortAttribute(String sortAttribute) {
        this.sortAttribute = sortAttribute;
    }

    /**
     * @return global filter string that is applied to all attributes
     */
    public String getFilter() {
        return prepare(filter);
    }

    /**
     * @param filter global filter string that is applied to all attributes (use {@link ListingFilterIgnore} on an attribute in the target entity to spare it
     *        out)
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Adds a filter to a specific attribute
     * 
     * @param attribute attribute name
     * @param value filter value
     */
    public void addFilterAttributes(String attribute, String value) {

        String preparedValue = prepare(value);

        if (preparedValue != null) {
            filterAttributes.put(attribute, preparedValue);
        }
    }

    /**
     * Adds a terms aggregation for a specific field
     * 
     * @param fieldName target field for the terms aggregation
     * @param size terms aggregation size
     */
    public void addTermsAttributes(String fieldName, String size) {

        String preparedValue = prepare(size.toString());

        if (preparedValue != null) {
            termsAttributes.put(fieldName, preparedValue);
        }
    }

    private String prepare(String value) {

        String trimmedValue = StringUtils.trimToNull(value);

        if (trimmedValue != null && ListingConfig.URI_DECODE) {
            try {
                return URLDecoder.decode(trimmedValue, ListingConfig.URI_CHARACTER_ENCODING);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return trimmedValue;
    }

    /**
     * @return Map of attribute specific filters
     */
    public Map<String, String> getFilterAttributes() {

        // collects filter from URI if given
        if (uriInfo != null) {

            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters(true);

            for (Map.Entry<String, List<String>> queryParameter : queryParameters.entrySet()) {

                String filterAttribute = queryParameter.getKey();
                if (StringUtils.isBlank(filterAttribute) || !filterAttribute.startsWith("filter-")) {
                    continue;
                }
                filterAttribute = filterAttribute.substring("filter-".length(), filterAttribute.length());

                addFilterAttributes(filterAttribute, queryParameter.getValue().get(0));
            }
        }
        return filterAttributes;
    }

    public void setFilterAttributes(Map<String, String> filterAttributes) {
        this.filterAttributes = filterAttributes;
    }

    /**
     * @return Map of attribute specific aggregations
     */
    public Map<String, String> getTermsAttributes() {

        // collects filter from URI if given
        if (uriInfo != null) {

            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters(true);

            for (Map.Entry<String, List<String>> queryParameter : queryParameters.entrySet()) {

                String termsAttribute = queryParameter.getKey();
                if (StringUtils.isBlank(termsAttribute) || !termsAttribute.startsWith("terms-")) {
                    continue;
                }
                termsAttribute = termsAttribute.substring("terms-".length(), termsAttribute.length());

                addTermsAttributes(termsAttribute, queryParameter.getValue().get(0));
            }
        }
        return termsAttributes;
    }

    public void setTermsAttributes(Map<String, String> termsAttributes) {
        this.termsAttributes = termsAttributes;
    }

    /**
     * @return root of custom filter condition tree
     */
    public ListingPredicate getPredicate() {
        return predicate;
    }

    /**
     * @param predicate root of custom filter condition tree
     */
    public void setPredicate(ListingPredicate predicate) {
        this.predicate = predicate;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

}
