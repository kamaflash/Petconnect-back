package com.petconnect.shelter.application.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShelterVisitRequestCreate(
        @NotNull UUID shelterUserId,
        @NotNull @Future LocalDateTime visitAt,
        @Min(1) @Max(20) int partySize,
        @Size(max = 1000) String message) {
}
