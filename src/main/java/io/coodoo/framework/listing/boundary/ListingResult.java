package io.coodoo.framework.listing.boundary;

import java.util.List;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public class ListingResult<T> {

    private List<T> results;

    private Metadata metadata;

    public ListingResult(List<T> result, Metadata metadata) {
        super();
        this.results = result;
        this.metadata = metadata;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> result) {
        this.results = result;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

}
