package com.petconnect.shared.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();

    LocalDateTime occurredOn();

    String eventName();
}