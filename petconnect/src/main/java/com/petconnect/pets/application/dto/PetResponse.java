package com.petconnect.pets.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PetResponse(
        UUID id,
        UUID ownerId,
        String name,
        String species,
        String breed,
        LocalDate dateOfBirth,
        String gender,
        String bio,
        String avatarUrl,
        List<String> imageUrls,
        Double weight,
        String weightUnit,
        boolean active,
        String microchipId,
        String color,

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
        String specialNeeds,

        // Seguridad y ubicación
        String lastKnownLocation,
        boolean lost,
        LocalDate lostDate) {
}