package com.petconnect.social.presentation.rest;

import com.petconnect.shared.infrastructure.services.CloudinaryService;
import com.petconnect.social.domain.Post;
import com.petconnect.social.domain.repositories.PostRepository;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.petconnect.users.domain.UserProfile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    private final PostRepository postRepository;
    private final UserProfileRepository userProfileRepository;
    private final CloudinaryService cloudinaryService;

    public PostController(PostRepository postRepository,
            UserProfileRepository userProfileRepository,
            @Autowired(required = false) CloudinaryService cloudinaryService) {
        this.postRepository = postRepository;
        this.userProfileRepository = userProfileRepository;
        this.cloudinaryService = cloudinaryService;
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

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(
            @RequestPart("authorId") String authorId,
            @RequestPart("caption") String caption,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        log.info("POST /api/v1/social/posts - authorId: {}, caption: {}", authorId, caption);

        if (authorId == null || authorId.trim().isEmpty()) {
            log.error("authorId is null or empty");
            return ResponseEntity.badRequest().build();
        }

        UUID authorUuid;
        try {
            authorUuid = UUID.fromString(authorId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid authorId format: {}", authorId);
            return ResponseEntity.badRequest().build();
        }

        // Validate that the author exists
        Optional<UserProfile> userProfile = userProfileRepository.findByAuthUserId(authorUuid);
        if (userProfile.isEmpty()) {
            log.error("Author not found with id: {}", authorUuid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header("error", "Author not found")
                    .build();
        }

        String imageUrl = "https://placehold.co/600x400?text=Post";
        if (image != null && !image.isEmpty() && cloudinaryService != null) {
            try {
                log.info("Uploading image to Cloudinary, size: {}", image.getSize());
                imageUrl = cloudinaryService.uploadImage(image);
                log.info("Image uploaded successfully: {}", imageUrl);
            } catch (IOException e) {
                log.error("Error uploading image to Cloudinary, using placeholder", e);
            }
        }

        try {
            Post post = new Post(authorUuid, imageUrl, caption);
            Post saved = postRepository.save(post);
            log.info("Post saved with id: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error creating post", e);
            throw e;
        }
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<Post> updatePost(@PathVariable UUID postId, @RequestBody UpdatePostRequest request) {
        log.debug("PUT /api/v1/social/posts/{}", postId);
        return postRepository.findById(postId)
                .map(existing -> {
                    if (request.imageUrl() != null) {
                        existing.setImageUrl(request.imageUrl());
                    }
                    if (request.caption() != null) {
                        existing.setCaption(request.caption());
                    }
                    Post saved = postRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        log.debug("DELETE /api/v1/social/posts/{}", postId);
        return postRepository.findById(postId)
                .map(post -> {
                    post.softDelete();
                    postRepository.save(post);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Post> likePost(@PathVariable UUID postId) {
        log.debug("POST /api/v1/social/posts/{}/like", postId);
        return postRepository.findById(postId)
                .map(post -> {
                    post.incrementLikes();
                    Post saved = postRepository.save(post);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/posts/{postId}/unlike")
    public ResponseEntity<Post> unlikePost(@PathVariable UUID postId) {
        log.debug("POST /api/v1/social/posts/{}/unlike", postId);
        return postRepository.findById(postId)
                .map(post -> {
                    post.decrementLikes();
                    Post saved = postRepository.save(post);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

record CreatePostRequest(UUID authorId, String imageUrl, String caption) {
}

record UpdatePostRequest(String imageUrl, String caption) {
}