package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.GoalCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataGoalCatalogRepository extends JpaRepository<GoalCatalog, UUID> {
    List<GoalCatalog> findAllByActiveTrue();
}