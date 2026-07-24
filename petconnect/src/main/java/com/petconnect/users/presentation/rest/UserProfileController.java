package com.petconnect.users.presentation.rest;

import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.users.application.dto.UserProfileResponse;
import com.petconnect.users.application.dto.UpdateProfileRequest;
import com.petconnect.users.application.usecases.GetUserProfileUseCase;
import com.petconnect.users.application.usecases.UpdateProfileUseCase;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private final UpdateProfileUseCase updateProfileUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UserProfileRepository userProfileRepository;

    public UserProfileController(
            UpdateProfileUseCase updateProfileUseCase,
            GetUserProfileUseCase getUserProfileUseCase,
            UserProfileRepository userProfileRepository) {
        this.updateProfileUseCase = updateProfileUseCase;
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.userProfileRepository = userProfileRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = getUserProfileUseCase.execute(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<UserProfileResponse> getProfileById(@PathVariable UUID profileId) {
        var profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return ResponseEntity.ok(new UserProfileResponse(
                profile.getId(),
                profile.getAuthUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getCoverImageUrl(),
                profile.getDateOfBirth(),
                profile.getCity(),
                profile.getCountry(),
                profile.isProfilePublic(),
                profile.isNotificationsEnabled(),
                profile.getProfileType(),
                profile.getSpecialty(),
                profile.getLicenseNumber(),
                profile.getCharityNumber(),
                profile.getStoreName(),
                profile.getWebsite()));
    }

    @GetMapping("/by-auth/{authUserId}")
    public ResponseEntity<UserProfileResponse> getProfileByAuthUserId(@PathVariable UUID authUserId) {
        var response = getUserProfileUseCase.execute(authUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<UserProfile>> getAllProfiles() {
        return ResponseEntity.ok(userProfileRepository.findAll());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfile> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "data", required = false) UpdateProfileRequest data,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) throws IOException {

        var profile = updateProfileUseCase.execute(userDetails.getUserId(), data, avatar, coverImage);
        return ResponseEntity.ok(profile);
    }
}