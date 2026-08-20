package com.petconnect.gamification.domain.repositories;

import com.petconnect.gamification.domain.AchievementCatalog;

import java.util.List;
import java.util.Optional;

public interface AchievementCatalogRepository {

    List<AchievementCatalog> findAll();

    Optional<AchievementCatalog> findByCode(String code);
}