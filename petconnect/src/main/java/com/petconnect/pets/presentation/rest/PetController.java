package com.petconnect.pets.presentation.rest;

import com.petconnect.pets.application.commands.CreatePetCommand;
import com.petconnect.pets.application.dto.PetResponse;
import com.petconnect.pets.application.dto.UpdatePetRequest;
import com.petconnect.pets.application.usecases.CreatePetUseCase;
import com.petconnect.pets.application.usecases.GetPetUseCase;
import com.petconnect.pets.application.usecases.UpdatePetUseCase;
import com.petconnect.pets.domain.Species;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;

    public PetController(
            CreatePetUseCase createPetUseCase,
            GetPetUseCase getPetUseCase,
            UpdatePetUseCase updatePetUseCase,
            com.petconnect.pets.domain.repositories.PetRepository petRepository,
            UserProfileRepository userProfileRepository) {
        this.createPetUseCase = createPetUseCase;
        this.getPetUseCase = getPetUseCase;
        this.updatePetUseCase = updatePetUseCase;
        this.petRepository = petRepository;
        this.userProfileRepository = userProfileRepository;
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
            @RequestParam(value = "authUserId", required = false) String authUserId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,

            // Información médica y de salud
            @RequestParam(value = "bloodType", required = false) String bloodType,
            @RequestParam(value = "allergies", required = false) String allergies,
            @RequestParam(value = "medicalConditions", required = false) String medicalConditions,
            @RequestParam(value = "vetName", required = false) String vetName,
            @RequestParam(value = "vetPhone", required = false) String vetPhone,
            @RequestParam(value = "vetAddress", required = false) String vetAddress,
            @RequestParam(value = "lastVaccinationDate", required = false) String lastVaccinationDate,
            @RequestParam(value = "nextVaccinationDate", required = false) String nextVaccinationDate,
            @RequestParam(value = "lastVetVisit", required = false) String lastVetVisit,
            @RequestParam(value = "insuranceProvider", required = false) String insuranceProvider,
            @RequestParam(value = "insurancePolicyNumber", required = false) String insurancePolicyNumber,

            // Información adicional
            @RequestParam(value = "emergencyContact", required = false) String emergencyContact,
            @RequestParam(value = "emergencyPhone", required = false) String emergencyPhone,
            @RequestParam(value = "adoptionDate", required = false) String adoptionDate,
            @RequestParam(value = "adoptionCenter", required = false) String adoptionCenter,
            @RequestParam(value = "registrationNumber", required = false) String registrationNumber,
            @RequestParam(value = "licenseNumber", required = false) String licenseNumber,
            @RequestParam(value = "licenseExpiryDate", required = false) String licenseExpiryDate,
            @RequestParam(value = "spayedNeutered", required = false) Boolean spayedNeutered,
            @RequestParam(value = "spayedNeuteredDate", required = false) String spayedNeuteredDate,

            // Comportamiento
            @RequestParam(value = "temperament", required = false) String temperament,
            @RequestParam(value = "energyLevel", required = false) String energyLevel,
            @RequestParam(value = "trainingLevel", required = false) String trainingLevel,
            @RequestParam(value = "favoriteActivities", required = false) String favoriteActivities,
            @RequestParam(value = "favoriteFood", required = false) String favoriteFood,
            @RequestParam(value = "specialNeeds", required = false) String specialNeeds) {
        // Resolve ownerId: priorizar ownerId directo, luego authUserId, finalmente
        // autenticación
        UUID userId = resolveOwnerId(ownerId, authUserId, authentication);
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
                images,
                bloodType,
                allergies,
                medicalConditions,
                vetName,
                vetPhone,
                vetAddress,
                lastVaccinationDate != null ? java.time.LocalDate.parse(lastVaccinationDate) : null,
                nextVaccinationDate != null ? java.time.LocalDate.parse(nextVaccinationDate) : null,
                lastVetVisit != null ? java.time.LocalDate.parse(lastVetVisit) : null,
                insuranceProvider,
                insurancePolicyNumber,
                emergencyContact,
                emergencyPhone,
                adoptionDate != null ? java.time.LocalDate.parse(adoptionDate) : null,
                adoptionCenter,
                registrationNumber,
                licenseNumber,
                licenseExpiryDate != null ? java.time.LocalDate.parse(licenseExpiryDate) : null,
                spayedNeutered != null ? spayedNeutered : false,
                spayedNeuteredDate != null ? java.time.LocalDate.parse(spayedNeuteredDate) : null,
                temperament,
                energyLevel,
                trainingLevel,
                favoriteActivities,
                favoriteFood,
                specialNeeds);

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
                        pet.getColor(),
                        pet.getBloodType(),
                        pet.getAllergies(),
                        pet.getMedicalConditions(),
                        pet.getVetName(),
                        pet.getVetPhone(),
                        pet.getVetAddress(),
                        pet.getLastVaccinationDate(),
                        pet.getNextVaccinationDate(),
                        pet.getLastVetVisit(),
                        pet.getInsuranceProvider(),
                        pet.getInsurancePolicyNumber(),
                        pet.getEmergencyContact(),
                        pet.getEmergencyPhone(),
                        pet.getAdoptionDate(),
                        pet.getAdoptionCenter(),
                        pet.getRegistrationNumber(),
                        pet.getLicenseNumber(),
                        pet.getLicenseExpiryDate(),
                        pet.isSpayedNeutered(),
                        pet.getSpayedNeuteredDate(),
                        pet.getTemperament(),
                        pet.getEnergyLevel(),
                        pet.getTrainingLevel(),
                        pet.getFavoriteActivities(),
                        pet.getFavoriteFood(),
                        pet.getSpecialNeeds(),
                        pet.getLastKnownLocation(),
                        pet.isLost(),
                        pet.getLostDate()))
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

    /**
     * Resuelve el ownerId a partir de ownerId directo, authUserId o autenticación
     */
    private UUID resolveOwnerId(String ownerId, String authUserId, Authentication authentication) {
        if (ownerId != null && !ownerId.isEmpty()) {
            return UUID.fromString(ownerId);
        }
        if (authUserId != null && !authUserId.isEmpty()) {
            return userProfileRepository.findByAuthUserId(UUID.fromString(authUserId))
                    .map(UserProfile::getId)
                    .orElse(null);
        }
        return getUserId(authentication);
    }
}
