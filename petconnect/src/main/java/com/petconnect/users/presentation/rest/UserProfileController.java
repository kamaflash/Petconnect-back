package com.petconnect.users.presentation.rest;

import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.users.application.dto.UserProfileResponse;
import com.petconnect.users.application.dto.UpdateProfileRequest;
import com.petconnect.users.application.usecases.GetUserProfileUseCase;
import com.petconnect.users.application.usecases.UpdateProfileUseCase;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.exceptions.UserNotFoundException;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new UserNotFoundException("Profile not found: " + profileId));
        return ResponseEntity.ok(toResponse(profile));
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

    // ===== Solicitudes de cambio de tipo de perfil (vet/shelter) =====

    /** Perfiles con un cambio de tipo pendiente de aprobación (solo admin). */
    @GetMapping("/profile-type-requests")
    public ResponseEntity<List<UserProfileResponse>> listPendingTypeRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        requireAdmin(userDetails);
        var pending = userProfileRepository.findByPendingProfileTypeIsNotNull().stream()
                .filter(profile -> profile.getPendingProfileType() != null
                        && !profile.getPendingProfileType().isBlank())
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(pending);
    }

    /** Aprueba la solicitud pendiente del perfil indicado (solo admin). */
    @PutMapping("/profile-type-requests/{profileId}/approve")
    public ResponseEntity<UserProfileResponse> approveTypeRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID profileId) {
        requireAdmin(userDetails);
        var profile = findProfile(profileId);
        profile.approvePendingProfileType();
        userProfileRepository.save(profile);
        return ResponseEntity.ok(toResponse(profile));
    }

    /** Rechaza la solicitud pendiente del perfil indicado (solo admin). */
    @PutMapping("/profile-type-requests/{profileId}/reject")
    public ResponseEntity<UserProfileResponse> rejectTypeRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID profileId) {
        requireAdmin(userDetails);
        var profile = findProfile(profileId);
        profile.rejectPendingProfileType();
        userProfileRepository.save(profile);
        return ResponseEntity.ok(toResponse(profile));
    }

    /** Cancela la propia solicitud pendiente (cualquier usuario autenticado). */
    @DeleteMapping("/profile/pending-type")
    public ResponseEntity<UserProfileResponse> cancelPendingTypeRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var profile = userProfileRepository.findByAuthUserId(userDetails.getUserId())
                .orElseThrow(
                        () -> new UserNotFoundException(
                                "Profile not found for user: " + userDetails.getUserId()));
        profile.rejectPendingProfileType();
        userProfileRepository.save(profile);
        return ResponseEntity.ok(toResponse(profile));
    }

    private UserProfile findProfile(UUID profileId) {
        return userProfileRepository.findById(profileId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found: " + profileId));
    }

    private void requireAdmin(CustomUserDetails userDetails) {
        boolean isAdmin = userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo los administradores pueden gestionar solicitudes de tipo de perfil");
        }
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
                profile.getPendingProfileType());
    }
}