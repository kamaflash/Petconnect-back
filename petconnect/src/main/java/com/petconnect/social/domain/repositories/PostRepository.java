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

    /** Devuelve una pÃƒÆ’Ã‚Â¡gina de posts ordenada segÃƒÆ’Ã‚Âºn el {@link Pageable} indicado. */
    Page<Post> findAll(Pageable pageable);

    /** Devuelve una pÃƒÆ’Ã‚Â¡gina de posts activos ordenada segÃƒÆ’Ã‚Âºn el {@link Pageable} indicado. */
    Page<Post> findActivePaginated(Pageable pageable);

    List<Post> findByAuthorId(UUID authorId);

    List<Post> findByAuthorIdIn(List<UUID> authorIds);

    /** Devuelve una pÃƒÆ’Ã‚Â¡gina de posts activos de los autores indicados. */
    Page<Post> findActiveByAuthorIdIn(List<UUID> authorIds, Pageable pageable);

    /** Devuelve una pÃƒÆ’Ã‚Â¡gina de posts activos de los autores indicados. */
    List<Post> findByPetId(UUID petId);

    Page<Post> findByPetIdAndActiveTrue(UUID petId, Pageable pageable);

    Post save(Post post);

    void delete(Post post);
}


