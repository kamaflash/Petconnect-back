package com.petconnect.social.presentation.rest;

import com.petconnect.social.domain.Post;
import com.petconnect.social.domain.repositories.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getAllPosts() {
        log.debug("GET /api/v1/social/posts");
        return ResponseEntity.ok(postRepository.findAll());
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable UUID postId) {
        log.debug("GET /api/v1/social/posts/{}", postId);
        return postRepository.findById(postId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/posts/by-author/{authorId}")
    public ResponseEntity<List<Post>> getPostsByAuthor(@PathVariable UUID authorId) {
        log.debug("GET /api/v1/social/posts/by-author/{}", authorId);
        return ResponseEntity.ok(postRepository.findByAuthorId(authorId));
    }
}