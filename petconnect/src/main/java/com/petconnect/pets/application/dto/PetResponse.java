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
                String color) {
}
