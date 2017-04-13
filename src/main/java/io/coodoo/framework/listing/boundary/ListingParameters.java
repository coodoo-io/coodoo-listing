package io.coodoo.framework.listing.boundary;

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
        } else if (limit == 0) {
            return null;
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
        if (sortAttribute.startsWith(ListingConfig.OPERATOR_SORT_DESC)) {
            return sortAttribute.substring(ListingConfig.OPERATOR_SORT_DESC.length());
        }
        if (sortAttribute.startsWith(ListingConfig.OPERATOR_SORT_ASC)) {
            return sortAttribute.substring(ListingConfig.OPERATOR_SORT_ASC.length());
        }
        return sortAttribute.trim();
    }

    public void setSortAttribute(String sortAttribute) {
        this.sortAttribute = sortAttribute;
    }

    public boolean isSortAsc() {
        if (StringUtils.isBlank(sortAttribute)) {
            return true;
        }
        return !sortAttribute.startsWith(ListingConfig.OPERATOR_SORT_DESC);
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
            return ListingConfig.DEFAULT_INDEX;
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
