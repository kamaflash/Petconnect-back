package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.Story;
import com.petconnect.social.domain.repositories.StoryRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class JpaStoryRepositoryAdapter implements StoryRepository {

    private final SpringDataStoryRepository springDataStoryRepository;

    public JpaStoryRepositoryAdapter(SpringDataStoryRepository springDataStoryRepository) {
        this.springDataStoryRepository = springDataStoryRepository;
    }

    @Override
    public List<Story> findAllActive() {
        LocalDateTime now = LocalDateTime.now();
        return springDataStoryRepository.findBySeenFalseAndExpiresAtAfter(now);
    }

    @Override
    public List<Story> findByUserId(UUID userId) {
        return springDataStoryRepository.findByUserId(userId);
    }

    @Override
    public List<Story> findActiveStoriesOrderByCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        return springDataStoryRepository.findBySeenFalseAndExpiresAtAfter(now);
    }
}
