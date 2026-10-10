package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.Post;
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
    public Post save(Post post) {
        return springDataPostRepository.save(post);
    }

    @Override
    public void delete(Post post) {
        springDataPostRepository.delete(post);
    }
}
