package com.petconnect.home.domain;

import java.util.UUID;

public class Achievement {
    private UUID id;
    private String name;
    private String description;
    private String icon;
    private int progress;
    private int total;
    private boolean unlocked;

    public Achievement() {
    }

    public Achievement(UUID id, String name, String description, String icon, int progress, int total,
            boolean unlocked) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.progress = progress;
        this.total = total;
        this.unlocked = unlocked;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public int getProgress() {
        return progress;
    }

    public int getTotal() {
        return total;
    }

    public boolean isUnlocked() {
        return unlocked;
    }
}