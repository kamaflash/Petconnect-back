package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.Post;
import com.petconnect.social.domain.repositories.PostRepository;
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
    public List<Post> findByAuthorId(UUID authorId) {
        return springDataPostRepository.findByAuthorId(authorId);
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