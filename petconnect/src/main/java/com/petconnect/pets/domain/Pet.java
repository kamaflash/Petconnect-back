package com.petconnect.pets.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pets")
public class Pet extends BaseEntity {

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Species species;

    @Column(length = 100)
    private String breed;

    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(length = 500)
    private String bio;

    @Column(length = 255)
    private String avatarUrl;

    private Double weight;

    @Column(length = 20)
    private String weightUnit;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 100)
    private String microchipId;

    @Column(length = 100)
    private String color;

    protected Pet() {
        super();
    }

    public Pet(UUID ownerId, String name, Species species) {
        super();
        this.ownerId = ownerId;
        this.name = name;
        this.species = species;
        this.active = true;
    }

    public void updateDetails(String name, String breed, LocalDate dateOfBirth, String gender,
            String bio, String color, String microchipId) {
        this.name = name;
        this.breed = breed;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bio = bio;
        this.color = color;
        this.microchipId = microchipId;
    }

    public void updateWeight(Double weight, String weightUnit) {
        this.weight = weight;
        this.weightUnit = weightUnit;
    }

    public void updateAvatar(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void deactivate() {
        this.active = false;
    }

    public void reactivate() {
        this.active = true;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public Species getSpecies() {
        return species;
    }

    public String getBreed() {
        return breed;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Double getWeight() {
        return weight;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public boolean isActive() {
        return active;
    }

    public String getMicrochipId() {
        return microchipId;
    }

    public String getColor() {
        return color;
    }
}