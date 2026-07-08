package com.petconnect.auth.application.usecases;

import com.petconnect.auth.application.dto.AuthResponse;
import com.petconnect.auth.application.dto.RefreshTokenRequest;
import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import com.petconnect.shared.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RefreshTokenUseCase {

    private final AuthUserRepository authUserRepository;
    private final JwtService jwtService;
    private final long accessTokenExpiration;

    public RefreshTokenUseCase(
            AuthUserRepository authUserRepository,
            JwtService jwtService,
            @Value("${jwt.access-token.expiration}") long accessTokenExpiration) {
        this.authUserRepository = authUserRepository;
        this.jwtService = jwtService;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    @Transactional
    public AuthResponse execute(RefreshTokenRequest request) {
        if (!jwtService.isRefreshTokenValid(request.refreshToken())) {
            throw new AuthException("Invalid or expired refresh token");
        }

        var email = jwtService.extractEmailFromRefreshToken(request.refreshToken());
        var authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));

        if (authUser.getRefreshToken() == null || !authUser.getRefreshToken().equals(request.refreshToken())) {
            throw new AuthException("Invalid refresh token");
        }

        if (authUser.getRefreshTokenExpiry() != null
                && authUser.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new AuthException("Refresh token has expired");
        }

        var newAccessToken = jwtService.generateAccessToken(authUser.getId(), authUser.getEmail(),
                authUser.getRole().name());
        var newRefreshToken = jwtService.generateRefreshToken(authUser.getId(), authUser.getEmail());

        authUser.updateRefreshToken(newRefreshToken, LocalDateTime.now().plusDays(30));
        authUserRepository.save(authUser);

        return new AuthResponse(
                authUser.getId(),
                authUser.getEmail(),
                authUser.getRole().name(),
                newAccessToken,
                newRefreshToken,
                accessTokenExpiration / 1000);
    }
}