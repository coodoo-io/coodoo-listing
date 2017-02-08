package io.coodoo.framework.listing.boundary;

import java.util.List;

public class ListingResult<T> {

    private List<T> result;

    private Long count;

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

}
