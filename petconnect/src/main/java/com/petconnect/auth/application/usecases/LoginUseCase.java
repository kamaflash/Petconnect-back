package com.petconnect.auth.application.usecases;

import com.petconnect.auth.application.commands.LoginCommand;
import com.petconnect.auth.application.dto.AuthResponse;
import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import com.petconnect.shared.infrastructure.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUseCase.class);

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long accessTokenExpiration;

    public LoginUseCase(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${jwt.access-token.expiration}") long accessTokenExpiration) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    @Transactional
    public AuthResponse execute(LoginCommand command) {
        log.info("Login attempt for email: {}", command.email());

        var authUser = authUserRepository.findByEmail(command.email().value())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", command.email());
                    return new AuthException("Invalid email or password");
                });

        if (!authUser.isEnabled()) {
            log.warn("Login failed - account disabled: {}", command.email());
            throw new AuthException("Account is disabled");
        }

        if (!passwordEncoder.matches(command.password(), authUser.getPassword())) {
            log.warn("Login failed - invalid password for: {}", command.email());
            throw new AuthException("Invalid email or password");
        }

        var accessToken = jwtService.generateAccessToken(authUser.getId(), authUser.getEmail(),
                authUser.getRole().name());
        var refreshToken = jwtService.generateRefreshToken(authUser.getId(), authUser.getEmail());

        authUser.updateRefreshToken(refreshToken, LocalDateTime.now().plusDays(30));
        authUserRepository.save(authUser);

        log.info("Login successful: userId={}, email={}", authUser.getId(), authUser.getEmail());

        return new AuthResponse(
                authUser.getId(),
                authUser.getEmail(),
                authUser.getRole().name(),
                accessToken,
                refreshToken,
                accessTokenExpiration / 1000);
    }
}