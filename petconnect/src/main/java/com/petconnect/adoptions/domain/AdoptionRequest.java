package com.petconnect.adoptions.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Solicitud de adopción de una mascota. Almacena los datos del animal, del
 * dueño (owner) y del solicitante (adopter).
 */
@Entity
@Table(name = "adoption_requests")
public class AdoptionRequest extends BaseEntity {

    // === Datos del animal ===
    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "pet_name", nullable = false, length = 100)
    private String petName;

    @Column(name = "pet_image", length = 500)
    private String petImage;

    @Column(length = 50)
    private String species;

    @Column(length = 100)
    private String breed;

    // === Datos del dueño ===
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    // === Datos del solicitante ===
    @Column(name = "adopter_id", nullable = false)
    private UUID adopterId;

    @Column(name = "adopter_name", nullable = false, length = 120)
    private String adopterName;

    @Column(length = 1000)
    private String message;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AdoptionStatus status;

    protected AdoptionRequest() {
        super();
    }

    public AdoptionRequest(UUID petId, String petName, String petImage, String species, String breed,
            UUID ownerId, UUID adopterId, String adopterName, String message) {
        super();
        this.petId = petId;
        this.petName = petName;
        this.petImage = petImage;
        this.species = species;
        this.breed = breed;
        this.ownerId = ownerId;
        this.adopterId = adopterId;
        this.adopterName = adopterName;
        this.message = message;
        this.status = AdoptionStatus.PENDING;
    }

    public UUID getPetId() {
        return petId;
    }

    public String getPetName() {
        return petName;
    }

    public String getPetImage() {
        return petImage;
    }

    public String getSpecies() {
        return species;
    }

    public String getBreed() {
        return breed;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getAdopterId() {
        return adopterId;
    }

    public String getAdopterName() {
        return adopterName;
    }

    public String getMessage() {
        return message;
    }

    public AdoptionStatus getStatus() {
        return status;
    }

    public void setStatus(AdoptionStatus status) {
        this.status = status;
    }
}