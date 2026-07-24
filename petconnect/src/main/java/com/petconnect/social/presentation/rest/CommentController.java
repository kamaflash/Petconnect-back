package com.petconnect.social.presentation.rest;

import com.petconnect.social.domain.Comment;
import com.petconnect.social.domain.Post;
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
        log.info("POST /api/v1/social/comments - userId: {}, targetId: {}, targetType: {}, parentId: {}",
                request.userId(), request.targetId(), request.targetType(), request.parentId());

        // Validate content
        if (request.content() == null || request.content().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.content().length() > 500) {
            return ResponseEntity.badRequest().build();
        }

        // Create comment (with optional parentId for replies)
        Comment comment;
        if (request.parentId() != null) {
            comment = new Comment(
                    request.userId(),
                    request.targetId(),
                    request.targetType(),
                    request.content().trim(),
                    request.parentId());
        } else {
            comment = new Comment(
                    request.userId(),
                    request.targetId(),
                    request.targetType(),
                    request.content().trim());
        }

        Comment saved = commentRepository.save(comment);
        log.info("Comment created with id: {}", saved.getId());

        // Update comments count on target (e.g., post) - only for top-level comments
        if (request.parentId() == null) {
            updateCommentsCount(request.targetId(), request.targetType(), 1);
        }

        // Get user profile for response
        Optional<UserProfile> userProfile = userProfileRepository.findByAuthUserId(request.userId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommentResponse.fromDomain(saved, userProfile.orElse(null)));
    }

    // ========== GET COMMENTS (top-level with replies) ==========

    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable String targetType,
            @PathVariable UUID targetId) {
        log.debug("GET /api/v1/social/comments/{}/{}", targetType, targetId);

        // Get all top-level comments
        List<Comment> topLevelComments = commentRepository
                .findTopLevelCommentsByTarget(targetId, targetType);

        // Build response with replies for each top-level comment
        List<CommentResponse> response = topLevelComments.stream()
                .map(comment -> {
                    Optional<UserProfile> userProfile = userProfileRepository
                            .findByAuthUserId(comment.getUserId());
                    CommentResponse cr = userProfile
                            .map(profile -> CommentResponse.fromDomain(comment, profile))
                            .orElse(null);
                    if (cr != null) {
                        // Load replies for this comment
                        List<Comment> replies = commentRepository.findRepliesByParentId(comment.getId());
                        List<CommentResponse> replyResponses = replies.stream()
                                .map(reply -> {
                                    Optional<UserProfile> replyProfile = userProfileRepository
                                            .findByAuthUserId(reply.getUserId());
                                    return replyProfile
                                            .map(profile -> CommentResponse.fromDomain(reply, profile))
                                            .orElse(null);
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList());
                        cr = cr.withReplies(replyResponses);
                    }
                    return cr;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ========== GET REPLIES BY PARENT ID ==========

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentResponse>> getReplies(@PathVariable UUID commentId) {
        log.debug("GET /api/v1/social/comments/{}/replies", commentId);

        List<Comment> replies = commentRepository.findRepliesByParentId(commentId);

        List<CommentResponse> response = replies.stream()
                .map(reply -> {
                    Optional<UserProfile> userProfile = userProfileRepository
                            .findByAuthUserId(reply.getUserId());
                    return userProfile
                            .map(profile -> CommentResponse.fromDomain(reply, profile))
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

        // Allow deletion if user is the comment author OR the post owner
        boolean isCommentAuthor = comment.getUserId().equals(userId);
        boolean isPostOwner = false;

        if ("POST".equalsIgnoreCase(comment.getTargetType())) {
            Optional<Post> postOpt = postRepository.findById(comment.getTargetId());
            if (postOpt.isPresent()) {
                isPostOwner = postOpt.get().getAuthorId().equals(userId);
            }
        }

        if (!isCommentAuthor && !isPostOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Soft delete
        comment.softDelete();
        commentRepository.save(comment);

        // Update comments count on target - only for top-level comments
        if (comment.getParentId() == null) {
            updateCommentsCount(comment.getTargetId(), comment.getTargetType(), -1);
        }

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
    }

    // ========== DTOs ==========

    public record CreateCommentRequest(UUID userId, UUID targetId, String targetType, String content, UUID parentId) {
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
            String createdAt,
            String parentId,
            List<CommentResponse> replies) {
        public static CommentResponse fromDomain(Comment comment, UserProfile author) {
            String parentIdStr = comment.getParentId() != null ? comment.getParentId().toString() : null;
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
                        comment.getCreatedAt().toString(),
                        parentIdStr,
                        List.of());
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
                    comment.getCreatedAt().toString(),
                    parentIdStr,
                    List.of());
        }

        public CommentResponse withReplies(List<CommentResponse> replies) {
            return new CommentResponse(
                    this.id, this.userId, this.targetId, this.targetType,
                    this.content, this.authorFirstName, this.authorLastName,
                    this.authorAvatarUrl, this.createdAt, this.parentId, replies);
        }
    }
}