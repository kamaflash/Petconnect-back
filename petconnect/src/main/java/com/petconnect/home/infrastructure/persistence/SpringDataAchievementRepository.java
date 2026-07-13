package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataAchievementRepository extends JpaRepository<Achievement, UUID> {
}