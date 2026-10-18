package com.petconnect.adoptions.presentation.rest;

import com.petconnect.adoptions.application.AdoptionRequestService;
import com.petconnect.adoptions.application.dto.AdoptionHistoryResponse;
import com.petconnect.adoptions.application.dto.AdoptionRequestResponse;
import com.petconnect.adoptions.application.dto.AdoptionRequestEventResponse;
import com.petconnect.adoptions.application.dto.CreateAdoptionRequest;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.shared.presentation.dto.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import com.petconnect.gamification.application.events.XpEvent;
import com.petconnect.gamification.domain.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/adoptions")
public class AdoptionRequestController {

    private static final Logger log = LoggerFactory.getLogger(AdoptionRequestController.class);

    private final AdoptionRequestService adoptionRequestService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public AdoptionRequestController(AdoptionRequestService adoptionRequestService) {
        this.adoptionRequestService = adoptionRequestService;
    }

    @PostMapping
    public ResponseEntity<AdoptionRequestResponse> createRequest(
            Authentication authentication,
            @RequestBody CreateAdoptionRequest request) {
        log.debug("POST /api/v1/adoptions for pet {}", request.petId());
        UUID adopterId = getUserId(authentication);
        var response = adoptionRequestService.createRequest(adopterId, request);
        publishGamification(ActionType.ADOPTION_REQUESTED, adopterId);
        return ResponseEntity.ok(response);
    }

    /** Mis solicitudes como adoptante, paginadas por fecha descendente. */
    @GetMapping("/my-requests")
    public ResponseEntity<PageResponse<AdoptionRequestResponse>> getMyRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /api/v1/adoptions/my-requests page={} size={}", page, size);
        UUID adopterId = getUserId(authentication);
        return ResponseEntity.ok(adoptionRequestService.getMyRequests(adopterId, page, size));
    }

    /** Solicitudes recibidas sobre mis mascotas, paginadas (lo actual primero). */
    @GetMapping("/my-pets-requests")
    public ResponseEntity<PageResponse<AdoptionRequestResponse>> getRequestsForMyPets(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /api/v1/adoptions/my-pets-requests page={} size={}", page, size);
        UUID ownerId = getUserId(authentication);
        return ResponseEntity.ok(adoptionRequestService.getRequestsForMyPets(ownerId, page, size));
    }

    @GetMapping("/{requestId}/timeline")
    public ResponseEntity<List<AdoptionRequestEventResponse>> getRequestTimeline(
            Authentication authentication,
            @PathVariable UUID requestId) {
        UUID actorId = getUserId(authentication);
        return ResponseEntity.ok(adoptionRequestService.getRequestTimeline(actorId, requestId));
    }

    @GetMapping("/{petId}/history")
    public ResponseEntity<AdoptionHistoryResponse> getHistory(@PathVariable UUID petId) {
        log.debug("GET /api/v1/adoptions/{}/history", petId);
        return ResponseEntity.ok(adoptionRequestService.getHistory(petId));
    }

    @PatchMapping("/{requestId}/status")
    public ResponseEntity<AdoptionRequestResponse> updateStatus(
            Authentication authentication,
            @PathVariable UUID requestId,
            @RequestParam String status) {
        log.debug("PATCH /api/v1/adoptions/{}/status -> {}", requestId, status);
        UUID actorId = getUserId(authentication);
        return ResponseEntity.ok(adoptionRequestService.updateStatus(actorId, requestId, status));
    }

    private UUID getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    // ========== GAMIFICATION ==========

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
}