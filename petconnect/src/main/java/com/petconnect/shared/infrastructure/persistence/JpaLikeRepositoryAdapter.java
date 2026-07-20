package com.petconnect.shared.infrastructure.persistence;

import com.petconnect.shared.domain.Like;
import com.petconnect.shared.domain.repositories.LikeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaLikeRepositoryAdapter implements LikeRepository {

    private final SpringDataLikeRepository springDataLikeRepository;

    public JpaLikeRepositoryAdapter(SpringDataLikeRepository springDataLikeRepository) {
        this.springDataLikeRepository = springDataLikeRepository;
    }

    @Override
    public Optional<Like> findById(UUID id) {
        return springDataLikeRepository.findById(id);
    }

    @Override
    public Optional<Like> findByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, String targetType) {
        return springDataLikeRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
    }

    @Override
    public List<Like> findByTargetIdAndTargetType(UUID targetId, String targetType) {
        return springDataLikeRepository.findByTargetIdAndTargetType(targetId, targetType);
    }

    @Override
    public List<Like> findByUserId(UUID userId) {
        return springDataLikeRepository.findByUserId(userId);
    }

    @Override
    public long countByTargetIdAndTargetType(UUID targetId, String targetType) {
        return springDataLikeRepository.countByTargetIdAndTargetType(targetId, targetType);
    }

    @Override
    public boolean existsByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, String targetType) {
        return springDataLikeRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
    }

    @Override
    public Like save(Like like) {
        return springDataLikeRepository.save(like);
    }

    @Override
    public void delete(Like like) {
        springDataLikeRepository.delete(like);
    }

    @Override
    public void deleteByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, String targetType) {
        springDataLikeRepository.deleteByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
    }
}