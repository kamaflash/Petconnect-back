package com.petconnect.users.presentation.rest;

import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.users.application.dtos.UpdateProfileRequest;
import com.petconnect.users.application.usecases.UpdateProfileUseCase;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private final UpdateProfileUseCase updateProfileUseCase;
    private final UserProfileRepository userProfileRepository;

    public UserProfileController(
            UpdateProfileUseCase updateProfileUseCase,
            UserProfileRepository userProfileRepository) {
        this.updateProfileUseCase = updateProfileUseCase;
        this.userProfileRepository = userProfileRepository;
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