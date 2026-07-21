package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.Follow;
import com.petconnect.social.domain.repositories.FollowRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class JpaFollowRepositoryAdapter implements FollowRepository {

    private final SpringDataFollowRepository springDataFollowRepository;

    public JpaFollowRepositoryAdapter(SpringDataFollowRepository springDataFollowRepository) {
        this.springDataFollowRepository = springDataFollowRepository;
    }

    @Override
    public List<Follow> findByFollowerId(UUID followerId) {
        return springDataFollowRepository.findByFollowerId(followerId);
    }

    @Override
    public List<Follow> findByFollowingId(UUID followingId) {
        return springDataFollowRepository.findByFollowingId(followingId);
    }

    @Override
    public boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId) {
        return springDataFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public Follow save(Follow follow) {
        return springDataFollowRepository.save(follow);
    }

    @Override
    public void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId) {
        springDataFollowRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }
}