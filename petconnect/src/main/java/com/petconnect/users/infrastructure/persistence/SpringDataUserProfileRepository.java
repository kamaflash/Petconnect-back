package com.petconnect.users.infrastructure.persistence;

import com.petconnect.users.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataUserProfileRepository extends JpaRepository<UserProfile, UUID>, JpaSpecificationExecutor<UserProfile> {
    Optional<UserProfile> findByAuthUserId(UUID authUserId);

    List<UserProfile> findByPendingProfileTypeIsNotNull();
}