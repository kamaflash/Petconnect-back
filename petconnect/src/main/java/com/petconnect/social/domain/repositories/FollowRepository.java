package com.petconnect.social.domain.repositories;

import com.petconnect.social.domain.Follow;

import java.util.List;
import java.util.UUID;

public interface FollowRepository {
    List<Follow> findByFollowerId(UUID followerId);

    List<Follow> findByFollowingId(UUID followingId);

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    Follow save(Follow follow);

    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
}