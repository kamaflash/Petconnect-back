package com.petconnect.shelter.application.dto;

import com.petconnect.shelter.domain.ShelterVisitStatus;
import jakarta.validation.constraints.NotNull;

public record ShelterVisitVisitorResponse(@NotNull ShelterVisitStatus status) {
}
