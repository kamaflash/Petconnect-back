package com.petconnect.auth.application.usecases;

import com.petconnect.auth.domain.AuthUser;
import com.petconnect.auth.domain.UserRole;
import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ChangePasswordUseCase useCase;
    private AuthUser user;

    @BeforeEach
    void setUp() {
        useCase = new ChangePasswordUseCase(authUserRepository, passwordEncoder);
        user = new AuthUser("user@example.com", "encoded-current-password", UserRole.USER);
        user.updateRefreshToken("refresh-token", LocalDateTime.now().plusDays(1));
        user.requestEmailChange(
                "pending@example.com",
                "token-hash",
                LocalDateTime.now().plusHours(1));
    }

    @Test
    void changesPasswordAndInvalidatesRefreshToken() {
        when(authUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "encoded-current-password")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        useCase.execute(user.getId(), "current-password", "new-password");

        assertEquals("encoded-new-password", user.getPassword());
        assertNull(user.getRefreshToken());
        assertNull(user.getPendingEmail());
        assertNull(user.getEmailChangeTokenHash());
        verify(authUserRepository).save(user);
    }

    @Test
    void rejectsAnIncorrectCurrentPasswordWithoutSaving() {
        when(authUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-current-password")).thenReturn(false);

        assertThrows(
                AuthException.class,
                () -> useCase.execute(user.getId(), "wrong-password", "new-password"));

        assertEquals("encoded-current-password", user.getPassword());
        verify(authUserRepository, never()).save(user);
    }
}
