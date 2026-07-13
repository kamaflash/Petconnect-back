package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataStoryRepository extends JpaRepository<Story, UUID> {
    List<Story> findByUserId(UUID userId);

    List<Story> findBySeenFalseAndExpiresAtAfter(LocalDateTime expiresAt);
}
