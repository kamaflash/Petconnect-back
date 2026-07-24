package com.petconnect.pets.application.usecases;

import com.petconnect.pets.application.dto.PetResponse;
import com.petconnect.pets.application.dto.UpdatePetRequest;
import com.petconnect.pets.domain.Pet;
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

                // Update basic details
                pet.updateDetails(
                                request.name(),
                                request.breed(),
                                request.dateOfBirth(),
                                request.gender(),
                                request.bio(),
                                request.color(),
                                request.microchipId());

                // Update weight
                pet.updateWeight(request.weight(), request.weightUnit());

                // Update medical info
                pet.updateMedicalInfo(
                                request.bloodType(),
                                request.allergies(),
                                request.medicalConditions(),
                                request.vetName(),
                                request.vetPhone(),
                                request.vetAddress(),
                                request.lastVaccinationDate(),
                                request.nextVaccinationDate(),
                                request.lastVetVisit(),
                                request.insuranceProvider(),
                                request.insurancePolicyNumber());

                // Update additional info
                pet.updateAdditionalInfo(
                                request.emergencyContact(),
                                request.emergencyPhone(),
                                request.adoptionDate(),
                                request.adoptionCenter(),
                                request.registrationNumber(),
                                request.licenseNumber(),
                                request.licenseExpiryDate(),
                                request.spayedNeutered(),
                                request.spayedNeuteredDate());

                // Update behavior
                pet.updateBehavior(
                                request.temperament(),
                                request.energyLevel(),
                                request.trainingLevel(),
                                request.favoriteActivities(),
                                request.favoriteFood(),
                                request.specialNeeds());

                var saved = petRepository.save(pet);

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
                                pet.getLostDate(),
                                pet.isAvailableForAdoption());
        }
}
