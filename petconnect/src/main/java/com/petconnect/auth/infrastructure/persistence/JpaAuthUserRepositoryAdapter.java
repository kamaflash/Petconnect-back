package com.petconnect.auth.infrastructure.persistence;

import com.petconnect.auth.domain.AuthUser;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaAuthUserRepositoryAdapter implements AuthUserRepository {

    private final SpringDataAuthUserRepository springDataAuthUserRepository;

    public JpaAuthUserRepositoryAdapter(SpringDataAuthUserRepository springDataAuthUserRepository) {
        this.springDataAuthUserRepository = springDataAuthUserRepository;
    }

    @Override
    public Optional<AuthUser> findById(UUID id) {
        return springDataAuthUserRepository.findById(id);
    }

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        return springDataAuthUserRepository.findByEmail(email);
    }

    @Override
    public Optional<AuthUser> findByRefreshToken(String refreshToken) {
        return springDataAuthUserRepository.findByRefreshToken(refreshToken);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataAuthUserRepository.existsByEmail(email);
    }

    @Override
    public AuthUser save(AuthUser authUser) {
        return springDataAuthUserRepository.save(authUser);
    }

    @Override
    public void delete(AuthUser authUser) {
        springDataAuthUserRepository.delete(authUser);
    }
}