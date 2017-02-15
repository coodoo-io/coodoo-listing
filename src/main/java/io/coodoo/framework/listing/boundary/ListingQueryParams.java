package io.coodoo.framework.listing.boundary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

/**
 * Listing query parameters and settings
 * 
 * @author coodoo
 */
public class ListingQueryParams {

    /**
     * Default index for pagination
     */
    public static final int DEFAULT_INDEX = 0;

    /**
     * Default current page number for pagination
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * Default limit of results per page for pagination
     */
    public static final int DEFAULT_LIMIT = 10;

    /**
     * If this key is present in filterAttributes map, the attributes gets disjuncted (default is conjunction)
     */
    public static final String FILTER_TYPE_DISJUNCTION = "Filter-Type-Disjunction";

    @QueryParam("page")
    @DefaultValue("" + ListingQueryParams.DEFAULT_PAGE)
    private Integer page;

    @QueryParam("limit")
    @DefaultValue("" + ListingQueryParams.DEFAULT_LIMIT)
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


    public ListingQueryParams() {}

    public ListingQueryParams(Integer limit, Integer page, Integer index, String sortAttribute, String filter) {
        super();
        this.page = page;
        this.limit = limit;
        this.filter = filter;
        this.sortAttribute = sortAttribute;
        this.index = index;
    }

    public Integer getPage() {
        if (page == null && limit != null && index != null) {
            int mod = index % limit;
            if (index == 0) {
                return ListingQueryParams.DEFAULT_PAGE;
            } else if (mod == 0) {
                return index / limit;
            }
            return (index / limit) + 1;
        } else if (page == null) {
            return ListingQueryParams.DEFAULT_PAGE;
        } else if (page < 1) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        if (limit == null) {
            return ListingQueryParams.DEFAULT_LIMIT;
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
        if (StringUtils.isBlank(sortAttribute)) {
            return null;
        }

        if (sortAttribute.startsWith("-")) {
            return sortAttribute.substring(1, sortAttribute.length());
        } else if (sortAttribute.startsWith("+")) {
            return sortAttribute.substring(1, sortAttribute.length());
        }

        return sortAttribute;
    }

    public void setSortAttribute(String sortAttribute) {
        this.sortAttribute = sortAttribute;
    }

    public boolean isSortAsc() {
        if (StringUtils.isBlank(sortAttribute)) {
            return true;
        }

        if (sortAttribute.startsWith("-")) {
            return false;
        } else {
            return true;
        }
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public Integer getIndex() {
        if (index == null && page != null) {
            // calculate index from page
            return (page - 1) * limit;
        } else if (index == null) {
            return ListingQueryParams.DEFAULT_INDEX;
        }
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void addFilterAttributes(String filter, String value) {
        filterAttributes.put(filter, value);
    }

    public Map<String, String> getFilterAttributes() {

        if (uriInfo != null) {
            MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

            for (Map.Entry<String, List<String>> queryParam : queryParams.entrySet()) {
                String queryParamAttribute = queryParam.getKey();
                if (StringUtils.isBlank(queryParamAttribute) || !queryParamAttribute.startsWith("filter-")) {
                    continue;
                }
                queryParamAttribute = queryParamAttribute.substring("filter-".length(), queryParamAttribute.length());
                filterAttributes.put(queryParamAttribute, queryParam.getValue().get(0));
            }
        }
        return filterAttributes;
    }

}
