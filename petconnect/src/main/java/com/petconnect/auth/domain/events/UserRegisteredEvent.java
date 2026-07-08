package com.petconnect.auth.domain.events;

import com.petconnect.shared.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRegisteredEvent(UUID eventId, LocalDateTime occurredOn, String eventName, UUID userId, String email)
        implements DomainEvent {

    public UserRegisteredEvent(UUID userId, String email) {
        this(UUID.randomUUID(), LocalDateTime.now(), "auth.user.registered", userId, email);
    }

    @Override
    public UUID eventId() {
        return eventId;
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventName() {
        return eventName;
    }
}