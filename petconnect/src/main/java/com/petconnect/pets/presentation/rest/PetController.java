package com.petconnect.pets.presentation.rest;

import com.petconnect.pets.application.commands.CreatePetCommand;
import com.petconnect.pets.application.dto.CreatePetRequest;
import com.petconnect.pets.application.dto.PetResponse;
import com.petconnect.pets.application.dto.UpdatePetRequest;
import com.petconnect.pets.application.usecases.CreatePetUseCase;
import com.petconnect.pets.application.usecases.GetPetUseCase;
import com.petconnect.pets.application.usecases.UpdatePetUseCase;
import com.petconnect.pets.domain.Species;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pets")
public class PetController {

    private static final Logger log = LoggerFactory.getLogger(PetController.class);

    private final CreatePetUseCase createPetUseCase;
    private final GetPetUseCase getPetUseCase;
    private final UpdatePetUseCase updatePetUseCase;

    public PetController(
            CreatePetUseCase createPetUseCase,
            GetPetUseCase getPetUseCase,
            UpdatePetUseCase updatePetUseCase) {
        this.createPetUseCase = createPetUseCase;
        this.getPetUseCase = getPetUseCase;
        this.updatePetUseCase = updatePetUseCase;
    }

    @PostMapping
    public ResponseEntity<PetResponse> createPet(
            Authentication authentication,
            @Valid @RequestBody CreatePetRequest request) {
        var userId = getUserId(authentication);
        log.debug("POST /api/v1/pets - userId={}, name={}", userId, request.name());

        var command = new CreatePetCommand(
                userId,
                request.name(),
                Species.valueOf(request.species().toUpperCase()),
                request.breed(),
                request.dateOfBirth(),
                request.gender(),
                request.color(),
                request.microchipId());

        var response = createPetUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{petId}")
    public ResponseEntity<PetResponse> getPet(@PathVariable UUID petId) {
        log.debug("GET /api/v1/pets/{}", petId);
        var response = getPetUseCase.execute(petId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{petId}")
    public ResponseEntity<PetResponse> updatePet(
            @PathVariable UUID petId,
            @Valid @RequestBody UpdatePetRequest request) {
        log.debug("PUT /api/v1/pets/{}", petId);
        var response = updatePetUseCase.execute(petId, request);
        return ResponseEntity.ok(response);
    }

    private UUID getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }
        throw new IllegalStateException("User not authenticated");
    }
}