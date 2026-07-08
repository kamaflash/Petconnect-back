package com.petconnect.pets.application.dto;

import java.time.LocalDate;
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
        Double weight,
        String weightUnit,
        boolean active,
        String microchipId,
        String color) {
}