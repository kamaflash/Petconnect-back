package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.UserGoalProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserGoalProgressRepository extends JpaRepository<UserGoalProgress, UUID> {
    Optional<UserGoalProgress> findByUserIdAndGoalCode(UUID userId, String goalCode);

    List<UserGoalProgress> findAllByUserId(UUID userId);
}