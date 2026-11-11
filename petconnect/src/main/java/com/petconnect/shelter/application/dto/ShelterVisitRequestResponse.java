package com.petconnect.shelter.application.dto;

import com.petconnect.shelter.domain.ShelterVisitRequest;
import com.petconnect.shelter.domain.ShelterVisitStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShelterVisitRequestResponse(
        UUID id,
        UUID shelterUserId,
        UUID visitorUserId,
        String visitorName,
        LocalDateTime visitAt,
        LocalDateTime proposedVisitAt,
        int partySize,
        String message,
        ShelterVisitStatus status,
        String responseNote,
        LocalDateTime createdAt) {

    public static ShelterVisitRequestResponse from(ShelterVisitRequest request) {
        return new ShelterVisitRequestResponse(
                request.getId(), request.getShelterUserId(), request.getVisitorUserId(), request.getVisitorName(),
                request.getVisitAt(), request.getProposedVisitAt(), request.getPartySize(), request.getMessage(), request.getStatus(),
                request.getResponseNote(), request.getCreatedAt());
    }
}
