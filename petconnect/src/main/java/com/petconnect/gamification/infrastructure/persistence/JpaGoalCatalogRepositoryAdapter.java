package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.GoalCatalog;
import com.petconnect.gamification.domain.repositories.GoalCatalogRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaGoalCatalogRepositoryAdapter implements GoalCatalogRepository {

    private final SpringDataGoalCatalogRepository repository;

    public JpaGoalCatalogRepositoryAdapter(SpringDataGoalCatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<GoalCatalog> findAllActive() {
        return repository.findAllByActiveTrue();
    }
}