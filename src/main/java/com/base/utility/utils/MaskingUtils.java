package com.base.utility.utils;

import java.util.Set;
import java.util.regex.Pattern;

public final class MaskingUtils {
    private MaskingUtils() {}

    private static final String MASK_PATTERN = "****";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b\\d{3}-\\d{3}-\\d{4}\\b|\\b\\d{10}\\b|\\b\\+\\d{1,3}\\s?\\d{3,14}\\b");
    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b");

    public static String maskSensitiveData(String input, Set<String> fieldsToMask) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;

        // Mask specific fields in JSON
        for (String field : fieldsToMask) {
            result = maskJsonField(result, field);
        }

        // Mask common patterns
        result = maskEmailAddresses(result);
        result = maskPhoneNumbers(result);
        result = maskSSN(result);
        result = maskCreditCards(result);

        return result;
    }

    public static String maskJsonField(String json, String fieldName) {
        if (json == null || fieldName == null) {
            return json;
        }

        // Pattern to match: "fieldName":"value" or "fieldName": "value"
        String pattern = "\"" + fieldName + "\"\\s*:\\s*\"[^\"]*\"";
        return json.replaceAll(pattern, "\"" + fieldName + "\":\"" + MASK_PATTERN + "\"");
    }

    public static String maskEmailAddresses(String input) {
        if (input == null) {
            return null;
        }

        return EMAIL_PATTERN.matcher(input).replaceAll(match -> {
            String[] parts = match.group().split("@");
            String localPart = parts[0];
            String domainPart = parts[1];

            if (localPart.length() <= 2) {
                return MASK_PATTERN + "@" + domainPart;
            }

            return localPart.charAt(0) + MASK_PATTERN + localPart.charAt(localPart.length() - 1) + "@" + domainPart;
        });
    }

    public static String maskPhoneNumbers(String input) {
        if (input == null) {
            return null;
        }

        return PHONE_PATTERN.matcher(input).replaceAll(MASK_PATTERN);
    }

    public static String maskSSN(String input) {
        if (input == null) {
            return null;
        }

        return SSN_PATTERN.matcher(input).replaceAll("***-**-" + MASK_PATTERN);
    }

    public static String maskCreditCards(String input) {
        if (input == null) {
            return null;
        }

        return CREDIT_CARD_PATTERN.matcher(input).replaceAll(match -> {
            String card = match.group().replaceAll("[\\s-]", "");
            return MASK_PATTERN + " " + MASK_PATTERN + " " + MASK_PATTERN + " " + card.substring(card.length() - 4);
        });
    }

    public static String maskString(String input, int visibleChars) {
        if (input == null || input.length() <= visibleChars) {
            return MASK_PATTERN;
        }

        if (visibleChars <= 0) {
            return MASK_PATTERN;
        }

        return input.substring(0, visibleChars) + MASK_PATTERN;
    }

    public static String maskStringKeepEnds(String input, int keepStart, int keepEnd) {
        if (input == null || input.length() <= keepStart + keepEnd) {
            return MASK_PATTERN;
        }

        return input.substring(0, keepStart) + MASK_PATTERN + input.substring(input.length() - keepEnd);
    }
}
