package com.petconnect.lostpets.presentation.rest;

import com.petconnect.lostpets.application.LostPetService;
import com.petconnect.lostpets.application.dto.LostPetDtos;
import com.petconnect.lostpets.domain.LostPetReportStatus;
import com.petconnect.pets.domain.Species;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * API de avisos de animales perdidos.
 *
 * <p>La lectura es pública (sin datos personales) para maximizar la difusión;
 * toda escritura exige usuario autenticado y el servicio valida en servidor
 * quién puede editar o cerrar cada aviso.</p>
 */
@RestController
@RequestMapping("/api/v1/lost-pets")
public class LostPetController {

    private static final Logger log = LoggerFactory.getLogger(LostPetController.class);

    private final LostPetService lostPetService;

    public LostPetController(LostPetService lostPetService) {
        this.lostPetService = lostPetService;
    }

    @GetMapping
    public ResponseEntity<List<LostPetDtos.LostPetReportResponse>> search(
            Authentication authentication,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) LostPetReportStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radiusKm) {
        log.debug("GET /api/v1/lost-pets species={} status={}", species, status);
        UUID viewerId = currentUserId(authentication).orElse(null);
        return ResponseEntity.ok(lostPetService.search(
                viewerId, species, status, from, to, latitude, longitude, radiusKm));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<LostPetDtos.LostPetReportResponse>> getMyReports(
            Authentication authentication) {
        return ResponseEntity.ok(lostPetService.getMyReports(requireUserId(authentication)));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<LostPetDtos.LostPetReportResponse> getById(
            Authentication authentication, @PathVariable UUID reportId) {
        UUID viewerId = currentUserId(authentication).orElse(null);
        return ResponseEntity.ok(lostPetService.getById(viewerId, reportId));
    }

    @GetMapping("/{reportId}/sightings")
    public ResponseEntity<List<LostPetDtos.LostPetSightingResponse>> getSightings(
            @PathVariable UUID reportId) {
        return ResponseEntity.ok(lostPetService.getSightings(reportId));
    }

    @PostMapping
    public ResponseEntity<LostPetDtos.LostPetReportResponse> create(
            Authentication authentication,
            @RequestBody LostPetDtos.CreateLostPetReport request) {
        UUID reporterId = requireUserId(authentication);
        log.debug("POST /api/v1/lost-pets by {}", reporterId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lostPetService.create(reporterId, request));
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<LostPetDtos.LostPetReportResponse> update(
            Authentication authentication,
            @PathVariable UUID reportId,
            @RequestBody LostPetDtos.UpdateLostPetReport request) {
        return ResponseEntity.ok(
                lostPetService.update(requireUserId(authentication), reportId, request));
    }

    @PatchMapping("/{reportId}/status")
    public ResponseEntity<LostPetDtos.LostPetReportResponse> updateStatus(
            Authentication authentication,
            @PathVariable UUID reportId,
            @RequestBody LostPetDtos.UpdateLostPetStatus request) {
        return ResponseEntity.ok(
                lostPetService.updateStatus(requireUserId(authentication), reportId, request));
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID reportId) {
        lostPetService.delete(requireUserId(authentication), reportId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reportId}/sightings")
    public ResponseEntity<LostPetDtos.LostPetSightingResponse> addSighting(
            Authentication authentication,
            @PathVariable UUID reportId,
            @RequestBody LostPetDtos.CreateLostPetSighting request) {
        UUID reporterId = requireUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lostPetService.addSighting(reporterId, reportId, request));
    }

    /** Especies disponibles para el filtro de la interfaz. */
    @GetMapping("/species")
    public ResponseEntity<List<String>> getSpecies() {
        return ResponseEntity.ok(java.util.Arrays.stream(Species.values())
                .map(Enum::name)
                .toList());
    }

    private Optional<UUID> currentUserId(Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails.getUserId());
        }
        return Optional.empty();
    }

    private UUID requireUserId(Authentication authentication) {
        return currentUserId(authentication)
                .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));
    }
}
