package com.librax.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * java.time <-> SQLite TEXT (ISO-8601) conversions, centralized so date
 * formatting is consistent everywhere.
 */
public final class DateUtils {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateUtils() {}

    public static String format(LocalDate date) {
        return date == null ? null : date.format(DATE_FMT);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FMT);
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim();
        if (trimmed.length() == 4 && trimmed.matches("\\d{4}")) {
            return LocalDate.of(Integer.parseInt(trimmed), 1, 1);
        }
        try {
            return LocalDate.parse(trimmed, DATE_FMT);
        } catch (Exception e) {
            if (trimmed.length() >= 10) {
                try {
                    return LocalDate.parse(trimmed.substring(0, 10), DATE_FMT);
                } catch (Exception ignored) {}
            }
            return null;
        }
    }

    public static LocalDateTime parseDateTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value, DATETIME_FMT);
    }

    public static long daysBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null || !to.isAfter(from)) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(from, to);
    }
}
