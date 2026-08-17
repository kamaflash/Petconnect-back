package com.petconnect.adoptions.application.dto;

import com.petconnect.adoptions.domain.PetOwnershipHistory;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de cambio de propietario de una mascota.
 * Consistente con el modelo del frontend (OwnershipChangeEvent).
 * Incluye los nombres de los usuarios implicados para mostrarlos en la UI.
 */
public record OwnershipChangeEventResponse(
        UUID petId,
        UUID previousOwnerId,
        UUID newOwnerId,
        LocalDateTime changedAt,
        String reason,
        UUID adoptionRequestId,
        String previousOwnerName,
        String newOwnerName) {

    public static OwnershipChangeEventResponse from(PetOwnershipHistory history) {
        return from(history, null, null);
    }

    public static OwnershipChangeEventResponse from(
            PetOwnershipHistory history, String previousOwnerName, String newOwnerName) {
        return new OwnershipChangeEventResponse(
                history.getPetId(),
                history.getPreviousOwnerId(),
                history.getNewOwnerId(),
                history.getCreatedAt(),
                history.getReason().name(),
                history.getAdoptionRequestId(),
                previousOwnerName,
                newOwnerName);
    }
}