package com.petconnect.auth.application.usecases;

import com.petconnect.auth.application.commands.RegisterUserCommand;
import com.petconnect.auth.application.dto.AuthResponse;
import com.petconnect.auth.domain.AuthUser;
import com.petconnect.auth.domain.UserRole;
import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import com.petconnect.shared.domain.DomainEventPublisher;
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
class RegisterUseCaseTest {

    @Mock
    private AuthUserRepository authUserRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private DomainEventPublisher eventPublisher;

    private PasswordEncoder passwordEncoder;
    private RegisterUseCase registerUseCase;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        registerUseCase = new RegisterUseCase(
                authUserRepository, userProfileRepository,
                passwordEncoder, jwtService, eventPublisher, 3600000L);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        var command = new RegisterUserCommand(
                new com.petconnect.auth.domain.valueobjects.Email("test@example.com"),
                "password123", "John", "Doe", "USER");

        when(authUserRepository.existsByEmail("test@example.com")).thenReturn(false);

        var savedUserId = UUID.randomUUID();
        when(authUserRepository.save(any(AuthUser.class))).thenAnswer(invocation -> {
            var user = invocation.<AuthUser>getArgument(0);
            user.setId(savedUserId);
            return user;
        });

        var savedProfile = new UserProfile(savedUserId, "John", "Doe", "USER");
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(savedProfile);
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");

        var response = registerUseCase.execute(command);

        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        assertEquals("USER", response.role());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals(3600L, response.expiresIn());

        verify(authUserRepository, times(2)).save(any(AuthUser.class));
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        var command = new RegisterUserCommand(
                new com.petconnect.auth.domain.valueobjects.Email("existing@example.com"),
                "password123", "John", "Doe", "USER");

        when(authUserRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(AuthException.class, () -> registerUseCase.execute(command));
        verify(authUserRepository, never()).save(any());
        verify(userProfileRepository, never()).save(any());
    }
}