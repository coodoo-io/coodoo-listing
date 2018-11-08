package io.coodoo.framework.listing.control;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import io.coodoo.framework.listing.boundary.ListingPredicate;
import io.coodoo.framework.listing.boundary.annotation.ListingLikeOnNumber;

/**
 * Creates a dynamic JPA query using Criteria API considering optional fields, e.g. a filter for attributes, sorting and result limit.
 * 
 * @param <T> The target entity
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ListingQuery<T> {

    private EntityManager entityManager;
    private CriteriaBuilder criteriaBuilder;
    private CriteriaQuery query;
    private Root<T> root;
    private Class<T> domainClass;
    private List<Predicate> whereConstraints = new ArrayList<>();

    public ListingQuery(EntityManager entityManager, Class<T> domainClass) {
        this.entityManager = entityManager;
        this.domainClass = domainClass;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.query = criteriaBuilder.createQuery();
        this.root = query.from(domainClass);
    }

    public ListingQuery<T> filterAllAttributes(String filter) {
        if (!StringUtils.isBlank(filter)) {

            Map<String, String> filterAttributes = new HashMap<>();
            filterAttributes.put(ListingConfig.FILTER_TYPE_DISJUNCTION, "this just enables an OR-statement for all the fields");

            ListingUtil.getFields(domainClass).forEach(field -> filterAttributes.put(field.getName(), filter));
            return filterByAttributes(filterAttributes);
        }
        return this;
    }

    public ListingQuery<T> filterByAttributes(Map<String, String> filterAttributes) {
        if (filterAttributes != null && !filterAttributes.isEmpty()) {

            ListingPredicate listingPredicate = new ListingPredicate().and();

            for (String attribute : filterAttributes.keySet()) {

                if (StringUtils.equals(ListingConfig.FILTER_TYPE_DISJUNCTION, attribute)) {
                    listingPredicate = listingPredicate.or(); // changing filter to disjunctive
                }
                String filter = filterAttributes.get(attribute);

                if (StringUtils.contains(attribute, ListingConfig.OPERATOR_OR)) {
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

    public ListingQuery<T> filterByPredicate(ListingPredicate listingPredicate) {

        addToWhereConstraint(listingPredicate);
        return this;
    }

    private void addToWhereConstraint(ListingPredicate listingPredicate) {
        if (listingPredicate != null) {

            Predicate predicate = null;
            List<ListingPredicate> filters = new ArrayList<>();
            Map<String, Field> fieldMap = ListingUtil.getFields(domainClass, true).stream().collect(Collectors.toMap(field -> field.getName(), field -> field));

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

        if (StringUtils.contains(filter, ListingConfig.OPERATOR_OR) || StringUtils.contains(filter, ListingConfig.OPERATOR_OR_WORD)) {

            List<String> orList = ListingUtil.split(filter.replaceAll(ListingUtil.escape(ListingConfig.OPERATOR_OR_WORD), ListingConfig.OPERATOR_OR));
            if (orList.size() > ListingConfig.OR_LIMIT) {
                // Too many OR-Predicates can cause a stack overflow, so higher numbers get processed in an IN statement
                return new ListingPredicate().in().filter(attribute,
                                filter.replaceAll(ListingUtil.escape(ListingConfig.OPERATOR_OR_WORD), ListingConfig.OPERATOR_OR));
            }
            return new ListingPredicate().or()
                            .predicates(orList.stream().map(orfilter -> createListingPredicateFilter(attribute, orfilter)).collect(Collectors.toList()));
        }
        return createListingPredicateFilter(attribute, filter);
    }

    private ListingPredicate createListingPredicateFilter(String attribute, String filter) {
        if (filter.startsWith(ListingConfig.OPERATOR_NOT)) {
            return new ListingPredicate().not().filter(attribute, filter.replaceFirst(ListingConfig.OPERATOR_NOT, ""));
        }
        if (filter.startsWith(ListingConfig.OPERATOR_NOT_WORD)) {
            return new ListingPredicate().not().filter(attribute, filter.replaceFirst(ListingConfig.OPERATOR_NOT_WORD, ""));
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

                    if (listingPredicate.isNegation()) {
                        predicate = criteriaBuilder.not(predicate);
                    }
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
        if (filter.matches("^" + ListingConfig.OPERATOR_NULL + "$")) {
            return criteriaBuilder.isNull(root.get(field.getName()));
        }

        switch (field.getType().getSimpleName()) {

            case "String":

                // quoted values needs an exact match
                if (ListingUtil.isQuoted(filter)) {
                    return criteriaBuilder.equal(root.get(field.getName()), ListingUtil.removeQuotes(filter));
                }
                return criteriaBuilder.like(criteriaBuilder.lower(root.get(field.getName())), ListingUtil.likeValue(filter));

            // case "LocalDate": Doesn't work in JPA 2.0...
            case "LocalDateTime":

                if (ListingUtil.validDate(filter)) {
                    return criteriaBuilder.between(root.get(field.getName()), ListingUtil.parseDateTime(filter, false),
                                    ListingUtil.parseDateTime(filter, true));
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LT) || filter.startsWith(ListingConfig.OPERATOR_LT_WORD)) {
                    String ltFilter = filter.replace(ListingConfig.OPERATOR_LT, "").replace(ListingConfig.OPERATOR_LT_WORD, "");
                    if (ListingUtil.validDate(ltFilter)) {
                        return criteriaBuilder.lessThan(root.get(field.getName()), ListingUtil.parseDateTime(ltFilter, false));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_GT) || filter.startsWith(ListingConfig.OPERATOR_GT_WORD)) {
                    String gtFilter = filter.replace(ListingConfig.OPERATOR_GT, "").replace(ListingConfig.OPERATOR_GT_WORD, "");
                    if (ListingUtil.validDate(gtFilter)) {
                        return criteriaBuilder.greaterThan(root.get(field.getName()), ListingUtil.parseDateTime(gtFilter, true));
                    }
                }
                Matcher dateTimeRange = Pattern.compile(ListingUtil.rangePatternDate()).matcher(filter);
                if (dateTimeRange.find()) {
                    return criteriaBuilder.between(root.get(field.getName()), ListingUtil.parseDateTime(dateTimeRange.group(1), false),
                                    ListingUtil.parseDateTime(dateTimeRange.group(8), true));
                }
                break;

            case "Date":
                if (ListingUtil.validDate(filter)) {
                    return criteriaBuilder.between(root.get(field.getName()), ListingUtil.parseDate(filter, false), ListingUtil.parseDate(filter, true));
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LT) || filter.startsWith(ListingConfig.OPERATOR_LT_WORD)) {
                    String ltFilter = filter.replace(ListingConfig.OPERATOR_LT, "").replace(ListingConfig.OPERATOR_LT_WORD, "");
                    if (ListingUtil.validDate(ltFilter)) {
                        return criteriaBuilder.lessThan(root.get(field.getName()), ListingUtil.parseDate(ltFilter, false));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_GT) || filter.startsWith(ListingConfig.OPERATOR_GT_WORD)) {
                    String gtFilter = filter.replace(ListingConfig.OPERATOR_GT, "").replace(ListingConfig.OPERATOR_GT_WORD, "");
                    if (ListingUtil.validDate(gtFilter)) {
                        return criteriaBuilder.greaterThan(root.get(field.getName()), ListingUtil.parseDate(gtFilter, true));
                    }
                }
                Matcher dateRange = Pattern.compile(ListingUtil.rangePatternDate()).matcher(filter);
                if (dateRange.find()) {
                    return criteriaBuilder.between(root.get(field.getName()), ListingUtil.parseDate(dateRange.group(1), false),
                                    ListingUtil.parseDate(dateRange.group(8), true));
                }
                break;

            case "Long":
            case "long":

                if (ListingUtil.validLong(filter)) {
                    if (field.isAnnotationPresent(ListingLikeOnNumber.class)) {
                        return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(filter));
                    }
                    return criteriaBuilder.equal(root.get(field.getName()), Long.valueOf(filter));
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LIKE) || filter.startsWith(ListingConfig.OPERATOR_LIKE_WORD)) {
                    String likeFilter = filter.replace(ListingConfig.OPERATOR_LIKE, "").replace(ListingConfig.OPERATOR_LIKE_WORD, "");
                    if (ListingUtil.validLong(likeFilter)) {
                        return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(likeFilter));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LT) || filter.startsWith(ListingConfig.OPERATOR_LT_WORD)) {
                    String ltFilter = filter.replace(ListingConfig.OPERATOR_LT, "").replace(ListingConfig.OPERATOR_LT_WORD, "");
                    if (ListingUtil.validLong(ltFilter)) {
                        return criteriaBuilder.lessThan(root.get(field.getName()), Long.valueOf(ltFilter));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_GT) || filter.startsWith(ListingConfig.OPERATOR_GT_WORD)) {
                    String gtFilter = filter.replace(ListingConfig.OPERATOR_GT, "").replace(ListingConfig.OPERATOR_GT_WORD, "");
                    if (ListingUtil.validLong(gtFilter)) {
                        return criteriaBuilder.greaterThan(root.get(field.getName()), Long.valueOf(gtFilter));
                    }
                }
                Matcher longRange = Pattern.compile(ListingUtil.rangePatternLong()).matcher(filter);
                if (longRange.find()) {
                    return criteriaBuilder.between(root.get(field.getName()), Long.valueOf(longRange.group(1)), Long.valueOf(longRange.group(3)));
                }
                break;

            case "Integer":
            case "int":

                if (ListingUtil.validInt(filter)) {
                    if (field.isAnnotationPresent(ListingLikeOnNumber.class)) {
                        return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(filter));
                    }
                    return criteriaBuilder.equal(root.get(field.getName()), Integer.valueOf(filter));
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LIKE) || filter.startsWith(ListingConfig.OPERATOR_LIKE_WORD)) {
                    String likeFilter = filter.replace(ListingConfig.OPERATOR_LIKE, "").replace(ListingConfig.OPERATOR_LIKE_WORD, "");
                    if (ListingUtil.validInt(likeFilter)) {
                        return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(likeFilter));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LT) || filter.startsWith(ListingConfig.OPERATOR_LT_WORD)) {
                    String ltFilter = filter.replace(ListingConfig.OPERATOR_LT, "").replace(ListingConfig.OPERATOR_LT_WORD, "");
                    if (ListingUtil.validInt(ltFilter)) {
                        return criteriaBuilder.lessThan(root.get(field.getName()), Integer.valueOf(ltFilter));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_GT) || filter.startsWith(ListingConfig.OPERATOR_GT_WORD)) {
                    String gtFilter = filter.replace(ListingConfig.OPERATOR_GT, "").replace(ListingConfig.OPERATOR_GT_WORD, "");
                    if (ListingUtil.validInt(gtFilter)) {
                        return criteriaBuilder.greaterThan(root.get(field.getName()), Integer.valueOf(gtFilter));
                    }
                }
                Matcher intRange = Pattern.compile(ListingUtil.rangePatternInt()).matcher(filter);
                if (intRange.find()) {
                    return criteriaBuilder.between(root.get(field.getName()), Integer.valueOf(intRange.group(1)), Integer.valueOf(intRange.group(3)));
                }
                break;

            case "Short":
            case "short":

                if (ListingUtil.validShort(filter)) {
                    if (field.isAnnotationPresent(ListingLikeOnNumber.class)) {
                        return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(filter));
                    }
                    return criteriaBuilder.equal(root.get(field.getName()), Short.valueOf(filter));
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LIKE) || filter.startsWith(ListingConfig.OPERATOR_LIKE_WORD)) {
                    String likeFilter = filter.replace(ListingConfig.OPERATOR_LIKE, "").replace(ListingConfig.OPERATOR_LIKE_WORD, "");
                    if (ListingUtil.validShort(likeFilter)) {
                        return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(likeFilter));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LT) || filter.startsWith(ListingConfig.OPERATOR_LT_WORD)) {
                    String ltFilter = filter.replace(ListingConfig.OPERATOR_LT, "").replace(ListingConfig.OPERATOR_LT_WORD, "");
                    if (ListingUtil.validShort(ltFilter)) {
                        return criteriaBuilder.lessThan(root.get(field.getName()), Short.valueOf(ltFilter));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_GT) || filter.startsWith(ListingConfig.OPERATOR_GT_WORD)) {
                    String gtFilter = filter.replace(ListingConfig.OPERATOR_GT, "").replace(ListingConfig.OPERATOR_GT_WORD, "");
                    if (ListingUtil.validShort(gtFilter)) {
                        return criteriaBuilder.greaterThan(root.get(field.getName()), Short.valueOf(gtFilter));
                    }
                }
                Matcher shortRange = Pattern.compile(ListingUtil.rangePatternShort()).matcher(filter);
                if (shortRange.find()) {
                    return criteriaBuilder.between(root.get(field.getName()), Short.valueOf(shortRange.group(1)), Short.valueOf(shortRange.group(3)));
                }
                break;

            case "Float":
            case "float":

                if (ListingUtil.validFloat(filter)) {
                    if (field.isAnnotationPresent(ListingLikeOnNumber.class)) {
                        return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(toFloat(filter).toString()));
                    }
                    return criteriaBuilder.equal(root.get(field.getName()), toFloat(filter));
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LIKE) || filter.startsWith(ListingConfig.OPERATOR_LIKE_WORD)) {
                    String likeFilter = filter.replace(ListingConfig.OPERATOR_LIKE, "").replace(ListingConfig.OPERATOR_LIKE_WORD, "");
                    if (ListingUtil.validFloat(likeFilter)) {
                        return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(likeFilter));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LT) || filter.startsWith(ListingConfig.OPERATOR_LT_WORD)) {
                    String ltFilter = filter.replace(ListingConfig.OPERATOR_LT, "").replace(ListingConfig.OPERATOR_LT_WORD, "");
                    if (ListingUtil.validFloat(ltFilter)) {
                        return criteriaBuilder.lessThan(root.get(field.getName()), toFloat(ltFilter));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_GT) || filter.startsWith(ListingConfig.OPERATOR_GT_WORD)) {
                    String gtFilter = filter.replace(ListingConfig.OPERATOR_GT, "").replace(ListingConfig.OPERATOR_GT_WORD, "");
                    if (ListingUtil.validFloat(gtFilter)) {
                        return criteriaBuilder.greaterThan(root.get(field.getName()), toFloat(gtFilter));
                    }
                }
                Matcher floatRange = Pattern.compile(ListingUtil.rangePatternFloat()).matcher(filter);
                if (floatRange.find()) {
                    return criteriaBuilder.between(root.get(field.getName()), toFloat(floatRange.group(1)), toFloat(floatRange.group(3)));
                }
                break;

            case "Double":
            case "double":

                if (ListingUtil.validDouble(filter)) {
                    if (field.isAnnotationPresent(ListingLikeOnNumber.class)) {
                        return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(toDouble(filter).toString()));
                    }
                    return criteriaBuilder.equal(root.get(field.getName()), toDouble(filter));
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LIKE) || filter.startsWith(ListingConfig.OPERATOR_LIKE_WORD)) {
                    String likeFilter = filter.replace(ListingConfig.OPERATOR_LIKE, "").replace(ListingConfig.OPERATOR_LIKE_WORD, "");
                    if (ListingUtil.validDouble(likeFilter)) {
                        return criteriaBuilder.like(root.get(field.getName()).as(String.class), ListingUtil.likeValue(likeFilter));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_LT) || filter.startsWith(ListingConfig.OPERATOR_LT_WORD)) {
                    String ltFilter = filter.replace(ListingConfig.OPERATOR_LT, "").replace(ListingConfig.OPERATOR_LT_WORD, "");
                    if (ListingUtil.validDouble(ltFilter)) {
                        return criteriaBuilder.lessThan(root.get(field.getName()), toDouble(ltFilter));
                    }
                }
                if (filter.startsWith(ListingConfig.OPERATOR_GT) || filter.startsWith(ListingConfig.OPERATOR_GT_WORD)) {
                    String gtFilter = filter.replace(ListingConfig.OPERATOR_GT, "").replace(ListingConfig.OPERATOR_GT_WORD, "");
                    if (ListingUtil.validDouble(gtFilter)) {
                        return criteriaBuilder.greaterThan(root.get(field.getName()), toDouble(gtFilter));
                    }
                }
                Matcher doubleRange = Pattern.compile(ListingUtil.rangePatternDouble()).matcher(filter);
                if (doubleRange.find()) {
                    return criteriaBuilder.between(root.get(field.getName()), toDouble(doubleRange.group(1)), toDouble(doubleRange.group(3)));
                }
                break;

            case "Boolean":
            case "boolean":

                if (filter.toLowerCase().equals("true") || filter.toLowerCase().equals("false")) {

                    Boolean booleanValue = filter.toLowerCase().equals("true");
                    return criteriaBuilder.equal(root.get(field.getName()), booleanValue);
                }
                break;

            default:

                // Enum
                if (field.getType().isEnum()) {

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
                break;
        }
        return null;
    }

    private Predicate createInPredicate(List<String> inList, Field field) {

        List<?> list = null;

        switch (field.getType().getSimpleName()) {

            case "String":
                list = inList;
                break;
            case "Long":
            case "long":
                list = inList.stream().filter(x -> ListingUtil.validLong(x)).map(Long::valueOf).collect(Collectors.toList());
                break;
            case "Integer":
            case "int":
                list = inList.stream().filter(x -> ListingUtil.validInt(x)).map(Integer::valueOf).collect(Collectors.toList());
                break;
            case "Short":
            case "short":
                list = inList.stream().filter(x -> ListingUtil.validShort(x)).map(Short::valueOf).collect(Collectors.toList());
                break;
            case "Float":
            case "float":
                list = inList.stream().filter(x -> ListingUtil.validFloat(x)).map(x -> toFloat(x)).collect(Collectors.toList());
                break;
            case "Double":
            case "double":
                list = inList.stream().filter(x -> ListingUtil.validDouble(x)).map(x -> toDouble(x)).collect(Collectors.toList());
                break;
            default:
                // Enum
                if (field.getType().isEnum()) {
                    List<Enum> inListEnum = new ArrayList<Enum>();
                    for (String enumString : inList) {
                        try {
                            inListEnum.add(Enum.valueOf((Class<Enum>) field.getType(), enumString));
                        } catch (IllegalArgumentException e) {
                        }
                    }
                    list = inListEnum;
                }
                break;
        }
        if (list != null) {
            return criteriaBuilder.isTrue(root.get(field.getName()).in(list));
        }
        return null;
    }

    private Double toDouble(String value) {
        return Double.valueOf(value.replace(",", "."));
    }

    private Float toFloat(String value) {
        return Float.valueOf(value.replace(",", "."));
    }

    public ListingQuery<T> filter(String filter, String... attributes) {
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

    public ListingQuery<T> addIsNullConstraint(String attribute) {
        whereConstraints.add(criteriaBuilder.isNull(root.get(attribute)));
        return this;
    }

    public ListingQuery<T> addEqualsConstraint(String attribute, Enum value) {
        whereConstraints.add(criteriaBuilder.equal(root.get(attribute), value));
        return this;
    }

    public ListingQuery<T> addEqualsNotConstraint(String attribute, Enum value) {
        whereConstraints.add(criteriaBuilder.notEqual(root.get(attribute), value));
        return this;
    }

    public ListingQuery<T> sort(String attribute) {

        if (attribute != null && !attribute.isEmpty()) {

            String[] attributes = attribute.trim().split(";");

            if (attributes.length == 1) {
                query.orderBy(getOrder(attribute));
            } else {
                Order[] orders = new Order[attributes.length];
                for (int i = 0; i < attributes.length; i++) {
                    orders[i] = getOrder(attributes[i]);
                }
                query.orderBy(orders);
            }
        }
        return this;
    }

    private Order getOrder(String attribute) {

        String sort = attribute.trim();

        if (sort.startsWith(ListingConfig.SORT_DESC)) {
            sort = sort.substring(ListingConfig.SORT_DESC.length());
        }
        if (sort.startsWith(ListingConfig.SORT_ASC)) {
            sort = sort.substring(ListingConfig.SORT_ASC.length());
        }
        if (attribute.startsWith(ListingConfig.SORT_DESC)) {
            return criteriaBuilder.desc(root.get(sort));
        }
        return criteriaBuilder.asc(root.get(sort));
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
