package com.petconnect.adoptions.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Registro de un cambio de propietario de una mascota. Cada vez que un animal
 * cambia de dueño (p. ej. al completarse una adopción) se guarda una entrada,
 * de modo que se puede reconstruir el historial de propietarios de un animal.
 */
@Entity
@Table(name = "pet_ownership_history")
public class PetOwnershipHistory extends BaseEntity {

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "previous_owner_id", nullable = false)
    private UUID previousOwnerId;

    @Column(name = "new_owner_id", nullable = false)
    private UUID newOwnerId;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private OwnershipChangeReason reason;

    @Column(name = "adoption_request_id")
    private UUID adoptionRequestId;

    protected PetOwnershipHistory() {
        super();
    }

    public PetOwnershipHistory(UUID petId, UUID previousOwnerId, UUID newOwnerId,
                               OwnershipChangeReason reason, UUID adoptionRequestId) {
        super();
        this.petId = petId;
        this.previousOwnerId = previousOwnerId;
        this.newOwnerId = newOwnerId;
        this.reason = reason;
        this.adoptionRequestId = adoptionRequestId;
    }

    public UUID getPetId() {
        return petId;
    }

    public UUID getPreviousOwnerId() {
        return previousOwnerId;
    }

    public UUID getNewOwnerId() {
        return newOwnerId;
    }

    public OwnershipChangeReason getReason() {
        return reason;
    }

    public UUID getAdoptionRequestId() {
        return adoptionRequestId;
    }
}