package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataCommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByTargetIdAndTargetTypeAndActiveTrueOrderByCreatedAtAsc(UUID targetId, String targetType);

    List<Comment> findByUserIdAndActiveTrueOrderByCreatedAtDesc(UUID userId);

    Optional<Comment> findByIdAndUserId(UUID id, UUID userId);

    long countByTargetIdAndTargetTypeAndActiveTrue(UUID targetId, String targetType);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT c FROM Comment c WHERE c.targetId = :targetId AND c.targetType = :targetType AND c.active = true ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByTarget(@Param("targetId") UUID targetId, @Param("targetType") String targetType);
}
