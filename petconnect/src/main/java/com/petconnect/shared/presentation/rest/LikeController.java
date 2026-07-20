package com.petconnect.shared.presentation.rest;

import com.petconnect.shared.domain.Like;
import com.petconnect.shared.domain.Like.TargetType;
import com.petconnect.shared.domain.repositories.LikeRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/likes")
public class LikeController {

    private static final Logger log = LoggerFactory.getLogger(LikeController.class);

    private final LikeRepository likeRepository;
    private final UserProfileRepository userProfileRepository;

    public LikeController(LikeRepository likeRepository, UserProfileRepository userProfileRepository) {
        this.likeRepository = likeRepository;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Dar like a un contenido
     * Acepta tanto userId (user_profiles.id) como authUserId (auth_users.id)
     */
    @PostMapping("/{targetType}/{targetId}")
    public ResponseEntity<LikeResponse> like(
            @PathVariable String targetType,
            @PathVariable UUID targetId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID authUserId) {
        log.debug("POST /api/v1/likes/{}/{} - userId: {}, authUserId: {}", targetType, targetId, userId, authUserId);

        // Validar el tipo de target
        if (!isValidTargetType(targetType)) {
            return ResponseEntity.badRequest().build();
        }

        // Determinar el userId a usar (priorizar userId directo, luego authUserId)
        UUID effectiveUserId = resolveUserId(userId, authUserId);
        if (effectiveUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        // Verificar si ya existe el like
        if (likeRepository.existsByUserIdAndTargetIdAndTargetType(effectiveUserId, targetId, targetType)) {
            return ResponseEntity.ok(new LikeResponse(true, "Already liked"));
        }

        Like like = new Like(effectiveUserId, targetId, targetType);
        likeRepository.save(like);

        return ResponseEntity.ok(new LikeResponse(true, "Liked successfully"));
    }

    /**
     * Quitar like de un contenido
     * Acepta tanto userId (user_profiles.id) como authUserId (auth_users.id)
     */
    @DeleteMapping("/{targetType}/{targetId}")
    public ResponseEntity<Void> unlike(
            @PathVariable String targetType,
            @PathVariable UUID targetId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID authUserId) {
        log.debug("DELETE /api/v1/likes/{}/{} - userId: {}, authUserId: {}", targetType, targetId, userId, authUserId);

        if (!isValidTargetType(targetType)) {
            return ResponseEntity.badRequest().build();
        }

        UUID effectiveUserId = resolveUserId(userId, authUserId);
        if (effectiveUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        likeRepository.deleteByUserIdAndTargetIdAndTargetType(effectiveUserId, targetId, targetType);

        return ResponseEntity.noContent().build();
    }

    /**
     * Verificar si un usuario ha dado like a un contenido
     * Acepta tanto userId (user_profiles.id) como authUserId (auth_users.id)
     */
    @GetMapping("/{targetType}/{targetId}/status")
    public ResponseEntity<LikeStatusResponse> getLikeStatus(
            @PathVariable String targetType,
            @PathVariable UUID targetId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID authUserId) {
        log.debug("GET /api/v1/likes/{}/{} - status for userId: {}, authUserId: {}", targetType, targetId, userId,
                authUserId);

        if (!isValidTargetType(targetType)) {
            return ResponseEntity.badRequest().build();
        }

        UUID effectiveUserId = resolveUserId(userId, authUserId);
        if (effectiveUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean liked = likeRepository.existsByUserIdAndTargetIdAndTargetType(effectiveUserId, targetId, targetType);
        long count = likeRepository.countByTargetIdAndTargetType(targetId, targetType);

        return ResponseEntity.ok(new LikeStatusResponse(liked, count));
    }

    /**
     * Obtener todos los likes de un contenido
     */
    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<List<Like>> getLikes(
            @PathVariable String targetType,
            @PathVariable UUID targetId) {
        log.debug("GET /api/v1/likes/{}/{}", targetType, targetId);

        if (!isValidTargetType(targetType)) {
            return ResponseEntity.badRequest().build();
        }

        List<Like> likes = likeRepository.findByTargetIdAndTargetType(targetId, targetType);
        return ResponseEntity.ok(likes);
    }

    /**
     * Obtener el conteo de likes de un contenido
     */
    @GetMapping("/{targetType}/{targetId}/count")
    public ResponseEntity<Long> getLikeCount(
            @PathVariable String targetType,
            @PathVariable UUID targetId) {
        log.debug("GET /api/v1/likes/{}/{} - count", targetType, targetId);

        if (!isValidTargetType(targetType)) {
            return ResponseEntity.badRequest().build();
        }

        long count = likeRepository.countByTargetIdAndTargetType(targetId, targetType);
        return ResponseEntity.ok(count);
    }

    /**
     * Obtener los likes de un usuario
     * Acepta tanto userId (user_profiles.id) como authUserId (auth_users.id)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Like>> getUserLikes(@PathVariable UUID userId) {
        log.debug("GET /api/v1/likes/user/{}", userId);
        List<Like> likes = likeRepository.findByUserId(userId);
        return ResponseEntity.ok(likes);
    }

    /**
     * Resuelve el userId efectivo a partir de userId directo o authUserId
     */
    private UUID resolveUserId(UUID userId, UUID authUserId) {
        if (userId != null) {
            return userId;
        }
        if (authUserId != null) {
            return userProfileRepository.findByAuthUserId(authUserId)
                    .map(UserProfile::getId)
                    .orElse(null);
        }
        return null;
    }

    private boolean isValidTargetType(String targetType) {
        try {
            TargetType.valueOf(targetType.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // DTOs
    public record LikeResponse(boolean liked, String message) {
    }

    public record LikeStatusResponse(boolean liked, long count) {
    }
}
