package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.UserAchievement;
import com.petconnect.gamification.domain.repositories.UserAchievementRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserAchievementRepositoryAdapter implements UserAchievementRepository {

    private final SpringDataUserAchievementRepository repository;

    public JpaUserAchievementRepositoryAdapter(SpringDataUserAchievementRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserAchievement> findByUserIdAndAchievementCode(UUID userId, String achievementCode) {
        return repository.findByUserIdAndAchievementCode(userId, achievementCode);
    }

    @Override
    public List<UserAchievement> findAllByUserId(UUID userId) {
        return repository.findAllByUserId(userId);
    }

    @Override
    public UserAchievement save(UserAchievement userAchievement) {
        return repository.save(userAchievement);
    }

    @Override
    public List<UserAchievement> saveAll(Iterable<UserAchievement> userAchievements) {
        return repository.saveAll(userAchievements);
    }
}