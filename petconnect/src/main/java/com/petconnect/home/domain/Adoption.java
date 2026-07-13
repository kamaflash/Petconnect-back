package com.petconnect.home.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "adoptions")
public class Adoption extends BaseEntity {

    @Column(name = "pet_name", nullable = false)
    private String name;

    @Column(name = "species", nullable = false)
    private String breed;

    @Column(name = "image_url", nullable = false)
    private String image;

    @Column(nullable = false)
    private String status;

    protected Adoption() {
        super();
    }

    public Adoption(UUID id, String name, String breed, String image) {
        super(id);
        this.name = name;
        this.breed = breed;
        this.image = image;
        this.status = "AVAILABLE";
    }

    public Adoption(UUID id, String name, String breed, String image, String status, LocalDateTime createdAt) {
        super(id, createdAt);
        this.name = name;
        this.breed = breed;
        this.image = image;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public String getImage() {
        return image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}