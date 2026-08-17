package com.petconnect.adoptions.presentation.rest;

import com.petconnect.adoptions.application.AdoptionRequestService;
import com.petconnect.adoptions.application.dto.AdoptionHistoryResponse;
import com.petconnect.adoptions.application.dto.AdoptionRequestResponse;
import com.petconnect.adoptions.application.dto.CreateAdoptionRequest;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
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

    public AdoptionRequestController(AdoptionRequestService adoptionRequestService) {
        this.adoptionRequestService = adoptionRequestService;
    }

    @PostMapping
    public ResponseEntity<AdoptionRequestResponse> createRequest(
            Authentication authentication,
            @RequestBody CreateAdoptionRequest request) {
        log.debug("POST /api/v1/adoptions for pet {}", request.petId());
        UUID adopterId = getUserId(authentication);
        return ResponseEntity.ok(adoptionRequestService.createRequest(adopterId, request));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<AdoptionRequestResponse>> getMyRequests(Authentication authentication) {
        log.debug("GET /api/v1/adoptions/my-requests");
        UUID adopterId = getUserId(authentication);
        return ResponseEntity.ok(adoptionRequestService.getMyRequests(adopterId));
    }

    @GetMapping("/my-pets-requests")
    public ResponseEntity<List<AdoptionRequestResponse>> getRequestsForMyPets(Authentication authentication) {
        log.debug("GET /api/v1/adoptions/my-pets-requests");
        UUID ownerId = getUserId(authentication);
        return ResponseEntity.ok(adoptionRequestService.getRequestsForMyPets(ownerId));
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
}