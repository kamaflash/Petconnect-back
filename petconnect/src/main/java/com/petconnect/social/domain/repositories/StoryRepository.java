package com.petconnect.social.domain.repositories;

import com.petconnect.social.domain.Story;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoryRepository {
    Optional<Story> findById(UUID id);

    List<Story> findAllActive();

    List<Story> findByUserId(UUID userId);

    List<Story> findActiveStoriesOrderByCreatedAt();
}
