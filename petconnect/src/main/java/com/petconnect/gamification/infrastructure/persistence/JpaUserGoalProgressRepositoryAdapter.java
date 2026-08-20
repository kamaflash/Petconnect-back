package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.UserGoalProgress;
import com.petconnect.gamification.domain.repositories.UserGoalProgressRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserGoalProgressRepositoryAdapter implements UserGoalProgressRepository {

    private final SpringDataUserGoalProgressRepository repository;

    public JpaUserGoalProgressRepositoryAdapter(SpringDataUserGoalProgressRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserGoalProgress> findByUserIdAndGoalCode(UUID userId, String goalCode) {
        return repository.findByUserIdAndGoalCode(userId, goalCode);
    }

    @Override
    public List<UserGoalProgress> findAllByUserId(UUID userId) {
        return repository.findAllByUserId(userId);
    }

    @Override
    public UserGoalProgress save(UserGoalProgress progress) {
        return repository.save(progress);
    }
}