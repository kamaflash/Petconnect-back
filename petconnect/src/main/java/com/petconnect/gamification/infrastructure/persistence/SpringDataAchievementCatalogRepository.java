package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.AchievementCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAchievementCatalogRepository extends JpaRepository<AchievementCatalog, UUID> {
    Optional<AchievementCatalog> findByCode(String code);
}