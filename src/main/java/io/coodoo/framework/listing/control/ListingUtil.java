package io.coodoo.framework.listing.control;

/**
 * @author coodoo GmbH (coodoo.io)
 */
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Id;
import javax.persistence.Transient;

import io.coodoo.framework.listing.boundary.annotation.ListingFilterIgnore;
import io.coodoo.framework.listing.boundary.annotation.ListingFilterIgnoreFields;

public final class ListingUtil {

    private static final String REGEX_LONG = "[-+]?\\d{1,37}";
    private static final String REGEX_INT = "[-+]?\\d{1,10}";
    private static final String REGEX_SHORT = "[-+]?\\d{1,5}";
    private static final String REGEX_FLOAT = "[-+]?\\d*[.,]?\\d+";
    private static final String REGEX_DOUBLE = "[-+]?\\d*[.,]?\\d+";
    // _______________________Group_numbers:__12______________34______________5________
    private static final String REGEX_DATE = "((\\d{1,2})\\.)?((\\d{1,2})\\.)?(\\d{4})";

    private ListingUtil() {}

    public static List<Field> getFields(Class<?> targetClass) {

        List<Field> fields = new ArrayList<>();

        Class<?> inheritanceClass = targetClass;
        while (inheritanceClass != null) {
            fields.addAll(Arrays.asList(inheritanceClass.getDeclaredFields()));
            inheritanceClass = inheritanceClass.getSuperclass();
        }
        return fields;
    }

    public static List<Field> getFilterFields(Class<?> targetClass) {

        List<Field> fields = new ArrayList<>();
        List<String> ignoreFields = new ArrayList<>();
        Class<?> inheritanceClass = targetClass;

        while (inheritanceClass != null) {
            if (inheritanceClass.isAnnotationPresent(ListingFilterIgnoreFields.class)) {
                ignoreFields.addAll(Arrays.asList(inheritanceClass.getAnnotation(ListingFilterIgnoreFields.class).value()));
            }
            for (Field field : inheritanceClass.getDeclaredFields()) {
                // There is no need to check the JPA identifier and transient fields are a irrelevant
                if (!field.isAnnotationPresent(Id.class) && !field.isAnnotationPresent(Transient.class)
                // Defined to ignore
                                && !field.isAnnotationPresent(ListingFilterIgnore.class) && !ignoreFields.contains(field.getName())
                                // Ignore collections, final and static fields
                                && !Collection.class.isAssignableFrom(field.getType()) && !Modifier.isFinal(field.getModifiers())
                                && !Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
            inheritanceClass = inheritanceClass.getSuperclass();
        }
        return fields;
    }

    public static String likeValue(String value) {
        return "%" + value.replace(ListingConfig.WILDCARD_MANY, "%").replace(ListingConfig.WILDCARD_ONE, "_").toLowerCase() + "%";
    }

    public static boolean isQuoted(String value) {
        return value.startsWith("\"") && value.endsWith("\"");
    }

    public static String removeQuotes(String value) {
        return value.replaceAll("^\"|\"$", "");
    }

    public static List<String> split(String value) {
        return Arrays.asList(value.split("\\|", -1));
    }

    public static Date parseDate(String dateString, boolean end) {
        return Date.from(parseDateTime(dateString, end).toInstant(ZoneOffset.UTC));
    }

    public static LocalDateTime parseDateTime(String dateString, boolean end) {

        Matcher matcher = Pattern.compile(REGEX_DATE).matcher(dateString);
        if (matcher.find()) {
            if (matcher.group(5) != null) {
                Integer year = Integer.valueOf(matcher.group(5));
                if (matcher.group(4) != null) {
                    Integer month = Integer.valueOf(matcher.group(4));
                    if (matcher.group(2) != null) {
                        Integer day = Integer.valueOf(matcher.group(2));
                        // DD.MM.YYYY
                        LocalDateTime date = LocalDateTime.of(year, month, day, 0, 0, 0);
                        if (end) {
                            return date.plusDays(1).minusSeconds(1);
                        }
                        return date;
                    }
                    // MM.YYYY
                    LocalDateTime date = LocalDateTime.of(year, month, 1, 0, 0, 0);
                    if (end) {
                        return date.plusMonths(1).minusSeconds(1);
                    }
                    return date;
                }
                // YYYY
                LocalDateTime date = LocalDateTime.of(year, 1, 1, 0, 0, 0);
                if (end) {
                    return date.plusYears(1).minusSeconds(1);
                }
                return date;
            }
        }
        return null;
    }

    public static boolean validDate(String value) {
        return matches(value, REGEX_DATE);
    }

    public static boolean validLong(String value) {
        return matches(value, REGEX_LONG);
    }

    public static boolean validInt(String value) {
        return matches(value, REGEX_INT);
    }

    public static boolean validShort(String value) {
        return matches(value, REGEX_SHORT);
    }

    public static boolean validFloat(String value) {
        return matches(value, REGEX_FLOAT);
    }

    public static boolean validDouble(String value) {
        return matches(value, REGEX_DOUBLE);
    }

    public static boolean matches(String value, String valueRegex) {
        return value.matches("^" + valueRegex + "$");
    }

    public static String rangePatternDate() {
        return rangePattern(REGEX_DATE);
    }

    public static String rangePatternLong() {
        return rangePattern(REGEX_LONG);
    }

    public static String rangePatternInt() {
        return rangePattern(REGEX_INT);
    }

    public static String rangePatternShort() {
        return rangePattern(REGEX_SHORT);
    }

    public static String rangePatternFloat() {
        return rangePattern(REGEX_FLOAT);
    }

    public static String rangePatternDouble() {
        return rangePattern(REGEX_DOUBLE);
    }

    public static String rangePattern(String valueRegex) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("^(");
        stringBuffer.append(valueRegex);
        stringBuffer.append(")(");
        stringBuffer.append(ListingConfig.OPERATOR_TO);
        stringBuffer.append("|");
        stringBuffer.append(ListingConfig.OPERATOR_TO_WORD);
        stringBuffer.append(")(");
        stringBuffer.append(valueRegex);
        stringBuffer.append(")$");
        return stringBuffer.toString();
    }

}
