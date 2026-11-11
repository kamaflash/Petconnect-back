package com.petconnect.social.domain.repositories;

import com.petconnect.social.domain.Post;
import com.petconnect.social.domain.PostIntent;
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

    Page<Post> findActivePaginatedExcludingAuthors(List<UUID> excludedAuthorIds, Pageable pageable);

    List<Post> findByAuthorId(UUID authorId);

    List<Post> findByAuthorIdIn(List<UUID> authorIds);

    /** Devuelve una pÃƒÆ’Ã‚Â¡gina de posts activos de los autores indicados. */
    Page<Post> findActiveByAuthorIdIn(List<UUID> authorIds, Pageable pageable);

    /** Devuelve una pÃƒÆ’Ã‚Â¡gina de posts activos de los autores indicados. */
    Page<Post> findActiveByIntent(PostIntent intent, Pageable pageable);

    Page<Post> findActiveByIntentExcludingAuthors(
            PostIntent intent, List<UUID> excludedAuthorIds, Pageable pageable);

    Page<Post> findActiveByAuthorIdInAndIntent(List<UUID> authorIds, PostIntent intent, Pageable pageable);

    Page<Post> findActiveByAuthorIdInAndIntentExcludingAuthors(
            List<UUID> authorIds, PostIntent intent, List<UUID> excludedAuthorIds, Pageable pageable);

    Page<Post> findActiveByAuthorIdInExcludingAuthors(
            List<UUID> authorIds, List<UUID> excludedAuthorIds, Pageable pageable);

    Page<Post> findVisibleActive(
            PostIntent intent,
            UUID viewerProfileId,
            List<UUID> excludedAuthorProfileIds,
            boolean excludeBlockedAuthors,
            List<UUID> authorProfileIds,
            boolean filterAuthors,
            Pageable pageable);

    List<Post> findActiveByAuthorId(UUID authorId);

    List<Post> findActiveByPetId(UUID petId);

    List<Post> findByPetId(UUID petId);

    Page<Post> findByPetIdAndActiveTrue(UUID petId, Pageable pageable);

    Post save(Post post);

    void delete(Post post);
}
