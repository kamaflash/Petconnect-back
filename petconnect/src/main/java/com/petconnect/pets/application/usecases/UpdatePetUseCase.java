package com.petconnect.pets.application.usecases;

import com.petconnect.pets.application.dto.PetResponse;
import com.petconnect.pets.application.dto.UpdatePetRequest;
import com.petconnect.pets.domain.exceptions.PetNotFoundException;
import com.petconnect.pets.domain.repositories.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdatePetUseCase {

    private final PetRepository petRepository;

    public UpdatePetUseCase(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional
    public PetResponse execute(UUID petId, UpdatePetRequest request) {
        var pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found with id: " + petId));

        pet.updateDetails(
                request.name(),
                request.breed(),
                request.dateOfBirth(),
                request.gender(),
                request.bio(),
                request.color(),
                request.microchipId());
        pet.updateWeight(request.weight(), request.weightUnit());

        var saved = petRepository.save(pet);

        return new PetResponse(
                saved.getId(),
                saved.getOwnerId(),
                saved.getName(),
                saved.getSpecies().name(),
                saved.getBreed(),
                saved.getDateOfBirth(),
                saved.getGender(),
                saved.getBio(),
                saved.getAvatarUrl(),
                saved.getWeight(),
                saved.getWeightUnit(),
                saved.isActive(),
                saved.getMicrochipId(),
                saved.getColor());
    }
}