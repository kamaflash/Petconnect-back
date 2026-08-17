package com.petconnect.adoptions.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Historial de adopción completo de una mascota: todas las solicitudes de
 * adopción a lo largo del tiempo, los cambios de propietario registrados y
 * la cronología de propietarios que ha tenido el animal.
 */
public record AdoptionHistoryResponse(
        UUID petId,
        List<AdoptionRequestResponse> adoptionRequests,
        List<OwnershipChangeEventResponse> ownershipChanges,
        List<PetOwnerPeriod> owners) {

    public static AdoptionHistoryResponse of(
            UUID petId,
            List<AdoptionRequestResponse> adoptionRequests,
            List<OwnershipChangeEventResponse> ownershipChanges,
            List<PetOwnerPeriod> owners) {
        return new AdoptionHistoryResponse(petId, adoptionRequests, ownershipChanges, owners);
    }
}