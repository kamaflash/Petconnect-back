package com.petconnect.home.application.dto;

import com.petconnect.home.domain.Adoption;
import com.petconnect.pets.application.dto.PetResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdoptionResponse(
        UUID id,
        UUID petId,
        String name,
        String breed,
        String image,
        String status,
        LocalDateTime createdAt,
        PetResponse pet) {

    public static AdoptionResponse from(Adoption adoption, PetResponse petResponse) {
        return new AdoptionResponse(
                adoption.getId(),
                adoption.getPetId(),
                adoption.getName(),
                adoption.getBreed(),
                adoption.getImage(),
                adoption.getStatus(),
                adoption.getCreatedAt(),
                petResponse);
    }
}