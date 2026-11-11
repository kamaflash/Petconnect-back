package com.petconnect.social.presentation.rest;

import com.petconnect.shared.domain.Like;
import com.petconnect.shared.domain.Like.TargetType;
import com.petconnect.shared.domain.repositories.LikeRepository;
import com.petconnect.shared.infrastructure.services.CloudinaryService;
import com.petconnect.social.domain.Post;
import com.petconnect.social.domain.Comment;
import com.petconnect.social.domain.PostAudience;
import com.petconnect.social.domain.PostIntent;
import com.petconnect.social.application.PostVisibilityService;
import com.petconnect.shared.infrastructure.security.CurrentUserService;
import com.petconnect.social.domain.repositories.CommentRepository;
import com.petconnect.social.domain.TrendingTopic;
import com.petconnect.social.domain.repositories.PostRepository;
import com.petconnect.social.domain.repositories.FollowRepository;
import com.petconnect.social.infrastructure.persistence.SpringDataTrendingTopicRepository;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import com.petconnect.users.application.BlockedUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import com.petconnect.gamification.application.events.XpEvent;
import com.petconnect.gamification.domain.ActionType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.petconnect.users.domain.UserProfile;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/social")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    private final PostRepository postRepository;
    private final UserProfileRepository userProfileRepository;
    private final CloudinaryService cloudinaryService;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;
    private final SpringDataTrendingTopicRepository trendingTopicRepository;
    private final CommentRepository commentRepository;
    private final CurrentUserService currentUserService;
    private final PostVisibilityService postVisibilityService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired(required = false)
    private BlockedUserService blockedUserService;

    public PostController(PostRepository postRepository,
            UserProfileRepository userProfileRepository,
            @Autowired(required = false) CloudinaryService cloudinaryService,
            LikeRepository likeRepository,
            FollowRepository followRepository,
            SpringDataTrendingTopicRepository trendingTopicRepository,
            CommentRepository commentRepository,
            CurrentUserService currentUserService,
            PostVisibilityService postVisibilityService) {
        this.postRepository = postRepository;
        this.userProfileRepository = userProfileRepository;
        this.cloudinaryService = cloudinaryService;
        this.likeRepository = likeRepository;
        this.followRepository = followRepository;
        this.trendingTopicRepository = trendingTopicRepository;
        this.commentRepository = commentRepository;
        this.currentUserService = currentUserService;
        this.postVisibilityService = postVisibilityService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getAllPosts() {
        log.debug("GET /api/v1/social/posts");
        UUID viewerId = currentUserService.getCurrentUserId().orElse(null);
        return ResponseEntity.ok(postRepository.findAll().stream()
                .filter(Post::isActive)
                .filter(post -> postVisibilityService.canView(post, viewerId))
                .toList());
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable UUID postId) {
        log.debug("GET /api/v1/social/posts/{}", postId);
        return postRepository.findById(postId)
                .filter(Post::isActive)
                .filter(post -> postVisibilityService.canView(
                        post, currentUserService.getCurrentUserId().orElse(null)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/posts/by-author/{authorId}")
    public ResponseEntity<List<Post>> getPostsByAuthor(@PathVariable UUID authorId) {
        log.debug("GET /api/v1/social/posts/by-author/{}", authorId);
        var author = userProfileRepository.findByAuthUserId(authorId)
                .or(() -> userProfileRepository.findById(authorId));
        if (author.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UUID viewerId = currentUserService.getCurrentUserId().orElse(null);
        if (!postVisibilityService.canViewProfile(author.get(), viewerId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(postRepository.findActiveByAuthorId(author.get().getId()).stream()
                .filter(post -> postVisibilityService.canView(post, viewerId))
                .toList());
    }

    @GetMapping("/posts/by-pet/{petId}")
    public ResponseEntity<List<Post>> getPostsByPet(@PathVariable UUID petId) {
        log.debug("GET /api/v1/social/posts/by-pet/{}", petId);
        UUID viewerId = currentUserService.getCurrentUserId().orElse(null);
        return ResponseEntity.ok(postRepository.findActiveByPetId(petId).stream()
                .filter(post -> postVisibilityService.canView(post, viewerId))
                .toList());
    }

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(
            @RequestPart("authorId") String authorId,
            @RequestPart("caption") String caption,
            @RequestPart(value = "tags", required = false) String tags,
            @RequestPart(value = "intent", required = false) String intent,
            @RequestPart(value = "audience", required = false) String audience,
            @RequestPart(value = "location", required = false) String location,
            @RequestPart(value = "latitude", required = false) String latitude,
            @RequestPart(value = "longitude", required = false) String longitude,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        log.info("POST /api/v1/social/posts - authorId: {}, caption: {}, tags: {}", authorId, caption, tags);

        if (authorId == null || authorId.trim().isEmpty()) {
            log.error("authorId is null or empty");
            return ResponseEntity.badRequest().build();
        }

        UUID authorAuthUserId;
        try {
            authorAuthUserId = UUID.fromString(authorId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid authorId format: {}", authorId);
            return ResponseEntity.badRequest().build();
        }
        UUID authenticatedUserId = currentUserService.getCurrentUserId().orElse(null);
        if (authenticatedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!authenticatedUserId.equals(authorAuthUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var authorProfile = userProfileRepository.findByAuthUserId(authenticatedUserId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user profile not found"));

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
            // Validate and process tags
            String processedTags = "";
            if (tags != null && !tags.trim().isEmpty()) {
                processedTags = Arrays.stream(tags.split(","))
                        .map(String::trim)
                        .filter(t -> !t.isEmpty())
                        .peek(t -> {
                            if (!t.startsWith("#") && !t.startsWith("@")) {
                                throw new IllegalArgumentException("Tag must start with # or @: " + t);
                            }
                        })
                        .collect(Collectors.joining(","));

                // Update or create TrendingTopic for each tag
                if (!processedTags.isEmpty()) {
                    String[] tagArray = processedTags.split(",");
                    for (String tag : tagArray) {
                        String trimmedTag = tag.trim();
                        if (!trimmedTag.isEmpty()) {
                            Optional<TrendingTopic> existing = trendingTopicRepository.findByName(trimmedTag);
                            if (existing.isPresent()) {
                                existing.get().incrementPostsCount();
                                trendingTopicRepository.save(existing.get());
                            } else {
                                trendingTopicRepository.save(new TrendingTopic(trimmedTag, 1));
                            }
                        }
                    }
                }
            }

            Double lat = parseCoordinate(latitude);
            Double lon = parseCoordinate(longitude);
            if ((latitude != null && !latitude.isBlank() && lat == null)
                    || (longitude != null && !longitude.isBlank() && lon == null)) {
                return ResponseEntity.badRequest().build();
            }

            Post post = new Post(authorProfile.getId(), imageUrl, caption, processedTags, null,
                    PostIntent.parse(intent), location, lat, lon);
            post.setAudience(PostAudience.parse(audience));
            Post saved = postRepository.save(post);
            log.info("Post saved with id: {}, tags: {}", saved.getId(), saved.getTags());
            publishGamification(ActionType.GENERATE_POST, authenticatedUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            log.error("Invalid tag format: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating post", e);
            throw e;
        }
    }

    /** Recibe la coordenada como texto del multipart: vacio es ausente, texto no numerico es 400. */
    private static Double parseCoordinate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<Post> updatePost(@PathVariable UUID postId, @RequestBody UpdatePostRequest request) {
        log.debug("PUT /api/v1/social/posts/{}", postId);
        return postRepository.findById(postId)
                .map(existing -> {
                    if (!isPostOwner(existing)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Post>build();
                    }
                    if (request.imageUrl() != null) {
                        existing.setImageUrl(request.imageUrl());
                    }
                    if (request.caption() != null) {
                        existing.setCaption(request.caption());
                    }
                    if (request.intent() != null) {
                        existing.setIntent(PostIntent.parse(request.intent()));
                    }
                    if (request.audience() != null) {
                        existing.setAudience(PostAudience.parse(request.audience()));
                    }
                    existing.setLocation(request.location(), request.latitude(), request.longitude());
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
                    if (!isPostOwner(post)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    post.softDelete();
                    postRepository.save(post);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<LikeResponse> likePost(
            @PathVariable UUID postId,
            @RequestParam UUID userId) {
        log.debug("POST /api/v1/social/posts/{}/like - userId: {}", postId, userId);

        // Check if already liked
        if (likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, postId, TargetType.POST.getValue())) {
            return ResponseEntity.ok(new LikeResponse(true, "Already liked"));
        }

        // Create the like
        Like like = new Like(userId, postId, TargetType.POST.getValue());
        likeRepository.save(like);

        // Update post likes count
        return postRepository.findById(postId)
                .map(post -> {
                    post.incrementLikes();
                    Post saved = postRepository.save(post);
                    publishGamification(ActionType.LIKE, userId);
                    return ResponseEntity.ok(new LikeResponse(true, "Liked successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/posts/{postId}/unlike")
    public ResponseEntity<Void> unlikePost(
            @PathVariable UUID postId,
            @RequestParam UUID userId) {
        log.debug("POST /api/v1/social/posts/{}/unlike - userId: {}", postId, userId);

        // Delete the like
        likeRepository.deleteByUserIdAndTargetIdAndTargetType(userId, postId, TargetType.POST.getValue());

        // Update post likes count
        return postRepository.findById(postId)
                .map(post -> {
                    post.decrementLikes();
                    postRepository.save(post);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== FEED - POSTS FROM FOLLOWED USERS ==========

    @GetMapping("/posts/following/{userId}")
    public ResponseEntity<Page<Post>> getPostsFromFollowing(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String intent) {
        log.debug("GET /api/v1/social/posts/following/{} - page: {}, size: {}", userId, page, size);
        UUID viewerAuthId = currentUserService.getCurrentUserId().orElse(null);
        if (viewerAuthId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!viewerAuthId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var viewerProfile = userProfileRepository.findByAuthUserId(viewerAuthId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user profile not found"));
        List<UUID> followingIds = followRepository.findByFollowerId(viewerProfile.getId()).stream()
                .map(follow -> follow.getFollowingId())
                .collect(Collectors.toCollection(java.util.ArrayList::new));
        followingIds.add(viewerProfile.getId());

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = visibleFeed(
                PostIntent.parse(intent), viewerProfile.getId(), followingIds, pageable);

        log.info("Returning page {} with {} posts for user {}", page, posts.getNumberOfElements(), userId);
        return ResponseEntity.ok(posts);
    }

    // ========== PUBLIC FEED - ALL POSTS ==========

        @GetMapping("/posts/feed")
    public ResponseEntity<Page<Post>> getPublicFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String intent) {
        log.debug("GET /api/v1/social/posts/feed - page: {}, size: {}", page, size);

        // Return paginated active posts for public feed (social-page)
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        PostIntent intentFilter = PostIntent.parse(intent);
        UUID viewerAuthId = currentUserService.getCurrentUserId().orElse(null);
        UUID viewerProfileId = viewerAuthId == null ? null
                : userProfileRepository.findByAuthUserId(viewerAuthId).map(UserProfile::getId).orElse(null);
        Page<Post> posts = visibleFeed(intentFilter, viewerProfileId, List.of(), pageable);

        log.info("Returning page {} with {} active posts (total: {})", page, posts.getNumberOfElements(), posts.getTotalElements());
        return ResponseEntity.ok(posts);
    }

    private Page<Post> visibleFeed(
            PostIntent intent, UUID viewerProfileId, List<UUID> authorProfileIds, Pageable pageable) {
        List<UUID> blockedProfileIds = viewerProfileId == null || blockedUserService == null
                ? List.of()
                : blockedUserService.relatedProfileIds(
                        userProfileRepository.findById(viewerProfileId).orElseThrow().getAuthUserId());
        boolean excludeBlocked = !blockedProfileIds.isEmpty();
        if (!excludeBlocked) {
            blockedProfileIds = List.of(new UUID(0L, 0L));
        }
        boolean filterAuthors = !authorProfileIds.isEmpty();
        if (!filterAuthors) {
            authorProfileIds = List.of(new UUID(0L, 0L));
        }
        return postRepository.findVisibleActive(
                intent,
                viewerProfileId,
                blockedProfileIds,
                excludeBlocked,
                authorProfileIds,
                filterAuthors,
                pageable);
    }

    private boolean isPostOwner(Post post) {
        UUID viewerId = currentUserService.getCurrentUserId().orElse(null);
        return viewerId != null && userProfileRepository.findById(post.getAuthorId())
                .map(author -> viewerId.equals(author.getAuthUserId()))
                .orElse(false);
    }

    // ========== SOLUCION DE PREGUNTAS ==========

    /**
     * Marca una respuesta como la solucion elegida por el autor de la pregunta.
     * Si ya habia otra, se desmarca para que solo exista una solucion activa.
     */
    @PostMapping("/posts/{postId}/solution/{commentId}")
    public ResponseEntity<Post> markSolution(
            @PathVariable UUID postId,
            @PathVariable UUID commentId) {
        log.debug("POST /api/v1/social/posts/{}/solution/{}", postId, commentId);

        UUID currentUserId = currentUserService.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Post post = postOpt.get();

        // Solo el autor de la publicacion puede elegir la solucion.
        if (!post.getAuthorId().equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // La marca de solucion solo aplica a preguntas.
        if (post.getIntentOrDefault() != PostIntent.QUESTION) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Comment comment = commentOpt.get();
        // La respuesta debe pertenecer a esta publicacion.
        if (!comment.getTargetId().equals(postId)) {
            return ResponseEntity.badRequest().build();
        }

        // Desmarca cualquier solucion previa de la misma publicacion.
        clearExistingSolution(postId);

        comment.setSolution(true);
        commentRepository.save(comment);
        post.setSolved(true);
        return ResponseEntity.ok(postRepository.save(post));
    }

    /**
     * Retira la marca de solucion, dejando la pregunta de nuevo sin resolver.
     */
    @DeleteMapping("/posts/{postId}/solution")
    public ResponseEntity<Post> clearSolution(@PathVariable UUID postId) {
        log.debug("DELETE /api/v1/social/posts/{}/solution", postId);

        UUID currentUserId = currentUserService.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return postRepository.findById(postId)
                .map(post -> {
                    if (!post.getAuthorId().equals(currentUserId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Post>build();
                    }

                    clearExistingSolution(postId);
                    post.setSolved(false);
                    return ResponseEntity.ok(postRepository.save(post));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Desmarca en bloque las respuestas marcadas como solucion de una publicacion. */
    private void clearExistingSolution(UUID postId) {
        List<Comment> currentSolutions =
                commentRepository.findByTargetIdAndTargetTypeAndSolutionTrue(postId, "POST");
        for (Comment current : currentSolutions) {
            current.setSolution(false);
            commentRepository.save(current);
        }
    }

    // ========== GAMIFICATION ==========

    /**
     * Publica un evento de XP para el motor de gamificación. Nunca debe romper
     * la acción principal aunque el módulo de gamificación falle.
     */
    private void publishGamification(ActionType actionType, UUID userId) {
        if (applicationEventPublisher == null) {
            return;
        }
        try {
            applicationEventPublisher.publishEvent(new XpEvent(userId, actionType));
        } catch (Exception e) {
            log.warn("No se pudo notificar XP de {} para el usuario {}", actionType, userId);
        }
    }

    // DTO for like response
    public record LikeResponse(boolean liked, String message) {
    }
}

record CreatePostRequest(UUID authorId, String imageUrl, String caption, String intent,
        String location, Double latitude, Double longitude) {
}

record UpdatePostRequest(String imageUrl, String caption, String intent,
        String audience, String location, Double latitude, Double longitude) {
}
