package com.petconnect.pets.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pet_care_activities")
public class PetCareActivity extends BaseEntity {

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 30)
    private PetCareActivityType activityType;

    @Column(length = 500)
    private String notes;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected PetCareActivity() {
        super();
    }

    public PetCareActivity(UUID petId, UUID actorId, PetCareActivityType activityType,
            String notes, LocalDateTime occurredAt) {
        super();
        this.petId = petId;
        this.actorId = actorId;
        this.activityType = activityType;
        this.notes = notes;
        this.occurredAt = occurredAt;
    }

    public UUID getPetId() {
        return petId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public PetCareActivityType getActivityType() {
        return activityType;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
