package com.petconnect.pets.domain.events;

import com.petconnect.shared.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record PetCreatedEvent(UUID eventId, LocalDateTime occurredOn, String eventName, UUID petId, UUID ownerId,
        String petName) implements DomainEvent {

    public PetCreatedEvent(UUID petId, UUID ownerId, String petName) {
        this(UUID.randomUUID(), LocalDateTime.now(), "pet.created", petId, ownerId, petName);
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