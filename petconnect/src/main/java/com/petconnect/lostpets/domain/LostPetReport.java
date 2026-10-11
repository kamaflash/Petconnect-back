package com.petconnect.lostpets.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Aviso de animal perdido. El {@code reporterId} es el authUserId del
 * responsable del aviso y la única persona autorizada a editarlo o cerrarlo.
 *
 * <p>La ubicación se almacena como zona aproximada ({@code lastSeenArea}) más
 * coordenadas opcionales. Nunca se persisten ni se exponen datos de contacto
 * privados: quien quiera ayudar usa la mensajería interna.</p>
 */
@Entity
@Table(name = "lost_pet_reports")
public class LostPetReport extends BaseEntity {

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    /** Mascota vinculada (opcional: también se puede reportar un animal suelto). */
    @Column(name = "pet_id")
    private UUID petId;

    @Column(name = "pet_name", nullable = false, length = 100)
    private String petName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private com.petconnect.pets.domain.Species species;

    @Column(length = 100)
    private String breed;

    @Column(length = 500)
    private String color;

    @Column(length = 1000)
    private String description;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "lost_date", nullable = false)
    private LocalDate lostDate;

    /** Zona aproximada de la desaparición (sin dirección exacta). */
    @Column(name = "last_seen_area", nullable = false, length = 255)
    private String lastSeenArea;

    private Double latitude;

    private Double longitude;

    @Column(name = "reward_amount")
    private Double rewardAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LostPetReportStatus status;

    @Column(name = "sightings_count", nullable = false)
    private int sightingsCount;

    @Column(name = "closed_at")
    private LocalDate closedAt;

    @Column(nullable = false)
    private boolean active;

    protected LostPetReport() {
        super();
    }

    public LostPetReport(UUID reporterId, UUID petId, String petName,
            com.petconnect.pets.domain.Species species, String breed, String color,
            String description, String photoUrl, LocalDate lostDate,
            String lastSeenArea, Double latitude, Double longitude, Double rewardAmount) {
        super();
        this.reporterId = reporterId;
        this.petId = petId;
        this.petName = petName;
        this.species = species;
        this.breed = breed;
        this.color = color;
        this.description = description;
        this.photoUrl = photoUrl;
        this.lostDate = lostDate;
        this.lastSeenArea = lastSeenArea;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rewardAmount = rewardAmount;
        this.status = LostPetReportStatus.LOST;
        this.sightingsCount = 0;
        this.active = true;
    }

    public void updateDetails(String petName, com.petconnect.pets.domain.Species species,
            String breed, String color, String description, String photoUrl,
            LocalDate lostDate, String lastSeenArea, Double latitude, Double longitude,
            Double rewardAmount) {
        this.petName = petName;
        this.species = species;
        this.breed = breed;
        this.color = color;
        this.description = description;
        this.photoUrl = photoUrl;
        this.lostDate = lostDate;
        this.lastSeenArea = lastSeenArea;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rewardAmount = rewardAmount;
    }

    /** Aplica una transición de estado validando que sea legal. */
    public void changeStatus(LostPetReportStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "No se puede cambiar el aviso de " + status + " a " + newStatus);
        }
        this.status = newStatus;
        if (newStatus == LostPetReportStatus.REUNITED || newStatus == LostPetReportStatus.CLOSED) {
            this.closedAt = LocalDate.now();
        }
    }

    public void incrementSightings() {
        this.sightingsCount++;
    }

    public void softDelete() {
        this.active = false;
    }

    public boolean isOwnedBy(UUID userId) {
        return reporterId.equals(userId);
    }

    public UUID getReporterId() {
        return reporterId;
    }

    public UUID getPetId() {
        return petId;
    }

    public String getPetName() {
        return petName;
    }

    public com.petconnect.pets.domain.Species getSpecies() {
        return species;
    }

    public String getBreed() {
        return breed;
    }

    public String getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public LocalDate getLostDate() {
        return lostDate;
    }

    public String getLastSeenArea() {
        return lastSeenArea;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getRewardAmount() {
        return rewardAmount;
    }

    public LostPetReportStatus getStatus() {
        return status;
    }

    public int getSightingsCount() {
        return sightingsCount;
    }

    public LocalDate getClosedAt() {
        return closedAt;
    }

    public boolean isActive() {
        return active;
    }
}
