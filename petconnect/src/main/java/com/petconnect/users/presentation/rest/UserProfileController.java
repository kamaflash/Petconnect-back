package com.petconnect.users.presentation.rest;

import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.messages.infrastructure.websocket.WebSocketPresenceListener;
import com.petconnect.users.application.dto.OnboardingProfileRequest;
import com.petconnect.users.application.dto.ProviderSearchResponse;
import com.petconnect.users.application.dto.UserProfileResponse;
import com.petconnect.users.application.dto.UserPreferencesResponse;
import com.petconnect.users.application.dto.UpdateProfileRequest;
import com.petconnect.users.application.dto.UpdateProfilePreferencesRequest;
import com.petconnect.users.application.BlockedUserService;
import com.petconnect.social.application.PostVisibilityService;
import com.petconnect.users.application.usecases.CompleteOnboardingUseCase;
import com.petconnect.users.application.usecases.GetUserProfileUseCase;
import com.petconnect.users.application.usecases.GetUserPreferencesUseCase;
import com.petconnect.users.application.usecases.SearchProvidersUseCase;
import com.petconnect.users.application.usecases.UpdateProfileUseCase;
import com.petconnect.users.application.usecases.UpdateProfilePreferencesUseCase;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.exceptions.UserNotFoundException;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final GetUserPreferencesUseCase getUserPreferencesUseCase;
    private final SearchProvidersUseCase searchProvidersUseCase;
    private final CompleteOnboardingUseCase completeOnboardingUseCase;
    private final UpdateProfilePreferencesUseCase updateProfilePreferencesUseCase;
    private final UserProfileRepository userProfileRepository;
    private final ObjectProvider<WebSocketPresenceListener> presenceListenerProvider;
    @Autowired(required = false)
    private BlockedUserService blockedUserService;
    @Autowired(required = false)
    private PostVisibilityService postVisibilityService;

    public UserProfileController(
            UpdateProfileUseCase updateProfileUseCase,
            GetUserProfileUseCase getUserProfileUseCase,
            GetUserPreferencesUseCase getUserPreferencesUseCase,
            SearchProvidersUseCase searchProvidersUseCase,
            CompleteOnboardingUseCase completeOnboardingUseCase,
            UpdateProfilePreferencesUseCase updateProfilePreferencesUseCase,
            UserProfileRepository userProfileRepository,
            ObjectProvider<WebSocketPresenceListener> presenceListenerProvider) {
        this.updateProfileUseCase = updateProfileUseCase;
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.getUserPreferencesUseCase = getUserPreferencesUseCase;
        this.searchProvidersUseCase = searchProvidersUseCase;
        this.completeOnboardingUseCase = completeOnboardingUseCase;
        this.updateProfilePreferencesUseCase = updateProfilePreferencesUseCase;
        this.userProfileRepository = userProfileRepository;
        this.presenceListenerProvider = presenceListenerProvider;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = getUserProfileUseCase.execute(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<UserProfileResponse> getProfileById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID profileId) {
        var profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found: " + profileId));
        boolean isOwner = userDetails != null && profile.getAuthUserId().equals(userDetails.getUserId());
        ensureProfileIsVisible(profile, isOwner, userDetails);
        return ResponseEntity.ok(toResponse(profile, isOwner));
    }

    @GetMapping("/by-auth/{authUserId}")
    public ResponseEntity<UserProfileResponse> getProfileByAuthUserId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID authUserId) {
        var profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found for user: " + authUserId));
        boolean isOwner = userDetails != null && authUserId.equals(userDetails.getUserId());
        ensureProfileIsVisible(profile, isOwner, userDetails);
        return ResponseEntity.ok(toResponse(profile, isOwner));
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<UserProfileResponse>> getAllProfiles(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userProfileRepository.findAll().stream()
                .filter(profile -> canViewProfile(profile, userDetails))
                .map(profile -> toResponse(profile, false))
                .toList());
    }

    /**
     * Proveedores para "Servicios cercanos": filtra por tipo de perfil y, para
     * vets, por los códigos de servicios del catálogo (multi-selección, AND).
     */
    @GetMapping("/providers")
    public ResponseEntity<ProviderSearchResponse> searchProviders(
            @RequestParam(value = "profileType", required = false) String profileType,
            @RequestParam(value = "services", required = false) List<String> services,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(searchProvidersUseCase.execute(profileType, services, page, size));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "data", required = false) UpdateProfileRequest data,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) throws IOException {

        var profile = updateProfileUseCase.execute(userDetails.getUserId(), data, avatar, coverImage);
        return ResponseEntity.ok(toResponse(profile));
    }

    @PatchMapping("/profile/preferences")
    public ResponseEntity<UserPreferencesResponse> updateProfilePreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfilePreferencesRequest request) {
        var preferences = updateProfilePreferencesUseCase.execute(userDetails.getUserId(), request);
        presenceListenerProvider.ifAvailable(listener ->
                listener.refreshOnlineStatusVisibility(userDetails.getUserId()));
        return ResponseEntity.ok(preferences);
    }

    @GetMapping("/profile/preferences")
    public ResponseEntity<UserPreferencesResponse> getProfilePreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(getUserPreferencesUseCase.execute(userDetails.getUserId()));
    }

    // ===== Onboarding post-registro =====

    /**
     * Completa el onboarding: el usuario elige su tipo de cuenta y confirma sus
     * datos. Es la única vía que marca el perfil como completo y desbloquea el
     * acceso a las rutas protegidas.
     */
    @PutMapping("/onboarding")
    public ResponseEntity<UserProfileResponse> completeOnboarding(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OnboardingProfileRequest request) {
        var response = completeOnboardingUseCase.execute(userDetails.getUserId(), request);
        return ResponseEntity.ok(response);
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

    private void ensureProfileIsVisible(
            UserProfile profile, boolean isOwner, CustomUserDetails userDetails) {
        if (!canViewProfile(profile, userDetails) && !isOwner) {
            throw new UserNotFoundException("Profile not found: " + profile.getId());
        }
    }

    private boolean canViewProfile(UserProfile profile, CustomUserDetails userDetails) {
        if (postVisibilityService != null) {
            return postVisibilityService.canViewProfile(
                    profile, userDetails == null ? null : userDetails.getUserId());
        }
        return (userDetails == null || blockedUserService == null
                || !blockedUserService.isBlocked(userDetails.getUserId(), profile.getAuthUserId()))
                && (profile.isProfilePublic()
                        || userDetails != null && userDetails.getUserId().equals(profile.getAuthUserId()));
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
        return toResponse(profile, true);
    }

    private UserProfileResponse toResponse(UserProfile profile, boolean includePrivateFields) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getAuthUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                includePrivateFields ? profile.getPhone() : null,
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getCoverImageUrl(),
                includePrivateFields ? profile.getDateOfBirth() : null,
                profile.getLocation(),
                profile.getLatitude(),
                profile.getLongitude(),
                profile.isProfilePublic(),
                includePrivateFields && profile.isNotificationsEnabled(),
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