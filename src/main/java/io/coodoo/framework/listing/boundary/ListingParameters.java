package io.coodoo.framework.listing.boundary;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import io.coodoo.framework.listing.control.ListingConfig;

/**
 * Listing query parameters and settings
 * 
 * @author coodoo GmbH (coodoo.io)
 */
public class ListingParameters {

    @QueryParam("page")
    private Integer page;

    @QueryParam("limit")
    private Integer limit;

    @QueryParam("filter")
    private String filter;

    @QueryParam("sort")
    private String sortAttribute;

    @Context
    private UriInfo uriInfo;

    @QueryParam("index")
    private Integer index;

    private Map<String, String> filterAttributes = new HashMap<>();

    private ListingPredicate predicate;

    public ListingParameters() {}

    public ListingParameters(Integer page, Integer limit, String sortAttribute) {
        super();
        this.page = page;
        this.limit = limit;
        this.sortAttribute = sortAttribute;
    }

    public Integer getPage() {
        if (page == null && limit != null && index != null) {
            int mod = index % limit;
            if (index == 0) {
                return ListingConfig.DEFAULT_PAGE;
            } else if (mod == 0) {
                return index / limit;
            }
            return (index / limit) + 1;
        } else if (page == null) {
            return ListingConfig.DEFAULT_PAGE;
        } else if (page < 1) {
            return ListingConfig.DEFAULT_PAGE;
        }
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        if (limit == null) {
            return ListingConfig.DEFAULT_LIMIT;
        }
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getFilter() {
        return StringUtils.trimToNull(filter);
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getSortAttribute() {
        return sortAttribute;
    }

    public void setSortAttribute(String sortAttribute) {
        this.sortAttribute = sortAttribute;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public Integer getIndex() {
        if (index == null && page != null && limit != null) {
            // calculate index from page and limit
            return (page - 1) * limit;
        }
        if (index == null) {
            return ListingConfig.DEFAULT_INDEX;
        }
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void addFilterAttributes(String filter, String value) {
        filterAttributes.put(filter, URLDecoder.decode(value));
    }

    public Map<String, String> getFilterAttributes() {

        if (uriInfo != null) {
            MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters(true);

            for (Map.Entry<String, List<String>> queryParam : queryParams.entrySet()) {
                String queryParamAttribute = queryParam.getKey();
                if (StringUtils.isBlank(queryParamAttribute) || !queryParamAttribute.startsWith("filter-")) {
                    continue;
                }
                queryParamAttribute = queryParamAttribute.substring("filter-".length(), queryParamAttribute.length());
                String filterVal = StringUtils.trimToNull(queryParam.getValue().get(0));
                if (filterVal != null) {
                    filterAttributes.put(queryParamAttribute, filterVal);
                }
            }
        }
        return filterAttributes;
    }

    public ListingPredicate getPredicate() {
        return predicate;
    }

    public void setPredicate(ListingPredicate predicate) {
        this.predicate = predicate;
    }

}
