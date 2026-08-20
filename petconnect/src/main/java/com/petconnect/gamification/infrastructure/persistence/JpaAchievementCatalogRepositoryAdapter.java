package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.AchievementCatalog;
import com.petconnect.gamification.domain.repositories.AchievementCatalogRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaAchievementCatalogRepositoryAdapter implements AchievementCatalogRepository {

    private final SpringDataAchievementCatalogRepository repository;

    public JpaAchievementCatalogRepositoryAdapter(SpringDataAchievementCatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AchievementCatalog> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<AchievementCatalog> findByCode(String code) {
        return repository.findByCode(code);
    }
}