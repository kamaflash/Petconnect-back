package com.petconnect.adoptions.application.dto;

import java.util.UUID;

public record CreateAdoptionRequest(
        UUID petId,
        String message) {
}