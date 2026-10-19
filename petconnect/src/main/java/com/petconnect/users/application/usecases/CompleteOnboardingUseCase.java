package com.petconnect.users.application.usecases;

import com.petconnect.users.application.dto.OnboardingProfileRequest;
import com.petconnect.users.application.dto.UserProfileResponse;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.exceptions.InvalidProfileCredentialException;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Onboarding post-registro: el usuario elige su tipo de cuenta y confirma los
 * datos obligatorios de ese tipo.
 *
 * <p>
 * Es la única vía que activa la marca de onboarding del perfil. Al terminar,
 * el usuario puede acceder a las rutas protegidas. Si los datos no son válidos
 * para el tipo elegido, se lanza {@link InvalidProfileCredentialException}
 * (400) y el perfil queda sin completar.
 * </p>
 */
@Service
public class CompleteOnboardingUseCase {

    private final UserProfileRepository userProfileRepository;

    public CompleteOnboardingUseCase(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfileResponse execute(UUID userId, OnboardingProfileRequest request) {
        var profile = userProfileRepository.findByAuthUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.updateProfile(
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.bio(),
                request.dateOfBirth());
        profile.updateLocation(request.location(), request.latitude(), request.longitude());
        profile.updateTypeSpecificFields(
                request.specialty(),
                request.licenseNumber(),
                request.charityNumber(),
                request.storeName(),
                request.website());
        profile.updateServices(normalizeServices(request.services(), request.profileType()));

        // Valida la acreditación y aplica el tipo (vet/shelter requieren número
        // con formato válido; el resto se aplican directamente).
        profile.requestProfileType(request.profileType());

        if (!profile.hasRequiredFieldsForType()) {
            throw new InvalidProfileCredentialException(
                    "profileType",
                    "Faltan datos obligatorios para el tipo de perfil '" + request.profileType() + "'.");
        }

        profile.markProfileCompleted();

        var saved = userProfileRepository.save(profile);
        return toResponse(saved);
    }

    /**
     * Normaliza los códigos de servicios del catálogo (minúsculas, sin espacios,
     * sin duplicados). Solo se conservan si el tipo solicitado es vet.
     */
    private static String normalizeServices(String services, String profileType) {
        boolean isVet = profileType != null && profileType.trim().equalsIgnoreCase("vet");
        if (!isVet || services == null || services.isBlank()) {
            return null;
        }
        return java.util.Arrays.stream(services.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .collect(java.util.stream.Collectors.joining(","));
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getAuthUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getCoverImageUrl(),
                profile.getDateOfBirth(),
                profile.getLocation(),
                profile.getLatitude(),
                profile.getLongitude(),
                profile.isProfilePublic(),
                profile.isNotificationsEnabled(),
                profile.getProfileType(),
                profile.getSpecialty(),
                profile.getLicenseNumber(),
                profile.getCharityNumber(),
                profile.getStoreName(),
                profile.getWebsite(),
                profile.getPendingProfileType(),
                profile.getServices(),
                profile.isProfileComplete());
    }
}