package com.petconnect.users.domain.repositories;

import com.petconnect.users.domain.UserProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository {
    Optional<UserProfile> findById(UUID id);

    Optional<UserProfile> findByAuthUserId(UUID authUserId);

    UserProfile save(UserProfile userProfile);

    void delete(UserProfile userProfile);
}