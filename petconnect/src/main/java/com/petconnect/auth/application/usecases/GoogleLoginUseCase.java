package com.petconnect.auth.application.usecases;

import com.petconnect.auth.domain.AuthUser;
import com.petconnect.auth.domain.UserRole;
import com.petconnect.auth.domain.events.UserRegisteredEvent;
import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import com.petconnect.auth.application.dto.AuthResponse;
import com.petconnect.auth.infrastructure.security.GoogleIdTokenVerifier;
import com.petconnect.auth.infrastructure.security.GoogleIdTokenVerifier.GoogleUserDetails;
import com.petconnect.shared.domain.DomainEventPublisher;
import com.petconnect.shared.infrastructure.security.JwtService;
import com.petconnect.users.application.dto.UserProfileResponse;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Login/registro mediante cuentas de Google (acepta cualquier cuenta verificada).
 * Si el email no existe se crea la cuenta y el perfil automáticamente.
 */
@Service
public class GoogleLoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(GoogleLoginUseCase.class);

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final AuthUserRepository authUserRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DomainEventPublisher eventPublisher;
    private final long accessTokenExpiration;

    public GoogleLoginUseCase(
            GoogleIdTokenVerifier googleIdTokenVerifier,
            AuthUserRepository authUserRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            DomainEventPublisher eventPublisher,
            @Value("${jwt.access-token.expiration}") long accessTokenExpiration) {
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.authUserRepository = authUserRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
        this.accessTokenExpiration = accessTokenExpiration;
    }
@Transactional
    public AuthResponse execute(String idToken) {
        GoogleUserDetails googleUser = googleIdTokenVerifier.verify(idToken);
        String email = googleUser.email();
        log.info("Google login attempt for email: {}", email);

        AuthUser authUser = authUserRepository.findByEmail(email)
                .orElseGet(() -> registerGoogleUser(googleUser));

        if (!authUser.isEnabled()) {
            log.warn("Google login failed - account disabled: {}", email);
            throw new AuthException("Account is disabled");
        }

        var accessToken = jwtService.generateAccessToken(authUser.getId(), authUser.getEmail(),
                authUser.getRole().name());
        var refreshToken = jwtService.generateRefreshToken(authUser.getId(), authUser.getEmail());

        authUser.updateRefreshToken(refreshToken, LocalDateTime.now().plusDays(30));
        authUserRepository.save(authUser);

        var profileResponse = userProfileRepository.findByAuthUserId(authUser.getId())
                .map(this::toProfileResponse)
                .orElse(null);

        log.info("Google login successful: userId={}, email={}", authUser.getId(), email);

        return new AuthResponse(
                authUser.getId(),
                authUser.getEmail(),
                authUser.getRole().name(),
                accessToken,
                refreshToken,
                accessTokenExpiration / 1000,
                profileResponse);
    }

    /** Crea la cuenta y el perfil si es la primera vez que entra con Google. */
    private AuthUser registerGoogleUser(GoogleUserDetails googleUser) {
        // Cuenta creada por OAuth: no tiene contraseña de acceso, usamos un valor aleatorio.
        var encodedRandomPassword = passwordEncoder.encode(UUID.randomUUID().toString());
        var authUser = new AuthUser(googleUser.email(), encodedRandomPassword, UserRole.USER);
        authUser.verifyEmail(); // Google ya verifica el email

        var savedUser = authUserRepository.save(authUser);

        var firstName = googleUser.firstName() != null ? googleUser.firstName() : "";
        var lastName = googleUser.lastName() != null ? googleUser.lastName() : "";
        var userProfile = new UserProfile(savedUser.getId(), firstName, lastName, "user");
        userProfileRepository.save(userProfile);

        eventPublisher.publish(new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail()));
        log.info("Google user registered: userId={}, email={}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    private UserProfileResponse toProfileResponse(UserProfile profile) {
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
                profile.getWebsite());
    }
}