package com.petconnect.users.application.usecases;

import com.petconnect.shared.infrastructure.services.CloudinaryService;
import com.petconnect.users.application.dto.UpdateProfileRequest;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class UpdateProfileUseCase {

    private final UserProfileRepository userProfileRepository;
    private final CloudinaryService cloudinaryService;

    public UpdateProfileUseCase(
            UserProfileRepository userProfileRepository,
            @Autowired(required = false) CloudinaryService cloudinaryService) {
        this.userProfileRepository = userProfileRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public UserProfile execute(
            UUID userId,
            UpdateProfileRequest data,
            MultipartFile avatar,
            MultipartFile coverImage) throws IOException {

        var profile = userProfileRepository.findByAuthUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        var previousType = profile.getProfileType();

        // Subir avatar a Cloudinary si se proporciona y el servicio está disponible
        if (avatar != null && !avatar.isEmpty() && cloudinaryService != null) {
            String avatarUrl = cloudinaryService.uploadImage(avatar);
            profile.updateAvatar(avatarUrl);
        }

        // Subir cover image a Cloudinary si se proporciona y el servicio está
        // disponible
        if (coverImage != null && !coverImage.isEmpty() && cloudinaryService != null) {
            String coverImageUrl = cloudinaryService.uploadImage(coverImage);
            profile.setCoverImageUrl(coverImageUrl);
        }

        // Actualizar otros campos del perfil
        if (data != null) {
            profile.updateProfile(
                    data.firstName(),
                    data.lastName(),
                    data.phone(),
                    data.bio(),
                    data.dateOfBirth());
            profile.updateLocation(data.location(), data.latitude(), data.longitude());
            profile.updateTypeSpecificFields(
                    data.specialty(),
                    data.licenseNumber(),
                    data.charityNumber(),
                    data.storeName(),
                    data.website());
            // Servicios del catálogo que ofrece un vet (códigos comma-separated);
            // solo se conservan si el tipo solicitado es vet.
            profile.updateServices(normalizeServices(data.services(), data.profileType()));
            // vet/shelter se aprueban automáticamente si la acreditación es
            // válida (lanza InvalidProfileCredentialException si no); el resto
            // se aplica directamente. El orden importa: primero se guardan los
            // números y después se validan.
            profile.requestProfileType(data.profileType());

            // La marca de onboarding se recalcula: si el usuario cambia de tipo
            // y le faltan los campos obligatorios del nuevo, vuelve a estar
            // pendiente de completar.
            profile.refreshProfileCompleted(previousType);
        }

        return userProfileRepository.save(profile);
    }

    /**
     * Normaliza los códigos de servicios del catálogo: minúsculas, sin espacios,
     * sin duplicados y sin entradas vacías. Solo se conservan si el tipo de
     * perfil (solicitado) es vet; para cualquier otro tipo se limpia el campo.
     *
     * Nota: el catálogo de códigos vive en el frontend (VET_SERVICE_OPTIONS);
     * aquí solo se normaliza para no duplicar la fuente de verdad.
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
}