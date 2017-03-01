package io.coodoo.framework.listing.boundary;

import java.util.ArrayList;
import java.util.List;

public class ListingPredicate {

    private String attribute;

    private String filter;

    private boolean disjunctive;

    private boolean negation;

    private List<ListingPredicate> predicates;

    /**
     * Constructor
     */
    public ListingPredicate() {
        this.disjunctive = false;
        this.negation = false;
        this.predicates = new ArrayList<>();
    }

    /**
     * Constructor (conjunctive query (AND) by default)
     * 
     * @param attribute target attribute
     * @param filter filter value
     */
    public ListingPredicate(String attribute, String filter) {
        this(attribute, filter, false, false);
    }

    /**
     * Constructor
     * 
     * @param attribute target attribute
     * @param filter filter value
     * @param disjunctive disjunctive query (OR)
     */
    public ListingPredicate(String attribute, String filter, boolean disjunctive) {
        this(attribute, filter, disjunctive, false);
    }

    /**
     * Constructor
     * 
     * @param attribute target attribute
     * @param filter filter value
     * @param disjunctive disjunctive query (OR)
     * @param negation negation of result
     */
    public ListingPredicate(String attribute, String filter, boolean disjunctive, boolean negation) {
        super();
        this.attribute = attribute;
        this.filter = filter;
        this.disjunctive = disjunctive;
        this.negation = negation;
        this.predicates = new ArrayList<>();
    }

    /**
     * Constructor (conjunctive query (AND) by default)
     * 
     * @param predicates
     */
    public ListingPredicate(List<ListingPredicate> predicates) {
        this(false, false, predicates);
    }

    /**
     * Constructor
     * 
     * @param disjunctive disjunctive query (OR)
     * @param predicates filter predicates
     */
    public ListingPredicate(boolean disjunctive, List<ListingPredicate> predicates) {
        this(disjunctive, false, predicates);
    }

    /**
     * Constructor
     * 
     * @param disjunctive disjunctive query (OR)
     * @param negation negation of result
     * @param predicates filter predicates
     */
    public ListingPredicate(boolean disjunctive, boolean negation, List<ListingPredicate> predicates) {
        super();
        this.disjunctive = disjunctive;
        this.negation = negation;
        if (predicates != null) {
            this.predicates = predicates;
        } else {
            this.predicates = new ArrayList<>();
        }
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
        return "ListingPredicate [attribute=" + attribute + ", filter=" + filter + ", disjunctive=" + disjunctive + ", negation=" + negation + ", predicates="
                        + predicates + "]";
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
