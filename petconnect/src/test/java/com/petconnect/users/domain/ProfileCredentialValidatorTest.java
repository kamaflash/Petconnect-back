package com.petconnect.users.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Validación estricta de formato de los números acreditativos profesionales. */
class ProfileCredentialValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = { "M-12345", "COLVET-M-12345", "B-9876", "m-12345", "  M-12345  ", "GC-123456" })
    void validLicenseNumbers(String license) {
        assertTrue(ProfileCredentialValidator.isValidLicenseNumber(license));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "asdf", "123", "M12345", "M-12", "M-1234567", "XX-12345",
            "COLVET-XX-12345", "M-ABCDE", "", "   "
    })
    void invalidLicenseNumbers(String license) {
        assertFalse(ProfileCredentialValidator.isValidLicenseNumber(license));
    }

    @Test
    void nullLicenseIsInvalid() {
        assertFalse(ProfileCredentialValidator.isValidLicenseNumber(null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "G-12345674", "ASOC-2021-4582", "PA-2019-123", "g-12345674" })
    void validCharityNumbers(String charity) {
        assertTrue(ProfileCredentialValidator.isValidCharityNumber(charity));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "asdf", "123", "G-12345675", "Z-12345678", "ASOC-21-4582",
            "ASOC-2021-45", "A-2021-4582", "", "   "
    })
    void invalidCharityNumbers(String charity) {
        assertFalse(ProfileCredentialValidator.isValidCharityNumber(charity));
    }

    @Test
    void nullCharityIsInvalid() {
        assertFalse(ProfileCredentialValidator.isValidCharityNumber(null));
    }
}
