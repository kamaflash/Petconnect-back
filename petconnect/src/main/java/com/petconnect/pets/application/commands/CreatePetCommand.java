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
        List<MultipartFile> images) {
}
