package com.petconnect.auth.infrastructure.persistence;

import com.petconnect.auth.domain.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataAuthUserRepository extends JpaRepository<AuthUser, UUID> {
    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findByRefreshToken(String refreshToken);

    Optional<AuthUser> findByEmailChangeTokenHash(String tokenHash);

    boolean existsByEmail(String email);
}