package com.petconnect.pets.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "pet_care_shares")
public class PetCareShare extends BaseEntity {

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "invited_user_id", nullable = false)
    private UUID invitedUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PetCareRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PetCareShareStatus status;

    protected PetCareShare() {
        super();
    }

    public PetCareShare(UUID petId, UUID ownerId, UUID invitedUserId, PetCareRole role) {
        super();
        this.petId = petId;
        this.ownerId = ownerId;
        this.invitedUserId = invitedUserId;
        this.role = role;
        this.status = PetCareShareStatus.PENDING;
    }

    public UUID getPetId() {
        return petId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getInvitedUserId() {
        return invitedUserId;
    }

    public PetCareRole getRole() {
        return role;
    }

    public PetCareShareStatus getStatus() {
        return status;
    }

    public void changeRole(PetCareRole role) {
        this.role = role;
    }

    public void changeStatus(PetCareShareStatus status) {
        this.status = status;
    }
}
