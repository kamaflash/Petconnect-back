package com.petconnect.auth.application.usecases;

import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChangePasswordUseCase {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordUseCase(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void execute(UUID userId, String currentPassword, String newPassword) {
        var user = authUserRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new AuthException("Current password is incorrect");
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
        user.clearRefreshToken();
        authUserRepository.save(user);
    }
}
