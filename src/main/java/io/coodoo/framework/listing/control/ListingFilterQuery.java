package io.coodoo.framework.listing.control;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

import io.coodoo.framework.listing.boundary.ListingQueryParams;
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

    public ListingFilterQuery<T> filter(String filter, String... attributes) {
        if (!StringUtils.isBlank(filter)) {
            filter = likeValue(filter);
            List<Predicate> predicates = new ArrayList<>();
            for (String attribute : attributes) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(attribute)), filter));
            }
            Predicate filterConstraint = criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()]));
            whereConstraints.add(filterConstraint);
        }

        return this;
    }

    public ListingFilterQuery<T> filterAllAttributes(String filter) {
        if (!StringUtils.isBlank(filter)) {

            List<Predicate> predicates = new ArrayList<>();

            // go for all fields that are defined as columns except if annotated with @ListingFilterIgnore
            for (Field field : getFields()) {
                if (field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(ListingFilterIgnore.class)) {

                    Predicate predicate = createPredicateAllowOrOperator(filter, field);

                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                }
            }
            Predicate filterConstraint = criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()]));
            whereConstraints.add(filterConstraint);
        }
        return this;
    }

    public ListingFilterQuery<T> filterByAttributes(Map<String, String> filterAttributes) {
        if (filterAttributes == null || filterAttributes.isEmpty()) {
            return this;
        }

        List<Field> fields = getFields();
        List<Predicate> predicates = new ArrayList<>();

        boolean disjunctivFilter = false; // default

        for (String filterAttribute : filterAttributes.keySet()) {

            String filter = filterAttributes.get(filterAttribute);

            // a filter can be applied on many attibutes, joined by a "|", those get conjuncted
            if (StringUtils.contains(filterAttribute, "|")) {

                Predicate orPredicate = criteriaBuilder.disjunction();
                for (String orAttribute : filterAttribute.split("\\|", -1)) {

                    Predicate predicate = filterByAttribute(fields, orAttribute.trim(), filter);
                    if (predicate != null) {
                        orPredicate = criteriaBuilder.or(orPredicate, predicate);
                    }
                }
                predicates.add(criteriaBuilder.and(orPredicate));

            } else { // just one attribute for one filter
                Predicate predicate = filterByAttribute(fields, filterAttribute, filter);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            }

            if (StringUtils.equals(ListingQueryParams.FILTER_TYPE_DISJUNCTION, filterAttribute)) {
                disjunctivFilter = true;
            }
        }

        if (disjunctivFilter && !predicates.isEmpty()) {
            // disjunctive filters
            whereConstraints.add(criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()])));
        } else {
            // conjunctive filters
            whereConstraints.addAll(predicates);
        }
        return this;
    }

    private Predicate filterByAttribute(List<Field> fields, String filterAttribute, String filter) {

        for (Field field : fields) {
            if (field.getName().equals(filterAttribute)) {
                return createPredicateAllowOrOperator(filter, field);
            }
        }
        return null;
    }

    private Predicate createPredicateAllowOrOperator(String filter, Field field) {

        // REVIEW: what about "," & ";" ?

        if (StringUtils.contains(filter, "|") || StringUtils.contains(filter, " OR ")) {

            List<String> orList = Arrays.asList(filter.replaceAll(" OR ", "|").trim().split("\\|", -1));

            // Too many OR-Predicates can cause a stack overflow, so higher numbers get processed in an IN statement
            if (orList.size() > 10) {
                return createInPredicate(orList, field);
            }
            Predicate orPredicate = criteriaBuilder.disjunction();
            for (String orfilter : orList) {

                Predicate predicate = createPredicateAllowNegation(orfilter.trim(), field);
                if (predicate != null) {
                    orPredicate = criteriaBuilder.or(orPredicate, predicate);
                }
            }
            return orPredicate;
        }
        return createPredicateAllowNegation(filter, field);
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

    private Predicate createPredicateAllowNegation(String filter, Field field) {

        String value = filter.trim();
        boolean negation = false;

        if (value.startsWith("!")) {
            value = value.replaceFirst("!", "");
            negation = true;
        }
        if (value.startsWith("NOT ")) {
            value = value.replaceFirst("NOT ", "");
            negation = true;
        }

        Predicate predicate = createPredicate(value, field);

        if (predicate != null && negation) {
            return criteriaBuilder.not(predicate);
        }
        return predicate;
    }

    private Predicate createPredicate(String filter, Field field) {

        // Nulls
        if (filter.matches("^NULL$")) {
            return criteriaBuilder.isNull(root.get(field.getName()));
        }

        // String
        if (field.getType().equals(String.class)) {

            // quoted values needs an exact match
            if (filter.startsWith("\"") && filter.endsWith("\"")) {
                return criteriaBuilder.equal(root.get(field.getName()), filter.replaceAll("^\"|\"$", ""));
            }
            return criteriaBuilder.like(root.get(field.getName()), likeValue(filter));
        }

        // Date
        if (field.getType().equals(LocalDateTime.class) || field.getType().equals(Date.class)) {
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;

            if (filter.contains("-")) { // Date range from - to
                String[] dateRange = filter.split("-");
                if (dateRange.length == 2) {
                    startDate = parseFilterDate(dateRange[0], false);
                    endDate = parseFilterDate(dateRange[1], true);
                }
            } else { // Date (year, month or day)
                startDate = parseFilterDate(filter, false);
                endDate = parseFilterDate(filter, true);
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
            if (filter.startsWith("\"") && filter.endsWith("\"")) {
                try {
                    Enum enumValue = Enum.valueOf((Class<Enum>) field.getType(), filter.replaceAll("^\"|\"$", ""));
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
                    return criteriaBuilder.like(root.get(field.getName()).as(String.class), likeValue(filter));
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
                    return criteriaBuilder.like(root.get(field.getName()).as(String.class), likeValue(filter));
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
                    return criteriaBuilder.like(root.get(field.getName()).as(String.class), likeValue(filter));
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

    private String likeValue(String value) {
        return "%" + value.toLowerCase() + "%";
    }

    private LocalDateTime parseFilterDate(String dateString, boolean end) {
        // YYYY
        Matcher yearMatcher = Pattern.compile("^(\\d{4})$").matcher(dateString);
        if (yearMatcher.find()) {
            LocalDateTime date = LocalDateTime.of(Integer.valueOf(yearMatcher.group(1)), 1, 1, 0, 0, 0);
            if (end) {
                return date.plusYears(1).minusSeconds(1);
            }
            return date;
        }
        // MM.YYYY
        Matcher monthYearMatcher = Pattern.compile("^(\\d{1,2})\\.(\\d{4})$").matcher(dateString);
        if (monthYearMatcher.find()) {
            LocalDateTime date = LocalDateTime.of(Integer.valueOf(monthYearMatcher.group(2)), Integer.valueOf(monthYearMatcher.group(1)), 1, 0, 0, 0);
            if (end) {
                return date.plusMonths(1).minusSeconds(1);
            }
            return date;
        }
        // DD.MM.YYYY
        Matcher dayMonthYearMatcher = Pattern.compile("^(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})$").matcher(dateString);
        if (dayMonthYearMatcher.find()) {
            LocalDateTime date = LocalDateTime.of(Integer.valueOf(dayMonthYearMatcher.group(3)), Integer.valueOf(dayMonthYearMatcher.group(2)),
                            Integer.valueOf(dayMonthYearMatcher.group(1)), 0, 0, 0);
            if (end) {
                return date.plusDays(1).minusSeconds(1);
            }
            return date;
        }
        return null;
    }

    private List<Field> getFields() {

        List<Field> fields = new ArrayList<>();

        Class<?> inheritanceClass = domainClass;
        while (inheritanceClass != null) {
            fields.addAll(Arrays.asList(inheritanceClass.getDeclaredFields()));
            inheritanceClass = inheritanceClass.getSuperclass();
        }
        return fields;
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
