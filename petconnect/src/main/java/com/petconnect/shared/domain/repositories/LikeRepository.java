package com.petconnect.shared.domain.repositories;

import com.petconnect.shared.domain.Like;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LikeRepository {
    Optional<Like> findById(UUID id);

    Optional<Like> findByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, String targetType);

    List<Like> findByTargetIdAndTargetType(UUID targetId, String targetType);

    List<Like> findByUserId(UUID userId);

    long countByTargetIdAndTargetType(UUID targetId, String targetType);

    boolean existsByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, String targetType);

    Like save(Like like);

    void delete(Like like);

    void deleteByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, String targetType);
}