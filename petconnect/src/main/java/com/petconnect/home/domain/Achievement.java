package com.petconnect.home.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "achievements")
public class Achievement extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "icon_class", nullable = false)
    private String icon;

    @Column(name = "current_progress", nullable = false)
    private int progress;

    @Column(name = "target_progress", nullable = false)
    private int total;

    @Column(name = "unlocked", nullable = false)
    private boolean completed;

    protected Achievement() {
        super();
    }

    public Achievement(UUID id, String name, String description, String icon, int progress, int total,
            boolean completed) {
        super(id);
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.progress = progress;
        this.total = total;
        this.completed = completed;
    }

    public Achievement(UUID id, String name, String description, String icon, int progress, int total,
            boolean completed, LocalDateTime createdAt) {
        super(id, createdAt);
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.progress = progress;
        this.total = total;
        this.completed = completed;
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

    public boolean isCompleted() {
        return completed;
    }
}
