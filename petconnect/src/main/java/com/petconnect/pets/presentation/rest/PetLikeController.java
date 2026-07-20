package com.petconnect.pets.presentation.rest;

import com.petconnect.shared.domain.Like;
import com.petconnect.shared.domain.Like.TargetType;
import com.petconnect.shared.domain.repositories.LikeRepository;
import com.petconnect.pets.domain.Pet;
import com.petconnect.pets.domain.repositories.PetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pets")
public class PetLikeController {

    private static final Logger log = LoggerFactory.getLogger(PetLikeController.class);

    private final PetRepository petRepository;
    private final LikeRepository likeRepository;

    public PetLikeController(PetRepository petRepository, LikeRepository likeRepository) {
        this.petRepository = petRepository;
        this.likeRepository = likeRepository;
    }

    /**
     * Dar like a una mascota
     */
    @PostMapping("/{petId}/like")
    public ResponseEntity<LikeResponse> likePet(
            @PathVariable UUID petId,
            @RequestParam UUID userId) {
        log.debug("POST /api/v1/pets/{}/like - userId: {}", petId, userId);

        // Check if already liked
        if (likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, petId, TargetType.PET.getValue())) {
            return ResponseEntity.ok(new LikeResponse(true, "Already liked"));
        }

        // Create the like
        Like like = new Like(userId, petId, TargetType.PET.getValue());
        likeRepository.save(like);

        return ResponseEntity.ok(new LikeResponse(true, "Liked successfully"));
    }

    /**
     * Quitar like de una mascota
     */
    @DeleteMapping("/{petId}/like")
    public ResponseEntity<Void> unlikePet(
            @PathVariable UUID petId,
            @RequestParam UUID userId) {
        log.debug("DELETE /api/v1/pets/{}/like - userId: {}", petId, userId);

        likeRepository.deleteByUserIdAndTargetIdAndTargetType(userId, petId, TargetType.PET.getValue());

        return ResponseEntity.noContent().build();
    }

    /**
     * Verificar si un usuario ha dado like a una mascota
     */
    @GetMapping("/{petId}/like/status")
    public ResponseEntity<LikeStatusResponse> getPetLikeStatus(
            @PathVariable UUID petId,
            @RequestParam UUID userId) {
        log.debug("GET /api/v1/pets/{}/like/status - userId: {}", petId, userId);

        boolean liked = likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, petId, TargetType.PET.getValue());
        long count = likeRepository.countByTargetIdAndTargetType(petId, TargetType.PET.getValue());

        return ResponseEntity.ok(new LikeStatusResponse(liked, count));
    }

    /**
     * Obtener el conteo de likes de una mascota
     */
    @GetMapping("/{petId}/like/count")
    public ResponseEntity<Long> getPetLikeCount(@PathVariable UUID petId) {
        log.debug("GET /api/v1/pets/{}/like/count", petId);

        long count = likeRepository.countByTargetIdAndTargetType(petId, TargetType.PET.getValue());
        return ResponseEntity.ok(count);
    }

    /**
     * Obtener los usuarios que dieron like a una mascota
     */
    @GetMapping("/{petId}/likes")
    public ResponseEntity<List<Like>> getPetLikes(@PathVariable UUID petId) {
        log.debug("GET /api/v1/pets/{}/likes", petId);

        List<Like> likes = likeRepository.findByTargetIdAndTargetType(petId, TargetType.PET.getValue());
        return ResponseEntity.ok(likes);
    }

    // DTOs
    public record LikeResponse(boolean liked, String message) {
    }

    public record LikeStatusResponse(boolean liked, long count) {
    }
}