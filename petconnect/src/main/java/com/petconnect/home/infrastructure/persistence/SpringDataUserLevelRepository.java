package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.UserLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserLevelRepository extends JpaRepository<UserLevel, UUID> {
    Optional<UserLevel> findFirstByOrderByIdDesc();
}