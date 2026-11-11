package com.petconnect.auth.presentation.rest;

import com.petconnect.auth.application.commands.LoginCommand;
import com.petconnect.auth.application.commands.RegisterUserCommand;
import com.petconnect.auth.application.dto.ChangeEmailRequest;
import com.petconnect.auth.application.dto.ChangePasswordRequest;
import com.petconnect.auth.application.dto.GoogleLoginRequest;
import com.petconnect.auth.application.dto.LoginRequest;
import com.petconnect.auth.application.dto.RegisterRequest;
import com.petconnect.auth.application.dto.SessionResponse;
import com.petconnect.auth.application.usecases.GoogleLoginUseCase;
import com.petconnect.auth.application.usecases.ChangePasswordUseCase;
import com.petconnect.auth.application.usecases.ConfirmEmailChangeUseCase;
import com.petconnect.auth.application.usecases.LoginUseCase;
import com.petconnect.auth.application.usecases.LogoutUseCase;
import com.petconnect.auth.application.usecases.RequestEmailChangeUseCase;
import com.petconnect.auth.application.usecases.RefreshTokenUseCase;
import com.petconnect.auth.application.usecases.RegisterUseCase;
import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.valueobjects.Email;
import com.petconnect.auth.presentation.rest.support.AuthCookieService;
import com.petconnect.gamification.application.events.XpEvent;
import com.petconnect.gamification.domain.ActionType;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Autenticación basada en cookies {@code HttpOnly} ({@code PC_AT} /
 * {@code PC_RT}). Los tokens nunca se exponen en el cuerpo de la respuesta
 * ni se aceptan en el cuerpo de las peticiones: el navegador los envía
 * automáticamente y el frontend usa {@code withCredentials}.
 */

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final AuthCookieService cookieService;
    @Autowired(required = false)
    private ChangePasswordUseCase changePasswordUseCase;
    @Autowired(required = false)
    private RequestEmailChangeUseCase requestEmailChangeUseCase;
    @Autowired(required = false)
    private ConfirmEmailChangeUseCase confirmEmailChangeUseCase;
    @Autowired(required = false)
    private LogoutUseCase logoutUseCase;
    @Autowired
    private GoogleLoginUseCase googleLoginUseCase;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public AuthController(
            RegisterUseCase registerUseCase,
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            AuthCookieService cookieService) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.cookieService = cookieService;
    }

    @PostMapping("/register")
    public ResponseEntity<SessionResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("POST /api/v1/auth/register - email: {}", request.email());
        var command = new RegisterUserCommand(
                new Email(request.email()),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.profileType());
        var response = registerUseCase.execute(command);
        var headers = cookieService.setSessionHeaders(response.accessToken(), response.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(SessionResponse.from(response));
    }

    @PostMapping("/login")
    public ResponseEntity<SessionResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("POST /api/v1/auth/login - email: {}", request.email());
        var command = new LoginCommand(new Email(request.email()), request.password());
        var response = loginUseCase.execute(command);
        publishGamification(ActionType.DAILY_LOGIN, response.userId());
        var headers = cookieService.setSessionHeaders(response.accessToken(), response.refreshToken());
        return ResponseEntity.ok().headers(headers).body(SessionResponse.from(response));
    }

    @PostMapping("/google")
    public ResponseEntity<SessionResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        log.debug("POST /api/v1/auth/google");
        var response = googleLoginUseCase.execute(request.idToken());
        var headers = cookieService.setSessionHeaders(response.accessToken(), response.refreshToken());
        return ResponseEntity.ok().headers(headers).body(SessionResponse.from(response));
    }

    /**
     * Renueva la sesión a partir de la cookie {@code PC_RT} (rotación: cada
     * refresh emite un refresh token nuevo y revoca el anterior). La cookie
     * viaja con {@code Path=/api/v1/auth}, por lo que el navegador solo la
     * envía a este endpoint.
     */
    @PostMapping("/refresh")
    public ResponseEntity<SessionResponse> refreshToken(
            @CookieValue(name = AuthCookieService.REFRESH_COOKIE, required = false) String refreshToken) {
        log.debug("POST /api/v1/auth/refresh");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException("Missing refresh token cookie");
        }
        var response = refreshTokenUseCase.execute(refreshToken);
        var headers = cookieService.setSessionHeaders(response.accessToken(), response.refreshToken());
        return ResponseEntity.ok().headers(headers).body(SessionResponse.from(response));
    }

    /**
     * Restaura la sesión tras una recarga: valida la cookie {@code PC_AT} y
     * devuelve los datos del usuario sin reemitir tokens.
     */
    @GetMapping("/session")
    public ResponseEntity<SessionResponse> session(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var response = refreshTokenUseCase.sessionFor(userDetails.getUserId());
        return ResponseEntity.ok(SessionResponse.from(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @CookieValue(name = AuthCookieService.ACCESS_COOKIE, required = false) String accessToken,
            @CookieValue(name = AuthCookieService.REFRESH_COOKIE, required = false) String refreshToken) {
        if (userDetails == null) {
            if (logoutUseCase != null) {
                logoutUseCase.executeByRefreshToken(refreshToken);
            }
            return ResponseEntity.noContent().headers(cookieService.clearSessionHeaders()).build();
        }
        log.debug("POST /api/v1/auth/logout");

        if (logoutUseCase != null) {
            logoutUseCase.execute(userDetails.getUserId(), accessToken);
        }
        HttpHeaders headers = cookieService.clearSessionHeaders();
        return ResponseEntity.noContent().headers(headers).build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @CookieValue(name = AuthCookieService.ACCESS_COOKIE, required = false) String accessToken,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (userDetails == null || changePasswordUseCase == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        changePasswordUseCase.execute(
                userDetails.getUserId(),
                request.currentPassword(),
                request.newPassword());
        if (logoutUseCase != null) {
            logoutUseCase.execute(userDetails.getUserId(), accessToken);
        }
        return ResponseEntity.noContent().headers(cookieService.clearSessionHeaders()).build();
    }

    @PostMapping("/change-email")
    public ResponseEntity<Void> changeEmail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangeEmailRequest request) {
        if (userDetails == null || requestEmailChangeUseCase == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        requestEmailChangeUseCase.execute(userDetails.getUserId(), request.newEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<Void> confirmEmailChange(@RequestParam String token) {
        if (confirmEmailChangeUseCase == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        confirmEmailChangeUseCase.execute(token);
        return ResponseEntity.noContent().build();
    }

    // ========== GAMIFICATION ==========

    private void publishGamification(ActionType actionType, UUID userId) {
        if (applicationEventPublisher == null) {
            return;
        }
        try {
            applicationEventPublisher.publishEvent(new XpEvent(userId, actionType));
        } catch (Exception e) {
            log.warn("No se pudo notificar XP de {} para el usuario {}", actionType, userId);
        }
    }
}
