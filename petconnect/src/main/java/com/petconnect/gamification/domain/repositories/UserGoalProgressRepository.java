package com.petconnect.gamification.domain.repositories;

import com.petconnect.gamification.domain.UserGoalProgress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserGoalProgressRepository {

    Optional<UserGoalProgress> findByUserIdAndGoalCode(UUID userId, String goalCode);

    List<UserGoalProgress> findAllByUserId(UUID userId);

    UserGoalProgress save(UserGoalProgress progress);
}