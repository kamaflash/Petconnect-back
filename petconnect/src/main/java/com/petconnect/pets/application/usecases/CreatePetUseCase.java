package com.petconnect.pets.application.usecases;

import com.petconnect.pets.application.commands.CreatePetCommand;
import com.petconnect.pets.application.dto.PetResponse;
import com.petconnect.pets.domain.Pet;
import com.petconnect.pets.domain.events.PetCreatedEvent;
import com.petconnect.pets.domain.repositories.PetRepository;
import com.petconnect.shared.domain.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatePetUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreatePetUseCase.class);

    private final PetRepository petRepository;
    private final DomainEventPublisher eventPublisher;

    public CreatePetUseCase(PetRepository petRepository, DomainEventPublisher eventPublisher) {
        this.petRepository = petRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PetResponse execute(CreatePetCommand command) {
        log.info("Creating new pet: name={}, species={}, ownerId={}", command.name(), command.species(),
                command.ownerId());

        var pet = new Pet(command.ownerId(), command.name(), command.species());

        if (command.breed() != null) {
            pet.updateDetails(command.name(), command.breed(), command.dateOfBirth(),
                    command.gender(), null, command.color(), command.microchipId());
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
                pet.getWeight(),
                pet.getWeightUnit(),
                pet.isActive(),
                pet.getMicrochipId(),
                pet.getColor());
    }
}