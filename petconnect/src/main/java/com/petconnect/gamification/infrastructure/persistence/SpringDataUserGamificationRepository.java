package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.UserGamification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserGamificationRepository extends JpaRepository<UserGamification, UUID> {
    Optional<UserGamification> findByUserId(UUID userId);
}