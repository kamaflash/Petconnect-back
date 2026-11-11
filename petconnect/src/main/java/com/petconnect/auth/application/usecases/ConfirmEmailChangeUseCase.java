package com.petconnect.auth.application.usecases;

import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ConfirmEmailChangeUseCase {

    private final AuthUserRepository authUserRepository;

    public ConfirmEmailChangeUseCase(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Transactional
    public void execute(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthException("Invalid email verification token");
        }

        var tokenHash = EmailChangeTokenHasher.hash(token);
        var user = authUserRepository.findByEmailChangeTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException("Invalid or expired email verification token"));
        var pendingEmail = user.getPendingEmail();
        var now = LocalDateTime.now();
        if (pendingEmail == null
                || user.getEmailChangeTokenExpiry() == null
                || !user.getEmailChangeTokenExpiry().isAfter(now)) {
            throw new AuthException("Invalid or expired email verification token");
        }
        if (authUserRepository.existsByEmail(pendingEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The email is already in use");
        }

        user.completeEmailChange(tokenHash, now);
        authUserRepository.save(user);
    }
}
