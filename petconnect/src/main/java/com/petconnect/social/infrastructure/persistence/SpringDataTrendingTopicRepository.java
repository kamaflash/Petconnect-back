package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.TrendingTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataTrendingTopicRepository extends JpaRepository<TrendingTopic, UUID> {
    List<TrendingTopic> findByActiveTrueOrderByPostsCountDesc();

    Optional<TrendingTopic> findByName(String name);
}
