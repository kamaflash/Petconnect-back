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