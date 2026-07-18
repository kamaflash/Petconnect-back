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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pets")
public class PetController {

    private static final Logger log = LoggerFactory.getLogger(PetController.class);

    private final CreatePetUseCase createPetUseCase;
    private final GetPetUseCase getPetUseCase;
    private final UpdatePetUseCase updatePetUseCase;
    private final com.petconnect.pets.domain.repositories.PetRepository petRepository;

    public PetController(
            CreatePetUseCase createPetUseCase,
            GetPetUseCase getPetUseCase,
            UpdatePetUseCase updatePetUseCase,
            com.petconnect.pets.domain.repositories.PetRepository petRepository) {
        this.createPetUseCase = createPetUseCase;
        this.getPetUseCase = getPetUseCase;
        this.updatePetUseCase = updatePetUseCase;
        this.petRepository = petRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetResponse> createPet(
            Authentication authentication,
            @RequestParam("name") String name,
            @RequestParam("species") String species,
            @RequestParam(value = "breed", required = false) String breed,
            @RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "microchipId", required = false) String microchipId,
            @RequestParam(value = "ownerId", required = false) String ownerId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        // Use ownerId from request if provided, otherwise get from authentication
        UUID userId = ownerId != null ? UUID.fromString(ownerId) : getUserId(authentication);
        log.debug("POST /api/v1/pets - userId={}, name={}", userId, name);

        var command = new CreatePetCommand(
                userId,
                name,
                Species.valueOf(species.toUpperCase()),
                breed,
                dateOfBirth != null ? java.time.LocalDate.parse(dateOfBirth) : null,
                gender,
                color,
                microchipId,
                image,
                images);

        var response = createPetUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{petId}")
    public ResponseEntity<PetResponse> getPet(@PathVariable UUID petId) {
        log.debug("GET /api/v1/pets/{}", petId);
        var response = getPetUseCase.execute(petId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-owner/{ownerId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PetResponse>> getPetsByOwner(@PathVariable UUID ownerId) {
        log.debug("GET /api/v1/pets/by-owner/{}", ownerId);
        var pets = petRepository.findByOwnerId(ownerId);
        var responses = pets.stream()
                .map(pet -> new PetResponse(
                        pet.getId(),
                        pet.getOwnerId(),
                        pet.getName(),
                        pet.getSpecies().name(),
                        pet.getBreed(),
                        pet.getDateOfBirth(),
                        pet.getGender(),
                        pet.getBio(),
                        pet.getAvatarUrl(),
                        pet.getImageUrls(),
                        pet.getWeight(),
                        pet.getWeightUnit(),
                        pet.isActive(),
                        pet.getMicrochipId(),
                        pet.getColor()))
                .toList();
        return ResponseEntity.ok(responses);
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