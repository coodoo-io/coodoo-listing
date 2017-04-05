package io.coodoo.framework.listing.boundary;

import java.util.ArrayList;
import java.util.List;

/**
 * @author coodoo GmbH (coodoo.io)
 */
public class ListingPredicate {

    private String attribute;

    private String filter;

    private boolean in;

    private boolean disjunctive;

    private boolean negation;

    private List<ListingPredicate> predicates;

    /**
     * Constructor
     */
    public ListingPredicate() {
        this.disjunctive = false;
        this.negation = false;
        this.in = false;
        this.predicates = new ArrayList<>();
    }

    /**
     * Makes this a conjunctive predicate
     * 
     * @return this
     */
    public ListingPredicate and() {
        this.disjunctive = false;
        return this;
    }

    /**
     * Makes this a disjunctive predicate
     * 
     * @return this
     */
    public ListingPredicate or() {
        this.disjunctive = true;
        return this;
    }

    /**
     * Makes this a negated predicate
     * 
     * @return this
     */
    public ListingPredicate not() {
        this.negation = true;
        return this;
    }

    /**
     * Makes this a IN statement predicate by providing an filter of values conjuncted by pipes ("|")
     * 
     * @return this
     */
    public ListingPredicate in() {
        this.in = true;
        return this;
    }

    /**
     * Makes this a predicate
     * 
     * @param attribute target attribute
     * @param filter filter value
     * @return this
     */
    public ListingPredicate filter(String attribute, String filter) {
        this.attribute = attribute;
        this.filter = filter;
        return this;
    }

    /**
     * Adds this a child predicate
     * 
     * @param predicate filter predicate
     * @return this
     */
    public ListingPredicate predicate(ListingPredicate predicate) {
        if (predicate != null) {
            this.predicates.add(predicate);
        }
        return this;
    }

    /**
     * Makes this a set of predicates
     * 
     * @param predicates filter predicates
     * @return this
     */
    public ListingPredicate predicates(List<ListingPredicate> predicates) {
        if (predicates != null) {
            this.predicates = predicates;
        }
        return this;
    }

    /**
     * Adds a child predicate
     * 
     * @param listingPredicate child predicate
     */
    public void addPredicate(ListingPredicate listingPredicate) {
        this.predicates.add(listingPredicate);
    }

    /**
     * Has child predicates
     * 
     * @return <code>true</code> if child predicate list is not null or empty, <code>false</code> otherwise
     */
    public boolean hasPredicates() {
        return this.predicates != null && !this.predicates.isEmpty();
    }

    @Override
    public String toString() {
        return "ListingPredicate [attribute=" + attribute + ", filter=" + filter + ", in=" + in + ", disjunctive=" + disjunctive + ", negation=" + negation
                        + ", predicates=" + predicates + "]";
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public boolean isIn() {
        return in;
    }

    public void setIn(boolean in) {
        this.in = in;
    }

    public boolean isDisjunctive() {
        return disjunctive;
    }

    public void setDisjunctive(boolean disjunctive) {
        this.disjunctive = disjunctive;
    }

    public boolean isNegation() {
        return negation;
    }

    public void setNegation(boolean negation) {
        this.negation = negation;
    }

    public List<ListingPredicate> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<ListingPredicate> predicates) {
        this.predicates = predicates;
    }

}
