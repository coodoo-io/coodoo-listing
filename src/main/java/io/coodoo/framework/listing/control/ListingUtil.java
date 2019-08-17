package io.coodoo.framework.listing.control;

/**
 * @author coodoo GmbH (coodoo.io)
 */
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Id;
import javax.persistence.Transient;

import io.coodoo.framework.listing.boundary.annotation.ListingFilterAsString;
import io.coodoo.framework.listing.boundary.annotation.ListingFilterIgnore;
import io.coodoo.framework.listing.boundary.annotation.ListingFilterIgnoreFields;

public final class ListingUtil {

    private static final String REGEX_LONG = "[-+]?\\d{1,37}";
    private static final String REGEX_INT = "[-+]?\\d{1,10}";
    private static final String REGEX_SHORT = "[-+]?\\d{1,5}";
    private static final String REGEX_FLOAT = "[-+]?\\d*[.,]?\\d+";
    private static final String REGEX_DOUBLE = "[-+]?\\d*[.,]?\\d+";
    // _______________________Group_numbers:__12______________34______________5_________
    private static final String REGEX_DATE = "((\\d{1,2})\\D)?((\\d{1,2})\\D)?(\\d{2,})";

    private ListingUtil() {}

    public static List<Field> getFields(Class<?> targetClass) {
        return getFields(targetClass, false);
    }

    public static List<Field> getFields(Class<?> targetClass, boolean allColumnFields) {

        List<Field> fields = new ArrayList<>();
        List<String> ignoreFields = new ArrayList<>();
        Class<?> inheritanceClass = targetClass;

        while (inheritanceClass != null) {
            if (inheritanceClass.isAnnotationPresent(ListingFilterIgnoreFields.class)) {
                ignoreFields.addAll(Arrays.asList(inheritanceClass.getAnnotation(ListingFilterIgnoreFields.class).value()));
            }
            for (Field field : inheritanceClass.getDeclaredFields()) {

                // There is no need to check the JPA identifier and transient fields are a irrelevant
                if ((allColumnFields || !field.isAnnotationPresent(Id.class))
                                // Transient fields have no database column
                                && !field.isAnnotationPresent(Transient.class)
                                // Defined to ignore
                                && (allColumnFields || !field.isAnnotationPresent(ListingFilterIgnore.class) && !ignoreFields.contains(field.getName()))
                                // Ignore collections except they should be treated like Stings (dangerous!)
                                && (!Collection.class.isAssignableFrom(field.getType()) || field.isAnnotationPresent(ListingFilterAsString.class))
                                // neither final
                                && !Modifier.isFinal(field.getModifiers())
                                // nor static fields
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
        return Arrays.asList(value.split(escape(ListingConfig.OPERATOR_OR), -1));
    }

    /**
     * Escapes all RegEx control characters for the usage like in {@link String#replaceAll(String, String)} or {@link String#split(String)}
     * 
     * @param value may containing some RegEx control characters like <code>|</code>, <code>?</code>, or <code>*</code>
     * @return value with escaped RegEx control characters like <code>\\|</code>, <code>\\?</code>, or <code>\\*</code>
     */
    public static String escape(String value) {
        return value.replaceAll("([\\\\\\.\\[\\{\\(\\*\\+\\?\\^\\$\\|])", "\\\\$1");
    }

    public static Date parseDate(String dateString, boolean end) {

        return Date.from(parseDateTime(dateString, end).atZone(ListingConfig.ZONE_ID).toInstant());
    }

    public static LocalDateTime parseDateTime(String dateString, boolean end) {
        if (dateString != null) {
            Matcher matcher = Pattern.compile(REGEX_DATE).matcher(dateString);
            if (matcher.find()) {
                if (matcher.group(5) != null) {
                    try {
                        String value = matcher.group(5);
                        if (value.length() > 4) { // from 10k we interpret this value as milliseconds
                            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.valueOf(value)), ListingConfig.ZONE_ID);
                        }
                        Integer year = Integer.valueOf(value);
                        if (year < 100) { // only two digits of year given
                            year += 2000; // sum it up
                            if (year > LocalDate.now(ListingConfig.ZONE_ID).getYear()) {
                                year -= 100; // if it is in the future, take it back to the 20th century
                            }
                        }
                        // DD.MM.YYYY
                        if (matcher.group(2) != null && matcher.group(4) != null) {
                            Integer month = Integer.valueOf(matcher.group(4));
                            Integer day = Integer.valueOf(matcher.group(2));
                            LocalDateTime date = LocalDateTime.of(year, month, day, 0, 0, 0);
                            if (end) {
                                return date.plusDays(1).minusSeconds(1);
                            }
                            return date;
                        }
                        // MM.YYYY
                        if (matcher.group(2) != null) {
                            Integer month = Integer.valueOf(matcher.group(2));
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
                    } catch (NumberFormatException | DateTimeException e) {
                        return null;
                    }
                }
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
        stringBuffer.append(escape(ListingConfig.OPERATOR_TO));
        stringBuffer.append("|");
        stringBuffer.append(escape(ListingConfig.OPERATOR_TO_WORD));
        stringBuffer.append(")(");
        stringBuffer.append(valueRegex);
        stringBuffer.append(")$");
        return stringBuffer.toString();
    }

}
