package com.petconnect.auth.domain.valueobjects;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    void shouldCreateValidEmail() {
        var email = new Email("test@example.com");
        assertEquals("test@example.com", email.value());
    }

    @Test
    void shouldConvertToLowerCase() {
        var email = new Email("Test@Example.COM");
        assertEquals("test@example.com", email.value());
    }

    @Test
    void shouldThrowOnNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Email(null));
    }

    @Test
    void shouldThrowOnBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Email("   "));
    }

    @Test
    void shouldThrowOnInvalidEmailFormat() {
        assertThrows(IllegalArgumentException.class, () -> new Email("invalid-email"));
    }

    @Test
    void shouldThrowOnEmailWithoutAt() {
        assertThrows(IllegalArgumentException.class, () -> new Email("userexample.com"));
    }

    @Test
    void shouldThrowOnEmailWithoutDomain() {
        assertThrows(IllegalArgumentException.class, () -> new Email("user@"));
    }

    @Test
    void shouldBeEqualForSameEmail() {
        var email1 = new Email("test@example.com");
        var email2 = new Email("test@example.com");
        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentEmails() {
        var email1 = new Email("test@example.com");
        var email2 = new Email("other@example.com");
        assertNotEquals(email1, email2);
    }

    @Test
    void toStringShouldReturnValue() {
        var email = new Email("test@example.com");
        assertEquals("test@example.com", email.toString());
    }
}