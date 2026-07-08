package com.petconnect.auth.application.usecases;

import com.petconnect.auth.application.commands.RegisterUserCommand;
import com.petconnect.auth.application.dto.AuthResponse;
import com.petconnect.auth.domain.AuthUser;
import com.petconnect.auth.domain.UserRole;
import com.petconnect.auth.domain.events.UserRegisteredEvent;
import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import com.petconnect.shared.domain.DomainEventPublisher;
import com.petconnect.shared.infrastructure.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegisterUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUseCase.class);

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DomainEventPublisher eventPublisher;
    private final long accessTokenExpiration;

    public RegisterUseCase(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            DomainEventPublisher eventPublisher,
            @Value("${jwt.access-token.expiration}") long accessTokenExpiration) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    @Transactional
    public AuthResponse execute(RegisterUserCommand command) {
        log.info("Registering new user with email: {}", command.email());

        if (authUserRepository.existsByEmail(command.email().value())) {
            log.warn("Registration failed - email already exists: {}", command.email());
            throw new AuthException("Email already registered");
        }

        var encodedPassword = passwordEncoder.encode(command.password());
        var authUser = new AuthUser(command.email().value(), encodedPassword, UserRole.USER);

        var savedUser = authUserRepository.save(authUser);

        var accessToken = jwtService.generateAccessToken(savedUser.getId(), savedUser.getEmail(),
                savedUser.getRole().name());
        var refreshToken = jwtService.generateRefreshToken(savedUser.getId(), savedUser.getEmail());

        savedUser.updateRefreshToken(refreshToken, LocalDateTime.now().plusDays(30));
        authUserRepository.save(savedUser);

        // Publish domain event
        var event = new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail());
        eventPublisher.publish(event);
        log.info("User registered successfully: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                accessToken,
                refreshToken,
                accessTokenExpiration / 1000);
    }
}