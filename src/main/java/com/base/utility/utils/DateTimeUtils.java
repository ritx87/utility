package com.base.utility.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class DateTimeUtils {

    public static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter CUSTOM_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter CUSTOM_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Optional<LocalDate> parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDate.parse(dateString, ISO_DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            try {
                return Optional.of(LocalDate.parse(dateString, CUSTOM_DATE_FORMATTER));
            } catch (DateTimeParseException ex) {
                return Optional.empty();
            }
        }
    }

    public static Optional<LocalDateTime> parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDateTime.parse(dateTimeString, ISO_DATETIME_FORMATTER));
        } catch (DateTimeParseException e) {
            try {
                return Optional.of(LocalDateTime.parse(dateTimeString, CUSTOM_DATETIME_FORMATTER));
            } catch (DateTimeParseException ex) {
                return Optional.empty();
            }
        }
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(ISO_DATE_FORMATTER) : null;
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_DATETIME_FORMATTER) : null;
    }

    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        return date != null ? date.format(formatter) : null;
    }

    public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime != null ? dateTime.format(formatter) : null;
    }

    public static long toEpochMilli(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toInstant(ZoneOffset.UTC).toEpochMilli() : 0;
    }

    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC);
    }

    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        return date != null && start != null && end != null &&
                !date.isBefore(start) && !date.isAfter(end);
    }

    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return dateTime != null && start != null && end != null &&
                !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    public static LocalDate now() {
        return LocalDate.now();
    }

    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }

    public static LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }

    public static LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }

    public static LocalDate startOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    public static LocalDate endOfMonth() {
        return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    }

    public static LocalDate startOfYear() {
        return LocalDate.now().withDayOfYear(1);
    }

    public static LocalDate endOfYear() {
        return LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
    }
}
