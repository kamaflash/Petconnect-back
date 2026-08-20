package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.UserGamification;
import com.petconnect.gamification.domain.repositories.UserGamificationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserGamificationRepositoryAdapter implements UserGamificationRepository {

    private final SpringDataUserGamificationRepository repository;

    public JpaUserGamificationRepositoryAdapter(SpringDataUserGamificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserGamification> findByUserId(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public UserGamification save(UserGamification userGamification) {
        return repository.save(userGamification);
    }
}