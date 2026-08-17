package com.petconnect.adoptions.application.dto;

import com.petconnect.adoptions.domain.AdoptionRequest;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdoptionRequestResponse(
        UUID id,
        UUID petId,
        String petName,
        String petImage,
        String species,
        String breed,
        UUID adopterId,
        String adopterName,
        UUID shelterId,
        String status,
        String message,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String ownerName) {

    public static AdoptionRequestResponse from(AdoptionRequest request) {
        return from(request, null);
    }

    public static AdoptionRequestResponse from(AdoptionRequest request, String ownerName) {
        return new AdoptionRequestResponse(
                request.getId(),
                request.getPetId(),
                request.getPetName(),
                request.getPetImage(),
                request.getSpecies(),
                request.getBreed(),
                request.getAdopterId(),
                request.getAdopterName(),
                request.getOwnerId(),
                request.getStatus().name(),
                request.getMessage(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                ownerName);
    }
}