package com.petconnect.shelter.application.dto;

import com.petconnect.shelter.domain.ShelterVisitStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ShelterVisitStatusUpdate(
        @NotNull ShelterVisitStatus status,
        @Size(max = 1000) String responseNote,
        LocalDateTime proposedVisitAt) {
}
