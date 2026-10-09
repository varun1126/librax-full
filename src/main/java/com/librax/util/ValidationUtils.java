package com.librax.util;

import java.util.regex.Pattern;

/**
 * Centralized, stateless validation helpers. UI and service code call
 * these instead of duplicating regex/logic (RULES.md §6).
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9 ()-]{7,15}$");

    private ValidationUtils() {}

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return !isBlank(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return !isBlank(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates ISBN-10 or ISBN-13 (checksum included). Hyphens/spaces
     * are ignored before validation.
     */
    public static boolean isValidIsbn(String isbn) {
        if (isBlank(isbn)) {
            return false;
        }
        String cleaned = isbn.replaceAll("[\\s-]", "").toUpperCase();
        if (cleaned.length() == 10) {
            return isValidIsbn10(cleaned);
        } else if (cleaned.length() == 13) {
            return isValidIsbn13(cleaned);
        }
        return false;
    }

    private static boolean isValidIsbn10(String isbn) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            char c = isbn.charAt(i);
            int digit;
            if (c == 'X' && i == 9) {
                digit = 10;
            } else if (Character.isDigit(c)) {
                digit = c - '0';
            } else {
                return false;
            }
            sum += digit * (10 - i);
        }
        return sum % 11 == 0;
    }

    private static boolean isValidIsbn13(String isbn) {
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            char c = isbn.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            int digit = c - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return sum % 10 == 0;
    }

    /**
     * Generates a mathematically valid 13-digit ISBN with a correct EAN-13 checksum.
     */
    public static String generateValidIsbn13(long seed) {
        String base12 = String.format("978%09d", Math.abs(seed) % 1_000_000_000L);
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = base12.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        return base12 + check;
    }
}
