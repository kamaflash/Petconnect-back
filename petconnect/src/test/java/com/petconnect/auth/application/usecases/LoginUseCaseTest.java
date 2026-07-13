package com.petconnect.auth.application.usecases;

import com.petconnect.auth.application.commands.LoginCommand;
import com.petconnect.auth.application.dto.AuthResponse;
import com.petconnect.auth.domain.AuthUser;
import com.petconnect.auth.domain.UserRole;
import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import com.petconnect.shared.infrastructure.security.JwtService;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private AuthUserRepository authUserRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        loginUseCase = new LoginUseCase(
                authUserRepository, userProfileRepository,
                passwordEncoder, jwtService, 3600000L);
    }

    @Test
    void shouldLoginSuccessfully() {
        var userId = UUID.randomUUID();
        var encodedPassword = passwordEncoder.encode("password123");
        var authUser = new AuthUser("test@example.com", encodedPassword, UserRole.USER);
        authUser.setId(userId);

        when(authUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(authUser));

        var savedProfile = new UserProfile(userId, "John", "Doe", "USER");
        when(userProfileRepository.findByAuthUserId(userId)).thenReturn(Optional.of(savedProfile));
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");

        var response = loginUseCase.execute(new LoginCommand(
                new com.petconnect.auth.domain.valueobjects.Email("test@example.com"),
                "password123"));

        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        assertEquals("USER", response.role());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertNotNull(response.profile());
        assertEquals("John", response.profile().firstName());
    }

    @Test
    void shouldThrowWhenEmailNotFound() {
        when(authUserRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> loginUseCase.execute(
                new LoginCommand(
                        new com.petconnect.auth.domain.valueobjects.Email("unknown@example.com"),
                        "password123")));
    }

    @Test
    void shouldThrowWhenAccountDisabled() {
        var authUser = new AuthUser("disabled@example.com", "encoded", UserRole.USER);
        authUser.disable();
        authUser.setId(UUID.randomUUID());

        when(authUserRepository.findByEmail("disabled@example.com")).thenReturn(Optional.of(authUser));

        assertThrows(AuthException.class, () -> loginUseCase.execute(
                new LoginCommand(
                        new com.petconnect.auth.domain.valueobjects.Email("disabled@example.com"),
                        "password123")));
    }

    @Test
    void shouldThrowWhenPasswordIncorrect() {
        var encodedPassword = passwordEncoder.encode("correctPassword");
        var authUser = new AuthUser("test@example.com", encodedPassword, UserRole.USER);
        authUser.setId(UUID.randomUUID());

        when(authUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(authUser));

        assertThrows(AuthException.class, () -> loginUseCase.execute(
                new LoginCommand(
                        new com.petconnect.auth.domain.valueobjects.Email("test@example.com"),
                        "wrongPassword")));
    }
}