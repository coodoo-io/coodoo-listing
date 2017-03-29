package io.coodoo.framework.listing.control;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import io.coodoo.framework.listing.boundary.ListingPredicate;
import io.coodoo.framework.listing.boundary.annotation.ListingFilterIgnore;
import io.coodoo.framework.listing.boundary.annotation.ListingLikeOnNumber;

/**
 * Creates a dynamic JPA query using Criteria API considering optional fields, e.g. a filter for attributes, sorting and result limit.
 * 
 * @param <T> The target entity
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ListingFilterQuery<T> {

    private EntityManager entityManager;
    private CriteriaBuilder criteriaBuilder;
    private CriteriaQuery query;
    private Root<T> root;
    private Class<T> domainClass;
    private List<Predicate> whereConstraints = new ArrayList<>();

    public ListingFilterQuery(EntityManager entityManager, Class<T> domainClass) {
        this.entityManager = entityManager;
        this.domainClass = domainClass;
        criteriaBuilder = entityManager.getCriteriaBuilder();
        query = criteriaBuilder.createQuery();
        root = query.from(domainClass);
    }

    public ListingFilterQuery<T> filterAllAttributes(String filter) {
        if (!StringUtils.isBlank(filter)) {

            Map<String, String> filterAttributes = new HashMap<>();
            filterAttributes.put(CoodooListingConfig.FILTER_TYPE_DISJUNCTION, "this just enables an OR-statement for all the fields");

            // go for all fields that are defined as columns except if annotated with @ListingFilterIgnore
            for (Field field : ListingUtil.getFields(domainClass)) {
                if (field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(ListingFilterIgnore.class)) {
                    filterAttributes.put(field.getName(), filter);
                }
            }
            return filterByAttributes(filterAttributes);
        }
        return this;
    }

    public ListingFilterQuery<T> filterByAttributes(Map<String, String> filterAttributes) {
        if (filterAttributes != null && !filterAttributes.isEmpty()) {

            ListingPredicate listingPredicate = new ListingPredicate().and();

            for (String attribute : filterAttributes.keySet()) {

                if (StringUtils.equals(CoodooListingConfig.FILTER_TYPE_DISJUNCTION, attribute)) {
                    listingPredicate = listingPredicate.or(); // changing filter to disjunctive
                }
                String filter = filterAttributes.get(attribute);

                if (StringUtils.contains(attribute, CoodooListingConfig.OPERATOR_OR)) {
                    // a filter can be applied on many fields, joined by a "|", those get conjuncted
                    listingPredicate.addPredicate(new ListingPredicate().or().predicates(ListingUtil.split(attribute).stream()
                                    .map(orAttribute -> createListingPredicate(orAttribute, filter)).collect(Collectors.toList())));
                } else {
                    // just one attribute for one filter
                    listingPredicate.addPredicate(createListingPredicate(attribute, filter));
                }
            }
            addToWhereConstraint(listingPredicate);
        }
        return this;
    }

    public ListingFilterQuery<T> filterByPredicate(ListingPredicate listingPredicate) {

        addToWhereConstraint(listingPredicate);
        return this;
    }

    private void addToWhereConstraint(ListingPredicate listingPredicate) {
        if (listingPredicate != null) {

            Predicate predicate = null;
            List<ListingPredicate> filters = new ArrayList<>();
            Map<String, Field> fieldMap = ListingUtil.getFields(domainClass).stream().collect(Collectors.toMap(field -> field.getName(), field -> field));

            if (listingPredicate.hasPredicates()) {
                filters.addAll(listingPredicate.getPredicates());
            } else {
                filters.add(new ListingPredicate().filter(listingPredicate.getAttribute(), listingPredicate.getFilter()));
            }
            predicate = filterByPredicateTree(listingPredicate.isDisjunctive(), listingPredicate.isNegation(), filters, fieldMap);

            if (predicate != null) {
                if (listingPredicate.isNegation()) {
                    predicate = criteriaBuilder.not(predicate);
                }
                whereConstraints.add(predicate);
            }
        }
    }

    private ListingPredicate createListingPredicate(String attribute, String filter) {

        if (StringUtils.contains(filter, CoodooListingConfig.OPERATOR_OR) || StringUtils.contains(filter, CoodooListingConfig.OPERATOR_OR_WORD)) {

            List<String> orList = ListingUtil.split(filter.replaceAll(CoodooListingConfig.OPERATOR_OR_WORD, CoodooListingConfig.OPERATOR_OR));
            if (orList.size() > CoodooListingConfig.OR_TO_IN_LIMIT) {
                // Too many OR-Predicates can cause a stack overflow, so higher numbers get processed in an IN statement
                return new ListingPredicate().in().filter(attribute, filter.replaceAll(CoodooListingConfig.OPERATOR_OR_WORD, CoodooListingConfig.OPERATOR_OR));
            }
            return new ListingPredicate().or()
                            .predicates(orList.stream().map(orfilter -> createListingPredicateFilter(attribute, orfilter)).collect(Collectors.toList()));
        }
        return createListingPredicateFilter(attribute, filter);
    }

    private ListingPredicate createListingPredicateFilter(String attribute, String filter) {
        if (filter.startsWith(CoodooListingConfig.OPERATOR_NOT)) {
            return new ListingPredicate().not().filter(attribute, filter.replaceFirst(CoodooListingConfig.OPERATOR_NOT, ""));
        }
        if (filter.startsWith(CoodooListingConfig.OPERATOR_NOT_WORD)) {
            return new ListingPredicate().not().filter(attribute, filter.replaceFirst(CoodooListingConfig.OPERATOR_NOT_WORD, ""));
        }
        return new ListingPredicate().filter(attribute, filter);
    }

    private Predicate filterByPredicateTree(boolean disjunctive, boolean negation, List<ListingPredicate> listingPredicates, Map<String, Field> fieldMap) {

        if (listingPredicates != null && !listingPredicates.isEmpty()) {

            List<Predicate> predicates = new ArrayList<>();
            for (ListingPredicate listingPredicate : listingPredicates) {

                Predicate predicate = null;
                if (listingPredicate.hasPredicates()) {

                    // process child predicates
                    predicate = filterByPredicateTree(listingPredicate.isDisjunctive(), listingPredicate.isNegation(), listingPredicate.getPredicates(),
                                    fieldMap);

                } else if (StringUtils.isNoneEmpty(listingPredicate.getAttribute()) && StringUtils.isNoneEmpty(listingPredicate.getFilter())) {

                    if (!fieldMap.containsKey(listingPredicate.getAttribute())) {
                        continue; // given fieldName does not exist in domainClass
                    }
                    // add predicate
                    if (listingPredicate.isIn()) {
                        predicate = createInPredicate(ListingUtil.split(listingPredicate.getFilter()), fieldMap.get(listingPredicate.getAttribute()));
                    } else {
                        predicate = createPredicate(listingPredicate.getFilter(), fieldMap.get(listingPredicate.getAttribute()));
                    }
                }
                if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            if (!predicates.isEmpty()) {
                Predicate predicate = null;
                if (disjunctive) {
                    predicate = criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()]));
                } else {
                    predicate = criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
                if (negation) {
                    return criteriaBuilder.not(predicate);
                }
                return predicate;
            }
        }
        return null;
    }

    private Predicate createPredicate(String filter, Field field) {

        // Nulls
        if (filter.matches("^" + CoodooListingConfig.OPERATOR_NULL + "$")) {
            return criteriaBuilder.isNull(root.get(field.getName()));
        }

        // String
        if (field.getType().equals(String.class)) {

            // quoted values needs an exact match
            if (ListingUtil.isQuoted(filter)) {
                return criteriaBuilder.equal(root.get(field.getName()), ListingUtil.removeQuotes(filter));
            }
            return criteriaBuilder.like(root.get(field.getName()), ListingUtil.likeValue(filter));
        }

        // Date
        if (field.getType().equals(LocalDateTime.class) || field.getType().equals(Date.class)) {
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;

            if (filter.contains("-")) { // Date range from - to
                String[] dateRange = filter.split("-");
                if (dateRange.length == 2) {
                    startDate = ListingUtil.parseFilterDate(dateRange[0], false);
                    endDate = ListingUtil.parseFilterDate(dateRange[1], true);
                }
            } else { // Date (year, month or day)
                startDate = ListingUtil.parseFilterDate(filter, false);
                endDate = ListingUtil.parseFilterDate(filter, true);
            }
            if (startDate != null && endDate != null) {

                Predicate date = criteriaBuilder.conjunction();
                if (field.getType().equals(Date.class)) {
                    date = criteriaBuilder.and(date, criteriaBuilder.greaterThan(root.get(field.getName()), Date.from(startDate.toInstant(ZoneOffset.UTC))));
                    date = criteriaBuilder.and(date, criteriaBuilder.lessThan(root.get(field.getName()), Date.from(endDate.toInstant(ZoneOffset.UTC))));
                } else {
                    date = criteriaBuilder.and(date, criteriaBuilder.greaterThan(root.get(field.getName()), startDate));
                    date = criteriaBuilder.and(date, criteriaBuilder.lessThan(root.get(field.getName()), endDate));
                }
                return criteriaBuilder.and(date);
            }
        }

        // Enum
        if (field.getType() instanceof Class && field.getType().isEnum()) {

            // quoted values needs an exact match
            if (ListingUtil.isQuoted(filter)) {
                try {
                    Enum enumValue = Enum.valueOf((Class<Enum>) field.getType(), ListingUtil.removeQuotes(filter));
                    return criteriaBuilder.equal(root.get(field.getName()), enumValue);
                } catch (IllegalArgumentException e) {
                }
            }

            Predicate possibleEnumValues = criteriaBuilder.disjunction();
            for (Object enumValue : field.getType().getEnumConstants()) {
                if (enumValue.toString().toUpperCase().contains(((String) filter).toUpperCase())) {
                    Predicate possibleEnumValue = criteriaBuilder.equal(root.get(field.getName()), enumValue);
                    possibleEnumValues = criteriaBuilder.or(possibleEnumValues, possibleEnumValue);
                }
            }
            return criteriaBuilder.and(possibleEnumValues);
        }

        // Long
        if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
            if (filter.matches("^-?\\d{1,37}$")) {
                if (field.isAnnotationPresent(ListingLikeOnNumber.class)) {
                    return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(filter));
                }
                return criteriaBuilder.equal(root.get(field.getName()), Long.valueOf(filter));
            }
            Matcher longRange = Pattern.compile("(^-?\\d{1,37})-(-?\\d{1,37}$)").matcher(filter);
            if (longRange.find()) {
                return criteriaBuilder.between(root.get(field.getName()), Long.valueOf(longRange.group(1)), Long.valueOf(longRange.group(2)));
            }
        }

        // Integer
        if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
            if (filter.matches("^-?\\d{1,10}$")) {
                if (field.isAnnotationPresent(ListingLikeOnNumber.class)) {
                    return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(filter));
                }
                return criteriaBuilder.equal(root.get(field.getName()), Integer.valueOf(filter));
            }
            Matcher intRange = Pattern.compile("(^-?\\d{1,10})-(-?\\d{1,10}$)").matcher(filter);
            if (intRange.find()) {
                return criteriaBuilder.between(root.get(field.getName()), Integer.valueOf(intRange.group(1)), Integer.valueOf(intRange.group(2)));
            }
        }

        // Short
        if (field.getType().equals(Short.class) || field.getType().equals(short.class)) {
            if (filter.matches("^-?\\d{1,5}$")) {
                if (field.isAnnotationPresent(ListingLikeOnNumber.class)) {
                    return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(filter));
                }
                return criteriaBuilder.equal(root.get(field.getName()), Short.valueOf(filter));
            }
            Matcher shortRange = Pattern.compile("(^-?\\d{1,5})-(-?\\d{1,5}$)").matcher(filter);
            if (shortRange.find()) {
                return criteriaBuilder.between(root.get(field.getName()), Short.valueOf(shortRange.group(1)), Short.valueOf(shortRange.group(2)));
            }
        }

        // Boolean
        if ((field.getType().equals(Boolean.class) || field.getType().equals(boolean.class))
                        && (filter.toLowerCase().equals("true") || filter.toLowerCase().equals("false"))) {

            Boolean booleanValue = filter.toLowerCase().equals("true");
            return criteriaBuilder.equal(root.get(field.getName()), booleanValue);
        }

        return null;
    }

    private Predicate createInPredicate(List<String> inList, Field field) {

        if (field.getType().equals(String.class)) {
            return criteriaBuilder.isTrue(root.get(field.getName()).in(inList));
        }
        if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
            List<Long> inListLong = inList.stream().map(Long::parseLong).collect(Collectors.toList());
            return criteriaBuilder.isTrue(root.get(field.getName()).in(inListLong));
        }
        if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
            List<Integer> inListInt = inList.stream().map(Integer::parseInt).collect(Collectors.toList());
            return criteriaBuilder.isTrue(root.get(field.getName()).in(inListInt));
        }
        if (field.getType().equals(Short.class) || field.getType().equals(short.class)) {
            List<Short> inListShort = inList.stream().map(Short::parseShort).collect(Collectors.toList());
            return criteriaBuilder.isTrue(root.get(field.getName()).in(inListShort));
        }
        if (field.getType() instanceof Class && field.getType().isEnum()) {
            List<Enum> inListEnum = new ArrayList<Enum>();
            for (String enumString : inList) {
                try {
                    inListEnum.add(Enum.valueOf((Class<Enum>) field.getType(), enumString));
                } catch (IllegalArgumentException e) {
                }
            }
            return criteriaBuilder.isTrue(root.get(field.getName()).in(inListEnum));
        }
        return null;
    }

    public ListingFilterQuery<T> filter(String filter, String... attributes) {
        if (!StringUtils.isBlank(filter)) {
            filter = ListingUtil.likeValue(filter);
            List<Predicate> predicates = new ArrayList<>();
            for (String attribute : attributes) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(attribute)), filter));
            }
            Predicate filterConstraint = criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()]));
            whereConstraints.add(filterConstraint);
        }

        return this;
    }

    public ListingFilterQuery<T> addIsNullConstraint(String attribute) {
        whereConstraints.add(criteriaBuilder.isNull(root.get(attribute)));
        return this;
    }

    public ListingFilterQuery<T> addEqualsConstraint(String attribute, Enum value) {
        whereConstraints.add(criteriaBuilder.equal(root.get(attribute), value));
        return this;
    }

    public ListingFilterQuery<T> addEqualsNotConstraint(String attribute, Enum value) {
        whereConstraints.add(criteriaBuilder.notEqual(root.get(attribute), value));
        return this;
    }

    public ListingFilterQuery<T> sort(String attribute, boolean asc) {
        if (attribute == null) {
            return this;
        }
        if (asc) {
            query.orderBy(criteriaBuilder.asc(root.get(attribute)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(attribute)));
        }
        return this;
    }

    public CriteriaQuery<T> getQuery() {
        query.select(root);
        query.where(criteriaBuilder.and(whereConstraints.toArray(new Predicate[whereConstraints.size()])));
        return query;
    }

    public CriteriaQuery<Long> getQueryForCount() {
        query.select(criteriaBuilder.count(root));
        query.where(criteriaBuilder.and(whereConstraints.toArray(new Predicate[whereConstraints.size()])));
        return query;
    }

    public List<T> list() {
        return list(null, null);
    }

    public List<T> list(Integer startPosition, Integer limit) {
        TypedQuery<T> typedQuery = entityManager.createQuery(this.getQuery());
        if (startPosition != null) {
            typedQuery.setFirstResult(startPosition);
        }
        if (limit != null) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    public Long count() {
        TypedQuery<Long> q = this.entityManager.createQuery(this.getQueryForCount());
        return q.getSingleResult();
    }

}
