package com.goDelivery.goDelivery.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating and normalizing phone numbers.
 * Supports international formats with country codes, spaces, dashes, and
 * parentheses.
 */
public class PhoneNumberValidator {

    /**
     * Flexible phone number pattern that accepts:
     * - Optional + prefix
     * - Optional parentheses around country/area codes
     * - Spaces, dashes, and dots as separators
     * - 10-15 total digits
     * 
     * Examples of valid formats:
     * - +258841234567
     * - +258 84 123 4567
     * - 258-84-123-4567
     * - (258) 84 123 4567
     * - 84 123 4567
     */
    public static final String PHONE_PATTERN = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,5}[-\\s\\.]?[0-9]{1,6}$";

    private static final Pattern pattern = Pattern.compile(PHONE_PATTERN);

    /**
     * Validates a phone number against the flexible pattern.
     * 
     * @param phoneNumber the phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public static boolean isValid(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // Check if it matches the pattern
        if (!pattern.matcher(phoneNumber).matches()) {
            return false;
        }

        // Extract only digits to verify length (10-15 digits)
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");
        return digitsOnly.length() >= 10 && digitsOnly.length() <= 15;
    }

    /**
     * Normalizes a phone number by removing all non-digit characters except the
     * leading +.
     * 
     * @param phoneNumber the phone number to normalize
     * @return normalized phone number (e.g., "+258841234567" or "258841234567")
     */
    public static String normalize(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }

        // Keep the + if it exists at the start
        boolean hasPlus = phoneNumber.trim().startsWith("+");
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");

        return hasPlus ? "+" + digitsOnly : digitsOnly;
    }

    /**
     * Gets a user-friendly error message for invalid phone numbers.
     * 
     * @return error message string
     */
    public static String getErrorMessage() {
        return "Phone number must be 10-15 digits and may include country code (e.g., +258 84 123 4567, 258-84-123-4567)";
    }
}
