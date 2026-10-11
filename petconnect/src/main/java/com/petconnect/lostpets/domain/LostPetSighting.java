package com.petconnect.lostpets.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Avistamiento registrado por cualquier usuario autenticado sobre un aviso
 * concreto. La ubicación es opcional y se guarda como zona aproximada para no
 * filtrar domicilios ni lugares privados.
 */
@Entity
@Table(name = "lost_pet_sightings")
public class LostPetSighting extends BaseEntity {

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    @Column(name = "sighted_at", nullable = false)
    private LocalDateTime sightedAt;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "area", length = 255)
    private String area;

    private Double latitude;

    private Double longitude;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    protected LostPetSighting() {
        super();
    }

    public LostPetSighting(UUID reportId, UUID reporterId, LocalDateTime sightedAt,
            String description, String area, Double latitude, Double longitude,
            String photoUrl) {
        super();
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.sightedAt = sightedAt;
        this.description = description;
        this.area = area;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrl = photoUrl;
    }

    public UUID getReportId() {
        return reportId;
    }

    public UUID getReporterId() {
        return reporterId;
    }

    public LocalDateTime getSightedAt() {
        return sightedAt;
    }

    public String getDescription() {
        return description;
    }

    public String getArea() {
        return area;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
