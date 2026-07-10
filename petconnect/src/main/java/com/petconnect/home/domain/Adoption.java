package com.petconnect.home.domain;

import java.util.UUID;

public class Adoption {
    private UUID id;
    private String name;
    private String breed;
    private String image;

    public Adoption() {
    }

    public Adoption(UUID id, String name, String breed, String image) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.image = image;
    }

    public UUID getId() {
        return id;
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
}