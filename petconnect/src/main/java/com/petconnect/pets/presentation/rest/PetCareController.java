package com.petconnect.pets.presentation.rest;

import com.petconnect.pets.application.PetCareService;
import com.petconnect.pets.application.dto.PetCareDtos;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PetCareController {

    private final PetCareService petCareService;

    public PetCareController(PetCareService petCareService) {
        this.petCareService = petCareService;
    }

    @GetMapping("/pets/{petId}/care-summary")
    public ResponseEntity<PetCareDtos.CareSummary> getCareSummary(
            Authentication authentication, @PathVariable UUID petId) {
        return ResponseEntity.ok(petCareService.getCareSummary(userId(authentication), petId));
    }

    @GetMapping("/pets/{petId}/passport")
    public ResponseEntity<PetCareDtos.PassportSettings> getPassportSettings(
            Authentication authentication, @PathVariable UUID petId) {
        return ResponseEntity.ok(petCareService.getPassportSettings(userId(authentication), petId));
    }

    @PutMapping("/pets/{petId}/passport")
    public ResponseEntity<PetCareDtos.PassportSettings> configurePassport(
            Authentication authentication,
            @PathVariable UUID petId,
            @Valid @RequestBody PetCareDtos.PassportUpdate request) {
        return ResponseEntity.ok(petCareService.configurePassport(userId(authentication), petId, request));
    }

    @GetMapping("/pet-passports/{token}")
    public ResponseEntity<PetCareDtos.PublicPassport> getPublicPassport(@PathVariable String token) {
        return ResponseEntity.ok(petCareService.getPublicPassport(token));
    }

    @GetMapping("/pets/{petId}/care-shares")
    public ResponseEntity<List<PetCareDtos.ShareResponse>> getCareShares(
            Authentication authentication, @PathVariable UUID petId) {
        return ResponseEntity.ok(petCareService.getShares(userId(authentication), petId));
    }

    @PostMapping("/pets/{petId}/care-shares")
    public ResponseEntity<PetCareDtos.ShareResponse> inviteCaregiver(
            Authentication authentication,
            @PathVariable UUID petId,
            @Valid @RequestBody PetCareDtos.ShareRequest request) {
        return ResponseEntity.ok(petCareService.invite(userId(authentication), petId, request));
    }

    @PatchMapping("/pets/{petId}/care-shares/{shareId}/role")
    public ResponseEntity<PetCareDtos.ShareResponse> changeCareRole(
            Authentication authentication,
            @PathVariable UUID petId,
            @PathVariable UUID shareId,
            @Valid @RequestBody PetCareDtos.ShareRoleUpdate request) {
        return ResponseEntity.ok(
                petCareService.changeRole(userId(authentication), petId, shareId, request));
    }

    @DeleteMapping("/pets/{petId}/care-shares/{shareId}")
    public ResponseEntity<Void> revokeCareShare(
            Authentication authentication, @PathVariable UUID petId, @PathVariable UUID shareId) {
        petCareService.revokeShare(userId(authentication), petId, shareId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pet-care/invitations")
    public ResponseEntity<List<PetCareDtos.ShareResponse>> getMyInvitations(Authentication authentication) {
        return ResponseEntity.ok(petCareService.getInvitations(userId(authentication)));
    }

    @PatchMapping("/pet-care/invitations/{shareId}")
    public ResponseEntity<PetCareDtos.ShareResponse> respondToInvitation(
            Authentication authentication,
            @PathVariable UUID shareId,
            @Valid @RequestBody PetCareDtos.ShareStatusUpdate request) {
        return ResponseEntity.ok(
                petCareService.respondToInvitation(userId(authentication), shareId, request));
    }

    @DeleteMapping("/pet-care/invitations/{shareId}")
    public ResponseEntity<Void> leaveCareShare(Authentication authentication, @PathVariable UUID shareId) {
        petCareService.leaveShare(userId(authentication), shareId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pets/{petId}/care-activities")
    public ResponseEntity<List<PetCareDtos.ActivityResponse>> getActivities(
            Authentication authentication, @PathVariable UUID petId) {
        return ResponseEntity.ok(petCareService.getActivities(userId(authentication), petId));
    }

    @PostMapping("/pets/{petId}/care-activities")
    public ResponseEntity<PetCareDtos.ActivityResponse> addActivity(
            Authentication authentication,
            @PathVariable UUID petId,
            @Valid @RequestBody PetCareDtos.ActivityRequest request) {
        return ResponseEntity.ok(petCareService.addActivity(userId(authentication), petId, request));
    }

    private UUID userId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }
        throw new IllegalStateException("User not authenticated");
    }
}
