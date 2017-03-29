package io.coodoo.framework.listing.control;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ListingUtil {

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

    public static String likeValue(String value) {
        return "%" + value.toLowerCase() + "%";
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

    public static LocalDateTime parseFilterDate(String dateString, boolean end) {
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

}
