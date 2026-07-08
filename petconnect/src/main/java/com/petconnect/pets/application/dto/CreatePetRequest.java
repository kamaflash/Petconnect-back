package com.petconnect.pets.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePetRequest(
        @NotBlank(message = "Name is required") String name,
        @NotNull(message = "Species is required") String species,
        String breed,
        LocalDate dateOfBirth,
        String gender,
        String color,
        String microchipId) {
}