package com.petconnect.pets.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "pet_images")
public class PetImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int sortOrder;

    protected PetImage() {
        super();
    }

    public PetImage(Pet pet, String imageUrl, int sortOrder) {
        super();
        this.pet = pet;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public Pet getPet() {
        return pet;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}