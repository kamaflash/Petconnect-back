package com.petconnect.home.domain;

import java.util.UUID;

public class VetService {
    private UUID id;
    private String title;
    private String description;
    private String icon;

    public VetService() {
    }

    public VetService(UUID id, String title, String description, String icon) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}