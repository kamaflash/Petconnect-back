package com.petconnect.adoptions.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Un período de propiedad de una mascota: un propietario dueño de la
 * mascota durante un intervalo de tiempo [fromDate, toDate).
 * <p>
 * Cuando {@code toDate} es {@code null} el propietario sigue siendo el dueño
 * actual de la mascota.
 */
public record PetOwnerPeriod(
        UUID ownerId,
        String ownerName,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        String reason,
        UUID adoptionRequestId) {

    public static PetOwnerPeriod of(UUID ownerId, String ownerName, LocalDateTime fromDate,
                                   LocalDateTime toDate, String reason, UUID adoptionRequestId) {
        return new PetOwnerPeriod(ownerId, ownerName, fromDate, toDate, reason, adoptionRequestId);
    }
}
