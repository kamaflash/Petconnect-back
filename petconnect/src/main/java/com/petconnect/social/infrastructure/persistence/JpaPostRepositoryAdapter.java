package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.Post;
import com.petconnect.social.domain.PostIntent;
import com.petconnect.social.domain.repositories.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaPostRepositoryAdapter implements PostRepository {

    private final SpringDataPostRepository springDataPostRepository;

    public JpaPostRepositoryAdapter(SpringDataPostRepository springDataPostRepository) {
        this.springDataPostRepository = springDataPostRepository;
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return springDataPostRepository.findById(id);
    }

    @Override
    public List<Post> findAll() {
        return springDataPostRepository.findAll();
    }

    @Override
    public Page<Post> findAll(Pageable pageable) {
        return springDataPostRepository.findAll(pageable);
    }

    @Override
    public Page<Post> findActivePaginated(Pageable pageable) {
        return springDataPostRepository.findByActiveTrue(pageable);
    }

    @Override
    public Page<Post> findActivePaginatedExcludingAuthors(List<UUID> excludedAuthorIds, Pageable pageable) {
        return springDataPostRepository.findByActiveTrueAndAuthorIdNotIn(excludedAuthorIds, pageable);
    }

    @Override
    public List<Post> findByAuthorId(UUID authorId) {
        return springDataPostRepository.findByAuthorId(authorId);
    }

    @Override
    public List<Post> findByAuthorIdIn(List<UUID> authorIds) {
        return springDataPostRepository.findByAuthorIdIn(authorIds);
    }

    @Override
    public Page<Post> findActiveByAuthorIdIn(List<UUID> authorIds, Pageable pageable) {
        return springDataPostRepository.findByAuthorIdInAndActiveTrue(authorIds, pageable);
    }

    @Override
    public List<Post> findByPetId(UUID petId) {
        return springDataPostRepository.findByPetId(petId);
    }

    @Override
    public Page<Post> findByPetIdAndActiveTrue(UUID petId, Pageable pageable) {
        return springDataPostRepository.findByPetIdAndActiveTrue(petId, pageable);
    }

    @Override
    public Page<Post> findActiveByIntent(PostIntent intent, Pageable pageable) {
        return springDataPostRepository.findByActiveTrueAndIntent(intent, pageable);
    }

    @Override
    public Page<Post> findActiveByIntentExcludingAuthors(
            PostIntent intent, List<UUID> excludedAuthorIds, Pageable pageable) {
        return springDataPostRepository.findByActiveTrueAndIntentAndAuthorIdNotIn(
                intent, excludedAuthorIds, pageable);
    }

    @Override
    public Page<Post> findActiveByAuthorIdInAndIntent(List<UUID> authorIds, PostIntent intent,
            Pageable pageable) {
        return springDataPostRepository.findByActiveTrueAndAuthorIdInAndIntent(authorIds, intent, pageable);
    }

    @Override
    public Page<Post> findActiveByAuthorIdInAndIntentExcludingAuthors(
            List<UUID> authorIds, PostIntent intent, List<UUID> excludedAuthorIds, Pageable pageable) {
        return springDataPostRepository.findByActiveTrueAndAuthorIdInAndIntentAndAuthorIdNotIn(
                authorIds, intent, excludedAuthorIds, pageable);
    }

    @Override
    public Page<Post> findActiveByAuthorIdInExcludingAuthors(
            List<UUID> authorIds, List<UUID> excludedAuthorIds, Pageable pageable) {
        return springDataPostRepository.findByActiveTrueAndAuthorIdInAndAuthorIdNotIn(
                authorIds, excludedAuthorIds, pageable);
    }

    @Override
    public Page<Post> findVisibleActive(
            PostIntent intent,
            UUID viewerProfileId,
            List<UUID> excludedAuthorProfileIds,
            boolean excludeBlockedAuthors,
            List<UUID> authorProfileIds,
            boolean filterAuthors,
            Pageable pageable) {
        return springDataPostRepository.findVisibleActive(
                intent,
                viewerProfileId,
                excludedAuthorProfileIds,
                excludeBlockedAuthors,
                authorProfileIds,
                filterAuthors,
                pageable);
    }

    @Override
    public List<Post> findActiveByAuthorId(UUID authorId) {
        return springDataPostRepository.findByAuthorIdAndActiveTrue(authorId);
    }

    @Override
    public List<Post> findActiveByPetId(UUID petId) {
        return springDataPostRepository.findByPetIdAndActiveTrue(petId);
    }

    @Override
    public Post save(Post post) {
        return springDataPostRepository.save(post);
    }

    @Override
    public void delete(Post post) {
        springDataPostRepository.delete(post);
    }
}
