package com.petconnect.social.presentation.rest;

import com.petconnect.social.domain.Comment;
import com.petconnect.social.domain.repositories.CommentRepository;
import com.petconnect.social.domain.repositories.PostRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/social/comments")
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserProfileRepository userProfileRepository;

    @Autowired
    public CommentController(CommentRepository commentRepository,
            PostRepository postRepository,
            UserProfileRepository userProfileRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // ========== CREATE COMMENT ==========

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@RequestBody CreateCommentRequest request) {
        log.info("POST /api/v1/social/comments - userId: {}, targetId: {}, targetType: {}",
                request.userId(), request.targetId(), request.targetType());

        // Validate content
        if (request.content() == null || request.content().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.content().length() > 500) {
            return ResponseEntity.badRequest().build();
        }

        // Create comment
        Comment comment = new Comment(
                request.userId(),
                request.targetId(),
                request.targetType(),
                request.content().trim());

        Comment saved = commentRepository.save(comment);
        log.info("Comment created with id: {}", saved.getId());

        // Update comments count on target (e.g., post)
        updateCommentsCount(request.targetId(), request.targetType(), 1);

        // Get user profile for response (optional - user might not have profile yet)
        Optional<UserProfile> userProfile = userProfileRepository.findByAuthUserId(request.userId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommentResponse.fromDomain(saved, userProfile.orElse(null)));
    }

    // ========== GET COMMENTS ==========

    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable String targetType,
            @PathVariable UUID targetId) {
        log.debug("GET /api/v1/social/comments/{}/{}", targetType, targetId);

        List<Comment> comments = commentRepository
                .findByTargetIdAndTargetTypeAndActiveTrueOrderByCreatedAtAsc(targetId, targetType);

        List<CommentResponse> response = comments.stream()
                .map(comment -> {
                    Optional<UserProfile> userProfile = userProfileRepository
                            .findByAuthUserId(comment.getUserId());
                    return userProfile
                            .map(profile -> CommentResponse.fromDomain(comment, profile))
                            .orElse(null);
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ========== UPDATE COMMENT ==========

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @RequestBody UpdateCommentRequest request) {
        log.debug("PUT /api/v1/social/comments/{}", commentId);

        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Comment comment = commentOpt.get();

        // Validate user is the author
        if (!comment.getUserId().equals(request.userId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Validate content
        if (request.content() == null || request.content().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.content().length() > 500) {
            return ResponseEntity.badRequest().build();
        }

        comment.updateContent(request.content().trim());
        Comment saved = commentRepository.save(comment);

        Optional<UserProfile> userProfile = userProfileRepository.findByAuthUserId(saved.getUserId());
        return userProfile
                .map(profile -> ResponseEntity.ok(CommentResponse.fromDomain(saved, profile)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== DELETE COMMENT ==========

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            @RequestParam UUID userId) {
        log.debug("DELETE /api/v1/social/comments/{} - userId: {}", commentId, userId);

        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Comment comment = commentOpt.get();

        // Validate user is the author
        if (!comment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Soft delete
        comment.softDelete();
        commentRepository.save(comment);

        // Update comments count
        updateCommentsCount(comment.getTargetId(), comment.getTargetType(), -1);

        return ResponseEntity.noContent().build();
    }

    // ========== GET USER COMMENTS ==========

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getUserComments(@PathVariable UUID userId) {
        log.debug("GET /api/v1/social/comments/user/{}", userId);

        List<Comment> comments = commentRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);

        List<CommentResponse> response = comments.stream()
                .map(comment -> {
                    Optional<UserProfile> userProfile = userProfileRepository
                            .findByAuthUserId(comment.getUserId());
                    return userProfile
                            .map(profile -> CommentResponse.fromDomain(comment, profile))
                            .orElse(null);
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ========== HELPER METHODS ==========

    private void updateCommentsCount(UUID targetId, String targetType, int delta) {
        if ("POST".equalsIgnoreCase(targetType)) {
            postRepository.findById(targetId).ifPresent(post -> {
                if (delta > 0) {
                    post.incrementComments();
                } else {
                    post.decrementComments();
                }
                postRepository.save(post);
            });
        }
        // Future: Add support for STORY, EVENT, PHOTO, PROFILE
    }

    // ========== DTOs ==========

    public record CreateCommentRequest(UUID userId, UUID targetId, String targetType, String content) {
    }

    public record UpdateCommentRequest(UUID userId, String content) {
    }

    public record CommentResponse(
            String id,
            String userId,
            String targetId,
            String targetType,
            String content,
            String authorFirstName,
            String authorLastName,
            String authorAvatarUrl,
            String createdAt) {
        public static CommentResponse fromDomain(Comment comment, UserProfile author) {
            if (author == null) {
                return new CommentResponse(
                        comment.getId().toString(),
                        comment.getUserId().toString(),
                        comment.getTargetId().toString(),
                        comment.getTargetType(),
                        comment.getContent(),
                        "Usuario",
                        "",
                        "",
                        comment.getCreatedAt().toString());
            }
            return new CommentResponse(
                    comment.getId().toString(),
                    comment.getUserId().toString(),
                    comment.getTargetId().toString(),
                    comment.getTargetType(),
                    comment.getContent(),
                    author.getFirstName(),
                    author.getLastName(),
                    author.getAvatarUrl(),
                    comment.getCreatedAt().toString());
        }
    }
}