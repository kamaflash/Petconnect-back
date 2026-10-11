package com.petconnect.users.application.usecases;

import com.petconnect.users.application.dto.UpdateProfileRequest;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.exceptions.InvalidProfileCredentialException;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** La actualización de perfil auto-aprueba vet/shelter solo con acreditación válida. */
@ExtendWith(MockitoExtension.class)
class UpdateProfileUseCaseTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private UpdateProfileUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        profile = new UserProfile(userId, "Ana", "García", "user");
        // Cloudinary deshabilitado en pruebas (constructor admite null)
        useCase = new UpdateProfileUseCase(userProfileRepository, null);
    }

    private UpdateProfileRequest request(String profileType) {
        return new UpdateProfileRequest(
                "Ana", "García", null, null, null, null, null, null,
                profileType, null, null, null, null, null);
    }

    private UpdateProfileRequest request(String profileType, String licenseNumber, String charityNumber) {
        return new UpdateProfileRequest(
                "Ana", "García", null, null, null, null, null, null,
                profileType, null, licenseNumber, charityNumber, null, null);
    }

    @Test
    void requestingShelterWithValidCharityNumberIsAutoApproved() throws Exception {
        when(userProfileRepository.findByAuthUserId(userId)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(userId, request("shelter", null, "ASOC-2021-4582"), null, null);

        assertEquals("shelter", profile.getProfileType());
        assertNull(profile.getPendingProfileType());
        verify(userProfileRepository).save(profile);
    }

    @Test
    void requestingVetWithValidLicenseIsAutoApproved() throws Exception {
        when(userProfileRepository.findByAuthUserId(userId)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(userId, request("vet", "M-12345", null), null, null);

        assertEquals("vet", profile.getProfileType());
        assertNull(profile.getPendingProfileType());
    }

    @Test
    void requestingVetWithInvalidLicenseThrowsAndDoesNotSave() {
        when(userProfileRepository.findByAuthUserId(userId)).thenReturn(Optional.of(profile));

        assertThrows(
                InvalidProfileCredentialException.class,
                () -> useCase.execute(userId, request("vet", "basura", null), null, null));

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void requestingShelterWithoutCharityNumberThrowsAndDoesNotSave() {
        when(userProfileRepository.findByAuthUserId(userId)).thenReturn(Optional.of(profile));

        assertThrows(
                InvalidProfileCredentialException.class,
                () -> useCase.execute(userId, request("shelter"), null, null));

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void selfServiceTypeIsAppliedDirectly() throws Exception {
        when(userProfileRepository.findByAuthUserId(userId)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(userId, request("trainer"), null, null);

        assertEquals("trainer", profile.getProfileType());
        assertNull(profile.getPendingProfileType());
    }
}
