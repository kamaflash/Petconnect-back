package com.petconnect.shared.infrastructure.persistence;

import com.petconnect.shared.domain.DomainEvent;
import com.petconnect.shared.domain.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}