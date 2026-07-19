package com.petconnect.pets.application.usecases;

import com.petconnect.pets.application.commands.CreatePetCommand;
import com.petconnect.pets.application.dto.PetResponse;
import com.petconnect.pets.domain.Pet;
import com.petconnect.pets.domain.events.PetCreatedEvent;
import com.petconnect.pets.domain.repositories.PetRepository;
import com.petconnect.shared.domain.DomainEventPublisher;
import com.petconnect.shared.infrastructure.services.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class CreatePetUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreatePetUseCase.class);

    private final PetRepository petRepository;
    private final DomainEventPublisher eventPublisher;
    private final Optional<CloudinaryService> cloudinaryService;

    @Autowired
    public CreatePetUseCase(PetRepository petRepository, DomainEventPublisher eventPublisher,
            Optional<CloudinaryService> cloudinaryService) {
        this.petRepository = petRepository;
        this.eventPublisher = eventPublisher;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public PetResponse execute(CreatePetCommand command) {
        log.info("Creating new pet: name={}, species={}, ownerId={}", command.name(), command.species(),
                command.ownerId());

        var pet = new Pet(command.ownerId(), command.name(), command.species());

        // Update basic details
        pet.updateDetails(
                command.name(),
                command.breed(),
                command.dateOfBirth(),
                command.gender(),
                null, // bio
                command.color(),
                command.microchipId());

        // Update medical info
        pet.updateMedicalInfo(
                command.bloodType(),
                command.allergies(),
                command.medicalConditions(),
                command.vetName(),
                command.vetPhone(),
                command.vetAddress(),
                command.lastVaccinationDate(),
                command.nextVaccinationDate(),
                command.lastVetVisit(),
                command.insuranceProvider(),
                command.insurancePolicyNumber());

        // Update additional info
        pet.updateAdditionalInfo(
                command.emergencyContact(),
                command.emergencyPhone(),
                command.adoptionDate(),
                command.adoptionCenter(),
                command.registrationNumber(),
                command.licenseNumber(),
                command.licenseExpiryDate(),
                command.spayedNeutered(),
                command.spayedNeuteredDate());

        // Update behavior
        pet.updateBehavior(
                command.temperament(),
                command.energyLevel(),
                command.trainingLevel(),
                command.favoriteActivities(),
                command.favoriteFood(),
                command.specialNeeds());

        // Handle image upload - single image (backward compatibility)
        if (command.image() != null && !command.image().isEmpty() && cloudinaryService.isPresent()) {
            try {
                String avatarUrl = cloudinaryService.get().uploadImage(command.image());
                pet.updateAvatar(avatarUrl);
                log.info("Image uploaded successfully for pet: {}", pet.getId());
            } catch (IOException e) {
                log.error("Failed to upload image for pet", e);
            }
        }

        // Handle multiple images
        if (command.images() != null && !command.images().isEmpty() && cloudinaryService.isPresent()) {
            for (MultipartFile image : command.images()) {
                try {
                    String imageUrl = cloudinaryService.get().uploadImage(image);
                    pet.addImage(imageUrl);
                    log.info("Additional image uploaded for pet: {}", pet.getId());
                } catch (IOException e) {
                    log.error("Failed to upload additional image for pet", e);
                }
            }
        }

        var saved = petRepository.save(pet);

        // Publish domain event
        var event = new PetCreatedEvent(saved.getId(), saved.getOwnerId(), saved.getName());
        eventPublisher.publish(event);
        log.info("Pet created successfully: petId={}, name={}", saved.getId(), saved.getName());

        return toResponse(saved);
    }

    private PetResponse toResponse(Pet pet) {
        return new PetResponse(
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
                pet.getLostDate());
    }
}