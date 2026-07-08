package com.petconnect.users.application.usecases;

import com.petconnect.users.application.dto.UpdateProfileRequest;
import com.petconnect.users.application.dto.UserProfileResponse;
import com.petconnect.users.domain.exceptions.UserNotFoundException;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateProfileUseCase {

    private final UserProfileRepository userProfileRepository;

    public UpdateProfileUseCase(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfileResponse execute(UUID authUserId, UpdateProfileRequest request) {
        var profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found for user: " + authUserId));

        profile.updateProfile(
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.bio(),
                request.dateOfBirth());
        profile.updateLocation(request.city(), request.country());

        var saved = userProfileRepository.save(profile);

        return new UserProfileResponse(
                saved.getId(),
                saved.getAuthUserId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getPhone(),
                saved.getBio(),
                saved.getAvatarUrl(),
                saved.getDateOfBirth(),
                saved.getCity(),
                saved.getCountry(),
                saved.isProfilePublic(),
                saved.isNotificationsEnabled());
    }
}