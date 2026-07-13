package com.petconnect.auth.presentation.rest;

import com.petconnect.auth.application.commands.LoginCommand;
import com.petconnect.auth.application.commands.RegisterUserCommand;
import com.petconnect.auth.application.dto.AuthResponse;
import com.petconnect.auth.application.dto.LoginRequest;
import com.petconnect.auth.application.dto.RefreshTokenRequest;
import com.petconnect.auth.application.dto.RegisterRequest;
import com.petconnect.auth.application.usecases.LoginUseCase;
import com.petconnect.auth.application.usecases.LogoutUseCase;
import com.petconnect.auth.application.usecases.RefreshTokenUseCase;
import com.petconnect.auth.application.usecases.RegisterUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import com.petconnect.auth.domain.valueobjects.Email;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    @Autowired(required = false)
    private LogoutUseCase logoutUseCase;

    public AuthController(
            RegisterUseCase registerUseCase,
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("POST /api/v1/auth/register - email: {}", request.email());
        var command = new RegisterUserCommand(
                new Email(request.email()),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.profileType());
        var response = registerUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("POST /api/v1/auth/login - email: {}", request.email());
        var command = new LoginCommand(new Email(request.email()), request.password());
        var response = loginUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("POST /api/v1/auth/refresh");
        var response = refreshTokenUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader("Authorization") String authHeader) {
        log.debug("POST /api/v1/auth/logout - userId: {}", userDetails.getUserId());

        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        if (logoutUseCase != null) {
            logoutUseCase.execute(userDetails.getUserId(), accessToken);
        }
        return ResponseEntity.noContent().build();
    }
}
