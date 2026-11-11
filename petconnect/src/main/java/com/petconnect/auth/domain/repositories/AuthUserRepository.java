package com.petconnect.auth.domain.repositories;

import com.petconnect.auth.domain.AuthUser;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository {
    Optional<AuthUser> findById(UUID id);

    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findByRefreshToken(String refreshToken);

    Optional<AuthUser> findByEmailChangeTokenHash(String tokenHash);

    boolean existsByEmail(String email);

    AuthUser save(AuthUser authUser);

    void delete(AuthUser authUser);
}