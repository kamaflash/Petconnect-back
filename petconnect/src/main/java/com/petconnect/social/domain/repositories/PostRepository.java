package com.petconnect.social.domain.repositories;

import com.petconnect.social.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {
    Optional<Post> findById(UUID id);

    List<Post> findAll();

    /** Devuelve una página de posts ordenada según el {@link Pageable} indicado. */
    Page<Post> findAll(Pageable pageable);

    List<Post> findByAuthorId(UUID authorId);

    List<Post> findByAuthorIdIn(List<UUID> authorIds);

    Post save(Post post);

    void delete(Post post);
}
