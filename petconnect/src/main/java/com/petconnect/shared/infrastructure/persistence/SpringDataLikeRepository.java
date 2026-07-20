package com.petconnect.shared.infrastructure.persistence;

import com.petconnect.shared.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataLikeRepository extends JpaRepository<Like, UUID> {
    Optional<Like> findByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, String targetType);

    List<Like> findByTargetIdAndTargetType(UUID targetId, String targetType);

    List<Like> findByUserId(UUID userId);

    long countByTargetIdAndTargetType(UUID targetId, String targetType);

    boolean existsByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, String targetType);

    @Transactional
    @Modifying
    @Query("DELETE FROM Like l WHERE l.userId = :userId AND l.targetId = :targetId AND l.targetType = :targetType")
    void deleteByUserIdAndTargetIdAndTargetType(
            @Param("userId") UUID userId,
            @Param("targetId") UUID targetId,
            @Param("targetType") String targetType);
}
