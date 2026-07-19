package com.petconnect.pets.application.commands;

import com.petconnect.pets.domain.Species;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreatePetCommand(
                UUID ownerId,
                String name,
                Species species,
                String breed,
                LocalDate dateOfBirth,
                String gender,
                String color,
                String microchipId,
                MultipartFile image,
                List<MultipartFile> images,

                // Información médica y de salud
                String bloodType,
                String allergies,
                String medicalConditions,
                String vetName,
                String vetPhone,
                String vetAddress,
                LocalDate lastVaccinationDate,
                LocalDate nextVaccinationDate,
                LocalDate lastVetVisit,
                String insuranceProvider,
                String insurancePolicyNumber,

                // Información adicional
                String emergencyContact,
                String emergencyPhone,
                LocalDate adoptionDate,
                String adoptionCenter,
                String registrationNumber,
                String licenseNumber,
                LocalDate licenseExpiryDate,
                boolean spayedNeutered,
                LocalDate spayedNeuteredDate,

                // Comportamiento
                String temperament,
                String energyLevel,
                String trainingLevel,
                String favoriteActivities,
                String favoriteFood,
                String specialNeeds) {
}