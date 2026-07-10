package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataPostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByAuthorId(UUID authorId);
}