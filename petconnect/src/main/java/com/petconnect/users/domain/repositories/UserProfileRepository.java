package com.petconnect.users.domain.repositories;

import com.petconnect.users.domain.UserProfile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository {
    Optional<UserProfile> findById(UUID id);

    Optional<UserProfile> findByAuthUserId(UUID authUserId);

    List<UserProfile> findByPendingProfileTypeIsNotNull();

    List<UserProfile> findAll();

    Page<UserProfile> findAll(Specification<UserProfile> spec, Pageable pageable);

    UserProfile save(UserProfile userProfile);

    void delete(UserProfile userProfile);
}
