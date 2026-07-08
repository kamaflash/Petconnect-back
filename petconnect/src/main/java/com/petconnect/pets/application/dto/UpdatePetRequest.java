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
        String weightUnit) {
}