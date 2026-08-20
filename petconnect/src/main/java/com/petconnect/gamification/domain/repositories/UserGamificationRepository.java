package com.petconnect.gamification.domain.repositories;

import com.petconnect.gamification.domain.UserGamification;

import java.util.Optional;
import java.util.UUID;

public interface UserGamificationRepository {

    Optional<UserGamification> findByUserId(UUID userId);

    UserGamification save(UserGamification userGamification);
}