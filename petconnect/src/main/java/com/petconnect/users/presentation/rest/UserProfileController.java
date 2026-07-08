package com.petconnect.users.presentation.rest;

import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.users.application.dto.UpdateProfileRequest;
import com.petconnect.users.application.dto.UserProfileResponse;
import com.petconnect.users.application.usecases.GetUserProfileUseCase;
import com.petconnect.users.application.usecases.UpdateProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    public UserProfileController(
            GetUserProfileUseCase getUserProfileUseCase,
            UpdateProfileUseCase updateProfileUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        var userId = getUserId(authentication);
        var response = getUserProfileUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        var userId = getUserId(authentication);
        var response = updateProfileUseCase.execute(userId, request);
        return ResponseEntity.ok(response);
    }

    private java.util.UUID getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }
        throw new IllegalStateException("User not authenticated");
    }
}