package io.coodoo.framework.listing.boundary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public class ListingResult<T> {

    private Metadata metadata;

    private List<T> results;

    private Map<String, List<Term>> terms = new HashMap<>();

    private Map<String, Stats> stats = new HashMap<>();

    public ListingResult(List<T> result, Metadata metadata) {
        super();
        this.metadata = metadata;
        this.results = result;
    }

    public ListingResult(List<T> result, Map<String, List<Term>> terms, Map<String, Stats> stats, Metadata metadata) {
        super();
        this.terms = terms;
        this.stats = stats;
        this.metadata = metadata;
        this.results = result;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> result) {
        this.results = result;
    }

    public Map<String, List<Term>> getTerms() {
        return terms;
    }

    public void setTerms(Map<String, List<Term>> terms) {
        this.terms = terms;
    }

    public Map<String, Stats> getStats() {
        return stats;
    }

    public void setStats(Map<String, Stats> stats) {
        this.stats = stats;
    }

    @Override
    public String toString() {
        return "ListingResult [metadata=" + metadata + ", results=" + results + ", terms=" + terms + ", stats=" + stats + "]";
    }

}
