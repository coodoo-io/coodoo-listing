package io.coodoo.framework.listing.control;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
            filter = "%" + filter.toLowerCase() + "%";
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

                    Predicate predicate = createPredicate(filter, field);

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

                    Predicate predicate = filterByAttribute(fields, orAttribute, filter);
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
                return createPredicate(filter, field);
            }
        }
        return null;
    }

    private Predicate createPredicate(String filter, Field field) {

        if (filter.startsWith("!")) {
            return createPredicate(filter.replaceFirst("!", ""), field, true);
        }
        return createPredicate(filter, field, false);
    }

    private Predicate createPredicate(String filter, Field field, boolean negation) {

        // String
        if (field.getType().equals(String.class)) {
            if (negation) {
                return criteriaBuilder.notLike(root.get(field.getName()), "%" + filter.toLowerCase() + "%");
            }
            return criteriaBuilder.like(root.get(field.getName()), "%" + filter.toLowerCase() + "%");
        }

        if (field.getType().equals(LocalDateTime.class) || field.getType().equals(Date.class)) {
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;

            if (filter.contains("-")) { // Datum von-bis
                String[] dateRange = filter.split("-");
                if (dateRange.length == 2) {
                    startDate = parseFilterDate(dateRange[0], true);
                    endDate = parseFilterDate(dateRange[1], false);
                }
            } else { // genau ein Datum (Jahr, Monat oder Tag)
                startDate = parseFilterDate(filter, true);
                endDate = parseFilterDate(filter, false);
            }
            if (startDate != null && endDate != null) {

                Predicate dateConjunction = criteriaBuilder.conjunction();
                if (field.getType().equals(Date.class)) {
                    dateConjunction = criteriaBuilder.and(dateConjunction,
                                    criteriaBuilder.greaterThan(root.get(field.getName()), Date.from(startDate.toInstant(ZoneOffset.UTC))));
                    dateConjunction = criteriaBuilder.and(dateConjunction,
                                    criteriaBuilder.lessThan(root.get(field.getName()), Date.from(endDate.toInstant(ZoneOffset.UTC))));
                } else {
                    dateConjunction = criteriaBuilder.and(dateConjunction, criteriaBuilder.greaterThan(root.get(field.getName()), startDate));
                    dateConjunction = criteriaBuilder.and(dateConjunction, criteriaBuilder.lessThan(root.get(field.getName()), endDate));
                }
                if (negation) {
                    return criteriaBuilder.not(dateConjunction);
                }
                return criteriaBuilder.and(dateConjunction);
            }
        }

        // Enum
        if (field.getType() instanceof Class && field.getType().isEnum()) {

            Predicate possibleEnumValues = criteriaBuilder.disjunction();

            for (Object enumValue : field.getType().getEnumConstants()) {
                if (enumValue.toString().toUpperCase().contains(((String) filter).toUpperCase())) {
                    Predicate possibleEnumValue = criteriaBuilder.equal(root.get(field.getName()), enumValue);
                    possibleEnumValues = criteriaBuilder.or(possibleEnumValues, possibleEnumValue);
                }
            }
            if (negation) {
                return criteriaBuilder.not(possibleEnumValues);
            }
            return criteriaBuilder.and(possibleEnumValues);
        }

        // Long
        if ((field.getType().equals(Long.class) || field.getType().equals(long.class)) && filter.matches("^-?\\d{1,37}$")) {
            if (negation) {
                return criteriaBuilder.equal(root.get(field.getName()), Long.valueOf(filter));
            }
            return criteriaBuilder.equal(root.get(field.getName()), Long.valueOf(filter));
        }

        // Integer
        if ((field.getType().equals(Integer.class) || field.getType().equals(int.class)) && filter.matches("^-?\\d{1,10}$")) {
            if (negation) {
                return criteriaBuilder.equal(root.get(field.getName()), Integer.valueOf(filter));
            }
            return criteriaBuilder.equal(root.get(field.getName()), Integer.valueOf(filter));
        }

        // Short
        if ((field.getType().equals(Short.class) || field.getType().equals(short.class)) && filter.matches("^-?\\d{1,5}$")) {
            if (negation) {
                return criteriaBuilder.equal(root.get(field.getName()), Short.valueOf(filter));
            }
            return criteriaBuilder.equal(root.get(field.getName()), Short.valueOf(filter));
        }

        return null;
    }

    private LocalDateTime parseFilterDate(String dateFilterString, boolean start) {
        // YYYY
        if (dateFilterString.matches("^\\d{4}$")) {
            if (start) {
                return LocalDateTime.of(Integer.valueOf(dateFilterString).intValue(), 1, 1, 0, 0, 0);
            } else {
                return LocalDateTime.of(Integer.valueOf(dateFilterString).intValue(), 1, 1, 0, 0, 0).plusYears(1).minusSeconds(1);
            }
        }
        // MM.YYYY
        if (dateFilterString.matches("^\\d{1,2}\\.\\d{4}$")) {
            String[] dateParts = dateFilterString.split("\\.");
            int year = Integer.valueOf(dateParts[1]).intValue();
            int month = Integer.valueOf(dateParts[0]).intValue();
            if (start) {
                return LocalDateTime.of(year, month, 1, 0, 0, 0);
            } else {
                return LocalDateTime.of(year, month, 1, 0, 0, 0).plusMonths(1).minusSeconds(1);
            }
        }
        // DD.MM.YYYY
        if (dateFilterString.matches("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$")) {
            String[] dateParts = dateFilterString.split("\\.");
            int year = Integer.valueOf(dateParts[2]).intValue();
            int month = Integer.valueOf(dateParts[1]).intValue();
            int day = Integer.valueOf(dateParts[0]).intValue();
            if (start) {
                return LocalDateTime.of(year, month, day, 0, 0, 0);
            } else {
                return LocalDateTime.of(year, month, day, 0, 0, 0).plusDays(1).minusSeconds(1);
            }
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
