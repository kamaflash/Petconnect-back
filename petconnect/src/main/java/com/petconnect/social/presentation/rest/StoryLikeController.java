package com.petconnect.social.presentation.rest;

import com.petconnect.shared.domain.Like;
import com.petconnect.shared.domain.Like.TargetType;
import com.petconnect.shared.domain.repositories.LikeRepository;
import com.petconnect.social.domain.Story;
import com.petconnect.social.domain.repositories.StoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social")
public class StoryLikeController {

    private static final Logger log = LoggerFactory.getLogger(StoryLikeController.class);

    private final StoryRepository storyRepository;
    private final LikeRepository likeRepository;

    public StoryLikeController(StoryRepository storyRepository, LikeRepository likeRepository) {
        this.storyRepository = storyRepository;
        this.likeRepository = likeRepository;
    }

    /**
     * Dar like a una historia
     */
    @PostMapping("/stories/{storyId}/like")
    public ResponseEntity<LikeResponse> likeStory(
            @PathVariable UUID storyId,
            @RequestParam UUID userId) {
        log.debug("POST /api/v1/social/stories/{}/like - userId: {}", storyId, userId);

        // Check if already liked
        if (likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, storyId, TargetType.STORY.getValue())) {
            return ResponseEntity.ok(new LikeResponse(true, "Already liked"));
        }

        // Create the like
        Like like = new Like(userId, storyId, TargetType.STORY.getValue());
        likeRepository.save(like);

        return ResponseEntity.ok(new LikeResponse(true, "Liked successfully"));
    }

    /**
     * Quitar like de una historia
     */
    @DeleteMapping("/stories/{storyId}/like")
    public ResponseEntity<Void> unlikeStory(
            @PathVariable UUID storyId,
            @RequestParam UUID userId) {
        log.debug("DELETE /api/v1/social/stories/{}/like - userId: {}", storyId, userId);

        likeRepository.deleteByUserIdAndTargetIdAndTargetType(userId, storyId, TargetType.STORY.getValue());

        return ResponseEntity.noContent().build();
    }

    /**
     * Verificar si un usuario ha dado like a una historia
     */
    @GetMapping("/stories/{storyId}/like/status")
    public ResponseEntity<LikeStatusResponse> getStoryLikeStatus(
            @PathVariable UUID storyId,
            @RequestParam UUID userId) {
        log.debug("GET /api/v1/social/stories/{}/like/status - userId: {}", storyId, userId);

        boolean liked = likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, storyId,
                TargetType.STORY.getValue());
        long count = likeRepository.countByTargetIdAndTargetType(storyId, TargetType.STORY.getValue());

        return ResponseEntity.ok(new LikeStatusResponse(liked, count));
    }

    /**
     * Obtener el conteo de likes de una historia
     */
    @GetMapping("/stories/{storyId}/like/count")
    public ResponseEntity<Long> getStoryLikeCount(@PathVariable UUID storyId) {
        log.debug("GET /api/v1/social/stories/{}/like/count", storyId);

        long count = likeRepository.countByTargetIdAndTargetType(storyId, TargetType.STORY.getValue());
        return ResponseEntity.ok(count);
    }

    /**
     * Obtener los usuarios que dieron like a una historia
     */
    @GetMapping("/stories/{storyId}/likes")
    public ResponseEntity<List<Like>> getStoryLikes(@PathVariable UUID storyId) {
        log.debug("GET /api/v1/social/stories/{}/likes", storyId);

        List<Like> likes = likeRepository.findByTargetIdAndTargetType(storyId, TargetType.STORY.getValue());
        return ResponseEntity.ok(likes);
    }

    // DTOs
    public record LikeResponse(boolean liked, String message) {
    }

    public record LikeStatusResponse(boolean liked, long count) {
    }
}