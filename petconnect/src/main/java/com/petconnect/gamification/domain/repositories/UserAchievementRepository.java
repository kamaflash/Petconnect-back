package com.petconnect.gamification.domain.repositories;

import com.petconnect.gamification.domain.UserAchievement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAchievementRepository {

    Optional<UserAchievement> findByUserIdAndAchievementCode(UUID userId, String achievementCode);

    List<UserAchievement> findAllByUserId(UUID userId);

    UserAchievement save(UserAchievement userAchievement);

    List<UserAchievement> saveAll(Iterable<UserAchievement> userAchievements);
}