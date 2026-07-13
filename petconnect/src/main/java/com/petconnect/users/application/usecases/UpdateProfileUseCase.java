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
            profile.updateLocation(data.city(), data.country());
        }

        return userProfileRepository.save(profile);
    }
}