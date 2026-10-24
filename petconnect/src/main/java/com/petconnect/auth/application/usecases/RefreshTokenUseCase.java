package com.petconnect.auth.application.usecases;

import com.petconnect.auth.application.dto.AuthResponse;
import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import com.petconnect.shared.infrastructure.security.JwtService;
import com.petconnect.users.application.dto.UserProfileResponse;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RefreshTokenUseCase {

    private final AuthUserRepository authUserRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;
    private final long accessTokenExpiration;

    public RefreshTokenUseCase(
            AuthUserRepository authUserRepository,
            UserProfileRepository userProfileRepository,
            JwtService jwtService,
            @Value("${jwt.access-token.expiration}") long accessTokenExpiration) {
        this.authUserRepository = authUserRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtService = jwtService;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    @Transactional(readOnly = true)
    public AuthResponse sessionFor(java.util.UUID userId) {
        var authUser = authUserRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));
        var userProfile = userProfileRepository.findByAuthUserId(authUser.getId())
                .orElse(null);
        boolean profileComplete = userProfile != null && userProfile.isProfileComplete();
        return new AuthResponse(
                authUser.getId(),
                authUser.getEmail(),
                authUser.getRole().name(),
                null,
                null,
                accessTokenExpiration / 1000,
                userProfile != null ? toProfileResponse(userProfile, profileComplete) : null,
                false,
                profileComplete);
    }

    @Transactional
    public AuthResponse execute(String refreshToken) {
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new AuthException("Invalid or expired refresh token");
        }

        var email = jwtService.extractEmailFromRefreshToken(refreshToken);
        var authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));

        if (authUser.getRefreshToken() == null || !authUser.getRefreshToken().equals(refreshToken)) {
            throw new AuthException("Invalid refresh token");
        }

        if (authUser.getRefreshTokenExpiry() != null
                && authUser.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new AuthException("Refresh token has expired");
        }

        var userProfile = userProfileRepository.findByAuthUserId(authUser.getId())
                .orElse(null);
        boolean profileComplete = userProfile != null && userProfile.isProfileComplete();

        var newAccessToken = jwtService.generateAccessToken(authUser.getId(), authUser.getEmail(),
                authUser.getRole().name(), profileComplete);
        var newRefreshToken = jwtService.generateRefreshToken(authUser.getId(), authUser.getEmail());

        authUser.updateRefreshToken(newRefreshToken, LocalDateTime.now().plusDays(30));
        authUserRepository.save(authUser);

        var profileResponse = userProfile != null
                ? toProfileResponse(userProfile, profileComplete)
                : null;

        return new AuthResponse(
                authUser.getId(),
                authUser.getEmail(),
                authUser.getRole().name(),
                newAccessToken,
                newRefreshToken,
                accessTokenExpiration / 1000,
                profileResponse,
                false,
                profileComplete);
    }

    private static UserProfileResponse toProfileResponse(
            com.petconnect.users.domain.UserProfile userProfile, boolean profileComplete) {
        return new UserProfileResponse(
                        userProfile.getId(),
                        userProfile.getAuthUserId(),
                        userProfile.getFirstName(),
                        userProfile.getLastName(),
                        userProfile.getPhone(),
                        userProfile.getBio(),
                        userProfile.getAvatarUrl(),
                        userProfile.getCoverImageUrl(),
                        userProfile.getDateOfBirth(),
                        userProfile.getLocation(),
                        userProfile.getLatitude(),
                        userProfile.getLongitude(),
                        userProfile.isProfilePublic(),
                        userProfile.isNotificationsEnabled(),
                        userProfile.getProfileType(),
                        userProfile.getSpecialty(),
                        userProfile.getLicenseNumber(),
                        userProfile.getCharityNumber(),
                        userProfile.getStoreName(),
                        userProfile.getWebsite(),
                        userProfile.getPendingProfileType(),
                        userProfile.getServices(),
                        profileComplete);
    }
}
