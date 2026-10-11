package com.petconnect.adoptions.application.dto;

import com.petconnect.adoptions.domain.AdoptionRequestEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdoptionRequestEventResponse(
        UUID id,
        UUID requestId,
        UUID actorId,
        String actorName,
        String status,
        String note,
        LocalDateTime createdAt) {

    public static AdoptionRequestEventResponse from(AdoptionRequestEvent event, String actorName) {
        return new AdoptionRequestEventResponse(
                event.getId(),
                event.getRequestId(),
                event.getActorId(),
                actorName,
                event.getStatus().name(),
                event.getNote(),
                event.getCreatedAt());
    }
}
