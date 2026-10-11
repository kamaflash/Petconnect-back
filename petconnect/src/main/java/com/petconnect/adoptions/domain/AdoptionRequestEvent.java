package com.petconnect.adoptions.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "adoption_request_events")
public class AdoptionRequestEvent extends BaseEntity {

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AdoptionStatus status;

    @Column(length = 500)
    private String note;

    protected AdoptionRequestEvent() {
        super();
    }

    public AdoptionRequestEvent(UUID requestId, UUID actorId, AdoptionStatus status, String note) {
        super();
        this.requestId = requestId;
        this.actorId = actorId;
        this.status = status;
        this.note = note;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public AdoptionStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }
}
