package com.petconnect.auth.application.usecases;

import com.petconnect.auth.domain.AuthUser;
import com.petconnect.auth.domain.UserRole;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestEmailChangeUseCaseTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private JavaMailSender mailSender;

    private AuthUser user;
    private RequestEmailChangeUseCase useCase;

    @BeforeEach
    void setUp() {
        user = new AuthUser("current@example.com", "encoded-password", UserRole.USER);
        useCase = new RequestEmailChangeUseCase(
                authUserRepository,
                mailSender,
                "https://petconnect.example/",
                "no-reply@petconnect.example",
                24);
    }

    @Test
    void sendsVerificationLinkAndKeepsCurrentEmailUntilConfirmation() {
        when(authUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(authUserRepository.existsByEmail("new@example.com")).thenReturn(false);

        useCase.execute(user.getId(), "New@Example.com");

        assertEquals("current@example.com", user.getEmail());
        assertEquals("new@example.com", user.getPendingEmail());
        assertNotNull(user.getEmailChangeTokenExpiry());
        assertTrue(user.getEmailChangeTokenExpiry().isAfter(LocalDateTime.now()));

        var messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertEquals("new@example.com", messageCaptor.getValue().getTo()[0]);
        assertTrue(messageCaptor.getValue().getText().contains("/auth/confirm-email?token="));
        assertNotNull(user.getEmailChangeTokenHash());
        verify(authUserRepository).save(user);
    }

    @Test
    void rejectsEmailAlreadyOwnedByAnotherAccountWithoutSendingMail() {
        when(authUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(authUserRepository.existsByEmail(anyString())).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> useCase.execute(user.getId(), "existing@example.com"));

        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }
}
