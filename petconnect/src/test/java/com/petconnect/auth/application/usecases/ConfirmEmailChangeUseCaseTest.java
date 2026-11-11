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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmEmailChangeUseCaseTest {

    @Mock
    private AuthUserRepository authUserRepository;

    private ConfirmEmailChangeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConfirmEmailChangeUseCase(authUserRepository);
    }

    @Test
    void updatesEmailOnlyAfterValidTokenAndInvalidatesRefreshSession() {
        var user = createPendingChange(LocalDateTime.now().plusHours(1));
        when(authUserRepository.findByEmailChangeTokenHash(anyString())).thenReturn(Optional.of(user));
        when(authUserRepository.existsByEmail("new@example.com")).thenReturn(false);

        useCase.execute("valid-token");

        assertEquals("new@example.com", user.getEmail());
        assertTrue(user.isEmailVerified());
        assertNull(user.getPendingEmail());
        assertNull(user.getEmailChangeTokenHash());
        assertNull(user.getRefreshToken());
        verify(authUserRepository).save(user);
    }

    @Test
    void rejectsExpiredTokenWithoutChangingEmail() {
        var user = createPendingChange(LocalDateTime.now().minusMinutes(1));
        when(authUserRepository.findByEmailChangeTokenHash(anyString())).thenReturn(Optional.of(user));

        assertThrows(AuthException.class, () -> useCase.execute("expired-token"));

        assertEquals("current@example.com", user.getEmail());
        verify(authUserRepository, never()).save(user);
    }

    private AuthUser createPendingChange(LocalDateTime expiry) {
        var user = new AuthUser("current@example.com", "encoded-password", UserRole.USER);
        user.updateRefreshToken("refresh-token", LocalDateTime.now().plusDays(1));
        user.requestEmailChange(
                "new@example.com",
                EmailChangeTokenHasher.hash("valid-token"),
                expiry);
        return user;
    }
}
