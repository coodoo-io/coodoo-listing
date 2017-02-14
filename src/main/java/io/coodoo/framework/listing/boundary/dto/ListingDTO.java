package io.coodoo.framework.listing.boundary.dto;

import java.util.List;

/**
 * Listing data transfer object. It contains the listing result and its meta data.
 * 
 * @author coodoo
 * @param <T>
 */
public class ListingDTO<T> {

    private ListingMetadataDTO metadata;

    private List<T> results;

    public ListingDTO(ListingMetadataDTO metadata, List<T> results) {
        this.metadata = metadata;
        this.results = results;
    }

    public ListingMetadataDTO getMetadata() {
        return metadata;
    }

    public void setMetadata(ListingMetadataDTO metaData) {
        this.metadata = metaData;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }
}
