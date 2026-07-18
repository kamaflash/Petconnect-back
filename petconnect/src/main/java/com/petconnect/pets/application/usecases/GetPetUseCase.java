package com.petconnect.pets.application.usecases;

import com.petconnect.pets.application.dto.PetResponse;
import com.petconnect.pets.domain.exceptions.PetNotFoundException;
import com.petconnect.pets.domain.repositories.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetPetUseCase {

    private final PetRepository petRepository;

    public GetPetUseCase(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    public PetResponse execute(UUID petId) {
        var pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found with id: " + petId));

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
                pet.getColor());
    }
}