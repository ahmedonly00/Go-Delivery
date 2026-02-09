package com.goDelivery.goDelivery.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PhoneNumberValidator utility
 */
class PhoneNumberValidatorTest {

    @Test
    void testValidPhoneNumbers() {
        // Valid international formats
        assertTrue(PhoneNumberValidator.isValid("+258841234567"));
        assertTrue(PhoneNumberValidator.isValid("+258 84 123 4567"));
        assertTrue(PhoneNumberValidator.isValid("258-84-123-4567"));
        assertTrue(PhoneNumberValidator.isValid("(258) 84 123 4567"));
        assertTrue(PhoneNumberValidator.isValid("+250 123 456 789"));
        assertTrue(PhoneNumberValidator.isValid("84 123 4567"));
        assertTrue(PhoneNumberValidator.isValid("841234567"));

        // Valid with dots
        assertTrue(PhoneNumberValidator.isValid("+258.84.123.4567"));
        assertTrue(PhoneNumberValidator.isValid("258.84.123.4567"));

        // Valid with mixed separators
        assertTrue(PhoneNumberValidator.isValid("+258 84-123 4567"));
    }

    @Test
    void testInvalidPhoneNumbers() {
        // Too short (less than 10 digits)
        assertFalse(PhoneNumberValidator.isValid("123456789"));
        assertFalse(PhoneNumberValidator.isValid("+258 123"));

        // Too long (more than 15 digits)
        assertFalse(PhoneNumberValidator.isValid("12345678901234567"));
        assertFalse(PhoneNumberValidator.isValid("+258 1234567890123456"));

        // Contains letters
        assertFalse(PhoneNumberValidator.isValid("+258 84 ABC 4567"));
        assertFalse(PhoneNumberValidator.isValid("258-84-ABC-4567"));

        // Invalid special characters
        assertFalse(PhoneNumberValidator.isValid("+258#84#123#4567"));
        assertFalse(PhoneNumberValidator.isValid("258*84*123*4567"));

        // Null or empty
        assertFalse(PhoneNumberValidator.isValid(null));
        assertFalse(PhoneNumberValidator.isValid(""));
        assertFalse(PhoneNumberValidator.isValid("   "));
    }

    @Test
    void testNormalizePhoneNumbers() {
        // Test normalization with +
        assertEquals("+258841234567", PhoneNumberValidator.normalize("+258 84 123 4567"));
        assertEquals("+258841234567", PhoneNumberValidator.normalize("+258-84-123-4567"));
        assertEquals("+258841234567", PhoneNumberValidator.normalize("+(258) 84 123 4567"));

        // Test normalization without +
        assertEquals("258841234567", PhoneNumberValidator.normalize("258 84 123 4567"));
        assertEquals("258841234567", PhoneNumberValidator.normalize("258-84-123-4567"));
        assertEquals("841234567", PhoneNumberValidator.normalize("84 123 4567"));

        // Test with dots
        assertEquals("+258841234567", PhoneNumberValidator.normalize("+258.84.123.4567"));
        assertEquals("258841234567", PhoneNumberValidator.normalize("258.84.123.4567"));
    }

    @Test
    void testEdgeCases() {
        // Minimum length (10 digits)
        assertTrue(PhoneNumberValidator.isValid("1234567890"));
        assertTrue(PhoneNumberValidator.isValid("+1 234 567 890"));

        // Maximum length (15 digits)
        assertTrue(PhoneNumberValidator.isValid("123456789012345"));
        assertTrue(PhoneNumberValidator.isValid("+1 234 567 890 12345"));

        // Just below minimum (9 digits)
        assertFalse(PhoneNumberValidator.isValid("123456789"));

        // Just above maximum (16 digits)
        assertFalse(PhoneNumberValidator.isValid("1234567890123456"));
    }

    @Test
    void testErrorMessage() {
        String errorMessage = PhoneNumberValidator.getErrorMessage();
        assertNotNull(errorMessage);
        assertTrue(errorMessage.contains("10-15 digits"));
        assertTrue(errorMessage.contains("country code"));
    }
}
