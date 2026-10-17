package com.petconnect.social.presentation.rest;

import com.petconnect.social.application.CommunityService;
import com.petconnect.social.application.dto.CommunityDtos;
import com.petconnect.shared.infrastructure.security.CurrentUserService;
import com.petconnect.shared.presentation.dto.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Círculos de comunidad.
 *
 * <p>La lectura es pública para poder descubrir círculos sin sesión. Toda
 * escritura exige usuario autenticado y el id se toma del token, nunca del
 * cuerpo de la petición, para que nadie pueda unir o sacar a otro usuario.</p>
 */
@RestController
@RequestMapping("/api/v1/communities")
public class CommunityController {

    private static final Logger log = LoggerFactory.getLogger(CommunityController.class);

    private final CommunityService communityService;
    private final CurrentUserService currentUserService;

    public CommunityController(CommunityService communityService,
            CurrentUserService currentUserService) {
        this.communityService = communityService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<CommunityDtos.CommunityResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/v1/communities q={} topic={}", q, topic);
        return ResponseEntity.ok(communityService.search(q, topic, page, size));
    }

    @GetMapping("/mine")
    public ResponseEntity<PageResponse<CommunityDtos.CommunityResponse>> myCommunities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));
        return ResponseEntity.ok(communityService.myCommunities(userId, page, size));
    }

    @GetMapping("/{communityId}")
    public ResponseEntity<CommunityDtos.CommunityResponse> getById(@PathVariable UUID communityId) {
        UUID viewerId = currentUserService.getCurrentUserId().orElse(null);
        return ResponseEntity.ok(communityService.getById(viewerId, communityId));
    }

    @PostMapping("/{communityId}/join")
    public ResponseEntity<CommunityDtos.CommunityResponse> join(@PathVariable UUID communityId) {
        UUID userId = currentUserService.getCurrentUserId().orElse(null);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(communityService.join(userId, communityId));
    }

    @PostMapping("/{communityId}/leave")
    public ResponseEntity<CommunityDtos.CommunityResponse> leave(@PathVariable UUID communityId) {
        UUID userId = currentUserService.getCurrentUserId().orElse(null);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(communityService.leave(userId, communityId));
    }

    /**
     * Alta de círculo. El id del creador se toma del token: aceptarlo en el
     * cuerpo permitiría crear círculos a nombre de otra persona.
     */
    @PostMapping
    public ResponseEntity<CommunityDtos.CommunityResponse> create(
            @RequestBody CommunityDtos.CreateCommunityRequest request) {
        UUID userId = currentUserService.getCurrentUserId().orElse(null);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("POST /api/v1/communities name={}", request.name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(communityService.create(userId, request));
    }

    /** Edición. Solo el propietario responde 200; el resto recibe 403. */
    @PutMapping("/{communityId}")
    public ResponseEntity<CommunityDtos.CommunityResponse> update(
            @PathVariable UUID communityId,
            @RequestBody CommunityDtos.UpdateCommunityRequest request) {
        UUID userId = currentUserService.getCurrentUserId().orElse(null);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("PUT /api/v1/communities/{}", communityId);
        return ResponseEntity.ok(communityService.update(userId, communityId, request));
    }

    /**
     * Baja lógica del círculo. Responde 204 sin cuerpo: el estado final ya lo
     * conocerá la UI al recibir 404 al recargar el detalle.
     */
    @DeleteMapping("/{communityId}")
    public ResponseEntity<Void> delete(@PathVariable UUID communityId) {
        UUID userId = currentUserService.getCurrentUserId().orElse(null);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("DELETE /api/v1/communities/{}", communityId);
        communityService.delete(userId, communityId);
        return ResponseEntity.noContent().build();
    }
}
