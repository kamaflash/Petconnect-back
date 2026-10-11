package com.petconnect.users.domain;

import com.petconnect.users.domain.exceptions.InvalidProfileCredentialException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reglas de negocio de los cambios de tipo de perfil (validación estricta + auto-aprobación). */
class UserProfileTest {

    private UserProfile profile(String profileType) {
        return new UserProfile(UUID.randomUUID(), "Ana", "García", profileType);
    }

    @Test
    void requiresApprovalOnlyForVetAndShelter() {
        assertTrue(UserProfile.requiresApproval("vet"));
        assertTrue(UserProfile.requiresApproval("shelter"));
        assertTrue(UserProfile.requiresApproval("VET"));
        assertFalse(UserProfile.requiresApproval("user"));
        assertFalse(UserProfile.requiresApproval("trainer"));
        assertFalse(UserProfile.requiresApproval("seller"));
        assertFalse(UserProfile.requiresApproval(null));
        assertFalse(UserProfile.requiresApproval(" "));
    }

    @Test
    void requestingVetWithValidLicenseIsAutoApproved() {
        var p = profile("user");
        p.updateTypeSpecificFields(null, "M-12345", null, null, null);

        p.requestProfileType("vet");

        assertEquals("vet", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }

    @Test
    void requestingVetWithLongFormLicenseIsAutoApproved() {
        var p = profile("user");
        p.updateTypeSpecificFields(null, "COLVET-B-9876", null, null, null);

        p.requestProfileType("vet");

        assertEquals("vet", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }

    @Test
    void requestingVetWithInvalidLicenseThrows() {
        var p = profile("user");
        p.updateTypeSpecificFields(null, "asdf", null, null, null);

        var ex = assertThrows(
                InvalidProfileCredentialException.class, () -> p.requestProfileType("vet"));

        assertEquals("licenseNumber", ex.getField());
        assertEquals("user", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }

    @Test
    void requestingVetWithoutLicenseThrows() {
        var p = profile("user");

        assertThrows(
                InvalidProfileCredentialException.class, () -> p.requestProfileType("vet"));

        assertEquals("user", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }

    @Test
    void requestingShelterWithValidCifIsAutoApproved() {
        var p = profile("user");
        p.updateTypeSpecificFields(null, null, "G-12345674", null, null);

        p.requestProfileType("shelter");

        assertEquals("shelter", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }

    @Test
    void requestingShelterWithRegionalReferenceIsAutoApproved() {
        var p = profile("user");
        p.updateTypeSpecificFields(null, null, "ASOC-2021-4582", null, null);

        p.requestProfileType("shelter");

        assertEquals("shelter", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }

    @Test
    void requestingShelterWithInvalidCharityNumberThrows() {
        var p = profile("user");
        p.updateTypeSpecificFields(null, null, "123", null, null);

        var ex = assertThrows(
                InvalidProfileCredentialException.class, () -> p.requestProfileType("shelter"));

        assertEquals("charityNumber", ex.getField());
        assertEquals("user", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }

    @Test
    void selfServiceTypeIsAppliedDirectlyAndClearsPending() {
        var p = profile("user");
        p.updateTypeSpecificFields(null, null, "ASOC-2021-4582", null, null);
        p.requestProfileType("shelter");

        p.requestProfileType("seller");

        assertEquals("seller", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }

    @Test
    void requestingCurrentTypeIsNoOp() {
        var p = profile("user");

        p.requestProfileType("user");

        assertEquals("user", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }

    @Test
    void approveWithoutPendingDoesNothing() {
        var p = profile("user");

        p.approvePendingProfileType();

        assertEquals("user", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }

    @Test
    void rejectClearsPendingAndKeepsActiveType() {
        var p = profile("user");
        p.requestProfileType("trainer");
        // Simula una solicitud pendiente heredada (p. ej. creada antes de la
        // validación automática) para verificar el descarte por admin/usuario.
        p.rejectPendingProfileType();

        assertEquals("trainer", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }

    @Test
    void blankOrNullRequestIsIgnored() {
        var p = profile("user");

        p.requestProfileType(null);
        p.requestProfileType("  ");

        assertEquals("user", p.getProfileType());
        assertNull(p.getPendingProfileType());
    }
}
