package com.petconnect.lostpets.application.dto;

import com.petconnect.lostpets.domain.LostPetReportStatus;
import com.petconnect.pets.domain.Species;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTOs del módulo de animales perdidos.
 *
 * <p>Ninguna respuesta incluye teléfono, domicilio ni correo del responsable:
 * el contacto se realiza por la mensajería interna. La ubicación se expone
 * como zona aproximada ({@code lastSeenArea}) y coordenadas opcionales.</p>
 */
public final class LostPetDtos {

    private LostPetDtos() {
    }

    // ============ SOLICITUDES ============

    public record CreateLostPetReport(
            UUID petId,
            String petName,
            Species species,
            String breed,
            String color,
            String description,
            String photoUrl,
            LocalDate lostDate,
            String lastSeenArea,
            Double latitude,
            Double longitude,
            Double rewardAmount) {
    }

    public record UpdateLostPetReport(
            String petName,
            Species species,
            String breed,
            String color,
            String description,
            String photoUrl,
            LocalDate lostDate,
            String lastSeenArea,
            Double latitude,
            Double longitude,
            Double rewardAmount) {
    }

    public record UpdateLostPetStatus(LostPetReportStatus status) {
    }

    public record CreateLostPetSighting(
            LocalDateTime sightedAt,
            String description,
            String area,
            Double latitude,
            Double longitude,
            String photoUrl) {
    }

    // ============ RESPUESTAS ============

    public record LostPetReportResponse(
            UUID id,
            UUID reporterId,
            String reporterName,
            UUID petId,
            String petName,
            String species,
            String breed,
            String color,
            String description,
            String photoUrl,
            LocalDate lostDate,
            String lastSeenArea,
            Double latitude,
            Double longitude,
            Double rewardAmount,
            LostPetReportStatus status,
            int sightingsCount,
            LocalDate closedAt,
            boolean owner,
            Double distanceKm,
            LocalDateTime createdAt) {
    }

    public record LostPetSightingResponse(
            UUID id,
            UUID reportId,
            UUID reporterId,
            String reporterName,
            LocalDateTime sightedAt,
            String description,
            String area,
            Double latitude,
            Double longitude,
            String photoUrl) {
    }
}
