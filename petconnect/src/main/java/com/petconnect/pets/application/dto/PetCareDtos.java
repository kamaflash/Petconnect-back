package com.petconnect.pets.application.dto;

import com.petconnect.pets.domain.PetCareActivityType;
import com.petconnect.pets.domain.PetCareRole;
import com.petconnect.pets.domain.PetCareShareStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public final class PetCareDtos {
    private PetCareDtos() {
    }

    public record PassportSettings(
            boolean enabled,
            String token,
            boolean shareMedical,
            boolean shareEmergencyContact,
            boolean shareVet) {
    }

    public record CareSummary(
            UUID petId,
            String petName,
            String species,
            String avatarUrl,
            boolean owner,
            PetCareRole role) {
    }

    public record PublicPassport(
            String petName,
            String species,
            String breed,
            String avatarUrl,
            String bloodType,
            String allergies,
            String medicalConditions,
            String emergencyContact,
            String emergencyPhone,
            String vetName,
            String vetPhone,
            String vetAddress) {
    }

    public record PassportUpdate(
            boolean enabled,
            boolean shareMedical,
            boolean shareEmergencyContact,
            boolean shareVet) {
    }

    public record ShareRequest(UUID userId, PetCareRole role) {
    }

    public record ShareResponse(
            UUID id,
            UUID petId,
            String petName,
            UUID ownerId,
            String ownerName,
            UUID userId,
            String userName,
            PetCareRole role,
            PetCareShareStatus status,
            LocalDateTime createdAt) {
    }

    public record ShareStatusUpdate(PetCareShareStatus status) {
    }

    public record ShareRoleUpdate(PetCareRole role) {
    }

    public record ActivityRequest(PetCareActivityType type, String notes, LocalDateTime occurredAt) {
    }

    public record ActivityResponse(
            UUID id,
            UUID petId,
            UUID actorId,
            String actorName,
            PetCareActivityType type,
            String notes,
            LocalDateTime occurredAt) {
    }
}
