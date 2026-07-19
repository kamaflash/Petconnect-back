package com.petconnect.pets.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    // === NUEVOS CAMPOS - Información médica y de salud ===
    @Column(length = 10)
    private String bloodType;

    @Column(length = 500)
    private String allergies;

    @Column(length = 500)
    private String medicalConditions;

    @Column(length = 100)
    private String vetName;

    @Column(length = 20)
    private String vetPhone;

    @Column(length = 255)
    private String vetAddress;

    private LocalDate lastVaccinationDate;

    private LocalDate nextVaccinationDate;

    private LocalDate lastVetVisit;

    @Column(length = 100)
    private String insuranceProvider;

    @Column(length = 100)
    private String insurancePolicyNumber;

    // === NUEVOS CAMPOS - Información adicional de la mascota ===
    @Column(length = 100)
    private String emergencyContact;

    @Column(length = 20)
    private String emergencyPhone;

    private LocalDate adoptionDate;

    @Column(length = 100)
    private String adoptionCenter;

    @Column(length = 50)
    private String registrationNumber;

    @Column(length = 50)
    private String licenseNumber;

    private LocalDate licenseExpiryDate;

    private boolean spayedNeutered;

    private LocalDate spayedNeuteredDate;

    private boolean deceased;

    private LocalDate dateDeceased;

    // === NUEVOS CAMPOS - Comportamiento y características ===
    @Column(length = 50)
    private String temperament;

    @Column(length = 20)
    private String energyLevel;

    @Column(length = 50)
    private String trainingLevel;

    @Column(length = 255)
    private String favoriteActivities;

    @Column(length = 100)
    private String favoriteFood;

    @Column(length = 500)
    private String specialNeeds;

    // === NUEVOS CAMPOS - Seguridad y ubicación ===
    @Column(length = 100)
    private String lastKnownLocation;

    private boolean lost;

    private LocalDate lostDate;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<PetImage> images = new ArrayList<>();

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

    public void addImage(String imageUrl) {
        int nextOrder = images.size();
        images.add(new PetImage(this, imageUrl, nextOrder));
        // Keep the first image as the avatar for backward compatibility
        if (avatarUrl == null) {
            avatarUrl = imageUrl;
        }
    }

    public List<String> getImageUrls() {
        return images.stream()
                .map(PetImage::getImageUrl)
                .toList();
    }

    public void clearImages() {
        images.clear();
    }

    public void deactivate() {
        this.active = false;
    }

    public void reactivate() {
        this.active = true;
    }

    // === NUEVOS MÉTODOS - Actualización de información médica ===
    public void updateMedicalInfo(String bloodType, String allergies, String medicalConditions,
            String vetName, String vetPhone, String vetAddress,
            LocalDate lastVaccinationDate, LocalDate nextVaccinationDate,
            LocalDate lastVetVisit, String insuranceProvider, String insurancePolicyNumber) {
        this.bloodType = bloodType;
        this.allergies = allergies;
        this.medicalConditions = medicalConditions;
        this.vetName = vetName;
        this.vetPhone = vetPhone;
        this.vetAddress = vetAddress;
        this.lastVaccinationDate = lastVaccinationDate;
        this.nextVaccinationDate = nextVaccinationDate;
        this.lastVetVisit = lastVetVisit;
        this.insuranceProvider = insuranceProvider;
        this.insurancePolicyNumber = insurancePolicyNumber;
    }

    // === NUEVOS MÉTODOS - Actualización de información adicional ===
    public void updateAdditionalInfo(String emergencyContact, String emergencyPhone,
            LocalDate adoptionDate, String adoptionCenter,
            String registrationNumber, String licenseNumber, LocalDate licenseExpiryDate,
            boolean spayedNeutered, LocalDate spayedNeuteredDate) {
        this.emergencyContact = emergencyContact;
        this.emergencyPhone = emergencyPhone;
        this.adoptionDate = adoptionDate;
        this.adoptionCenter = adoptionCenter;
        this.registrationNumber = registrationNumber;
        this.licenseNumber = licenseNumber;
        this.licenseExpiryDate = licenseExpiryDate;
        this.spayedNeutered = spayedNeutered;
        this.spayedNeuteredDate = spayedNeuteredDate;
    }

    // === NUEVOS MÉTODOS - Actualización de estado ===
    public void markAsDeceased(LocalDate dateDeceased) {
        this.deceased = true;
        this.dateDeceased = dateDeceased;
        this.active = false;
    }

    public void updateBehavior(String temperament, String energyLevel, String trainingLevel,
            String favoriteActivities, String favoriteFood, String specialNeeds) {
        this.temperament = temperament;
        this.energyLevel = energyLevel;
        this.trainingLevel = trainingLevel;
        this.favoriteActivities = favoriteActivities;
        this.favoriteFood = favoriteFood;
        this.specialNeeds = specialNeeds;
    }

    public void updateLocationStatus(String lastKnownLocation, boolean lost, LocalDate lostDate) {
        this.lastKnownLocation = lastKnownLocation;
        this.lost = lost;
        this.lostDate = lostDate;
    }

    // === GETTERS ===
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

    // Nuevos getters
    public String getBloodType() {
        return bloodType;
    }

    public String getAllergies() {
        return allergies;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public String getVetName() {
        return vetName;
    }

    public String getVetPhone() {
        return vetPhone;
    }

    public String getVetAddress() {
        return vetAddress;
    }

    public LocalDate getLastVaccinationDate() {
        return lastVaccinationDate;
    }

    public LocalDate getNextVaccinationDate() {
        return nextVaccinationDate;
    }

    public LocalDate getLastVetVisit() {
        return lastVetVisit;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public String getInsurancePolicyNumber() {
        return insurancePolicyNumber;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public LocalDate getAdoptionDate() {
        return adoptionDate;
    }

    public String getAdoptionCenter() {
        return adoptionCenter;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public LocalDate getLicenseExpiryDate() {
        return licenseExpiryDate;
    }

    public boolean isSpayedNeutered() {
        return spayedNeutered;
    }

    public LocalDate getSpayedNeuteredDate() {
        return spayedNeuteredDate;
    }

    public boolean isDeceased() {
        return deceased;
    }

    public LocalDate getDateDeceased() {
        return dateDeceased;
    }

    public String getTemperament() {
        return temperament;
    }

    public String getEnergyLevel() {
        return energyLevel;
    }

    public String getTrainingLevel() {
        return trainingLevel;
    }

    public String getFavoriteActivities() {
        return favoriteActivities;
    }

    public String getFavoriteFood() {
        return favoriteFood;
    }

    public String getSpecialNeeds() {
        return specialNeeds;
    }

    public String getLastKnownLocation() {
        return lastKnownLocation;
    }

    public boolean isLost() {
        return lost;
    }

    public LocalDate getLostDate() {
        return lostDate;
    }
}