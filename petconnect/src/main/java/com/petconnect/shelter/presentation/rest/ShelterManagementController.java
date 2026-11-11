package com.petconnect.shelter.presentation.rest;

import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.shelter.application.ShelterManagementService;
import com.petconnect.shelter.application.dto.ShelterDonationLinkRequest;
import com.petconnect.shelter.application.dto.ShelterDonationLinkResponse;
import com.petconnect.shelter.application.dto.ShelterVisitRequestCreate;
import com.petconnect.shelter.application.dto.ShelterVisitRequestResponse;
import com.petconnect.shelter.application.dto.ShelterVisitStatusUpdate;
import com.petconnect.shelter.application.dto.ShelterVisitVisitorResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ShelterManagementController {

    private final ShelterManagementService shelterManagementService;

    public ShelterManagementController(ShelterManagementService shelterManagementService) {
        this.shelterManagementService = shelterManagementService;
    }

    @GetMapping("/api/v1/shelters/{shelterUserId}/donation-link")
    public ResponseEntity<ShelterDonationLinkResponse> getPublicDonationLink(@PathVariable UUID shelterUserId) {
        return ResponseEntity.ok(shelterManagementService.getPublicDonationLink(shelterUserId));
    }

    @PutMapping("/api/v1/shelter-management/donation-link")
    public ResponseEntity<ShelterDonationLinkResponse> updateDonationLink(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ShelterDonationLinkRequest request) {
        return ResponseEntity.ok(shelterManagementService.updateDonationLink(user.getUserId(), request));
    }

    @GetMapping("/api/v1/shelter-management/donation-link")
    public ResponseEntity<ShelterDonationLinkResponse> getMyDonationLink(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(shelterManagementService.getPublicDonationLink(user.getUserId()));
    }

    @PostMapping("/api/v1/shelter-visits")
    public ResponseEntity<ShelterVisitRequestResponse> requestVisit(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ShelterVisitRequestCreate request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shelterManagementService.requestVisit(user.getUserId(), request));
    }

    @GetMapping("/api/v1/shelter-management/visits")
    public ResponseEntity<List<ShelterVisitRequestResponse>> getMyVisitRequests(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(shelterManagementService.getMyVisitRequests(user.getUserId()));
    }

    @GetMapping("/api/v1/shelters/{shelterUserId}/my-visit-requests")
    public ResponseEntity<List<ShelterVisitRequestResponse>> getMyVisitorRequests(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID shelterUserId) {
        return ResponseEntity.ok(
                shelterManagementService.getMyVisitorRequests(shelterUserId, user.getUserId()));
    }

    @PatchMapping("/api/v1/shelter-visits/{requestId}/response")
    public ResponseEntity<ShelterVisitRequestResponse> respondToVisit(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID requestId,
            @Valid @RequestBody ShelterVisitVisitorResponse response) {
        return ResponseEntity.ok(shelterManagementService.respondToVisit(user.getUserId(), requestId, response));
    }

    @PatchMapping("/api/v1/shelter-management/visits/{requestId}")
    public ResponseEntity<ShelterVisitRequestResponse> updateVisitStatus(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID requestId,
            @Valid @RequestBody ShelterVisitStatusUpdate request) {
        return ResponseEntity.ok(shelterManagementService.updateVisitStatus(user.getUserId(), requestId, request));
    }
}
