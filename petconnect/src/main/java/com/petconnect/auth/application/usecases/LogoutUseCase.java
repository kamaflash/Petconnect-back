package com.petconnect.auth.application.usecases;

import com.petconnect.auth.domain.repositories.AuthUserRepository;
import com.petconnect.shared.infrastructure.cache.CacheService;
import com.petconnect.shared.infrastructure.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LogoutUseCase {

    private static final Logger log = LoggerFactory.getLogger(LogoutUseCase.class);

    private final AuthUserRepository authUserRepository;
    private final JwtService jwtService;
    private final CacheService cacheService;

    public LogoutUseCase(AuthUserRepository authUserRepository, JwtService jwtService,
            @Autowired(required = false) CacheService cacheService) {
        this.authUserRepository = authUserRepository;
        this.jwtService = jwtService;
        this.cacheService = cacheService;
    }

    @Transactional
    public void execute(UUID userId, String accessToken) {
        log.info("Logging out user: userId={}", userId);

        // Clear refresh token from database
        authUserRepository.findById(userId).ifPresent(authUser -> {
            authUser.clearRefreshToken();
            authUserRepository.save(authUser);
        });

        // Blacklist the access token in Redis for remaining TTL (if Redis is available)
        if (cacheService != null && accessToken != null && !accessToken.isEmpty()) {
            cacheService.blacklistToken(accessToken, jwtService.getAccessTokenExpiration());
        }

        // Remove session from Redis (if Redis is available)
        if (cacheService != null) {
            cacheService.removeSession(userId.toString());
        }

        log.info("User logged out successfully: userId={}", userId);
    }
}
