package com.petconnect.pets.application.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdatePetRequest(
                @NotBlank(message = "Name is required") String name,
                String breed,
                LocalDate dateOfBirth,
                String gender,
                String bio,
                String color,
                String microchipId,
                Double weight,
                String weightUnit,

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