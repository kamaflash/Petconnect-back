package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    Optional<UserAchievement> findByUserIdAndAchievementCode(UUID userId, String achievementCode);

    List<UserAchievement> findAllByUserId(UUID userId);
}