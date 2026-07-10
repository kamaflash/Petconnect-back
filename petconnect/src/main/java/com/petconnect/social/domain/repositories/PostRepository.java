package com.petconnect.social.domain.repositories;

import com.petconnect.social.domain.Post;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {
    Optional<Post> findById(UUID id);

    List<Post> findAll();

    List<Post> findByAuthorId(UUID authorId);

    Post save(Post post);

    void delete(Post post);
}