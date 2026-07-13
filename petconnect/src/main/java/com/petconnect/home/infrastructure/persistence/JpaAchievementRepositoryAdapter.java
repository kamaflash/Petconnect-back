package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.Achievement;
import com.petconnect.home.domain.repositories.AchievementRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaAchievementRepositoryAdapter implements AchievementRepository {

    private final SpringDataAchievementRepository repository;

    public JpaAchievementRepositoryAdapter(SpringDataAchievementRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Achievement> findAll() {
        return repository.findAll();
    }
}