package com.petconnect.pets.application.commands;

import com.petconnect.pets.domain.Species;

import java.time.LocalDate;
import java.util.UUID;

public record CreatePetCommand(
        UUID ownerId,
        String name,
        Species species,
        String breed,
        LocalDate dateOfBirth,
        String gender,
        String color,
        String microchipId) {
}