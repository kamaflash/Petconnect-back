package com.petconnect.social.domain.repositories;

import com.petconnect.social.domain.Comment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {

    Optional<Comment> findById(UUID id);

    List<Comment> findAll();

    List<Comment> findByTargetIdAndTargetTypeAndActiveTrueOrderByCreatedAtAsc(UUID targetId, String targetType);

    List<Comment> findByUserIdAndActiveTrueOrderByCreatedAtDesc(UUID userId);

    Optional<Comment> findByIdAndUserId(UUID id, UUID userId);

    long countByTargetIdAndTargetTypeAndActiveTrue(UUID targetId, String targetType);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    Comment save(Comment comment);

    void delete(Comment comment);

    List<Comment> findCommentsByTarget(UUID targetId, String targetType);
}
