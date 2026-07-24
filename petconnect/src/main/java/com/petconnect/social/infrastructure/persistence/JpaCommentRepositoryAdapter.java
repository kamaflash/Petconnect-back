package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.Comment;
import com.petconnect.social.domain.repositories.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCommentRepositoryAdapter implements CommentRepository {

    private final SpringDataCommentRepository springDataCommentRepository;

    @Autowired
    public JpaCommentRepositoryAdapter(SpringDataCommentRepository springDataCommentRepository) {
        this.springDataCommentRepository = springDataCommentRepository;
    }

    @Override
    public Optional<Comment> findById(UUID id) {
        return springDataCommentRepository.findById(id);
    }

    @Override
    public List<Comment> findAll() {
        return springDataCommentRepository.findAll();
    }

    @Override
    public List<Comment> findByTargetIdAndTargetTypeAndActiveTrueOrderByCreatedAtAsc(UUID targetId, String targetType) {
        return springDataCommentRepository.findByTargetIdAndTargetTypeAndActiveTrueOrderByCreatedAtAsc(targetId,
                targetType);
    }

    @Override
    public List<Comment> findByUserIdAndActiveTrueOrderByCreatedAtDesc(UUID userId) {
        return springDataCommentRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);
    }

    @Override
    public Optional<Comment> findByIdAndUserId(UUID id, UUID userId) {
        return springDataCommentRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public long countByTargetIdAndTargetTypeAndActiveTrue(UUID targetId, String targetType) {
        return springDataCommentRepository.countByTargetIdAndTargetTypeAndActiveTrue(targetId, targetType);
    }

    @Override
    public boolean existsByIdAndUserId(UUID id, UUID userId) {
        return springDataCommentRepository.existsByIdAndUserId(id, userId);
    }

    @Override
    public Comment save(Comment comment) {
        return springDataCommentRepository.save(comment);
    }

    @Override
    public void delete(Comment comment) {
        springDataCommentRepository.delete(comment);
    }

    @Override
    public List<Comment> findCommentsByTarget(UUID targetId, String targetType) {
        return springDataCommentRepository.findCommentsByTarget(targetId, targetType);
    }

    @Override
    public List<Comment> findTopLevelCommentsByTarget(UUID targetId, String targetType) {
        return springDataCommentRepository.findTopLevelCommentsByTarget(targetId, targetType);
    }

    @Override
    public List<Comment> findRepliesByParentId(UUID parentId) {
        return springDataCommentRepository.findRepliesByParentId(parentId);
    }
}
