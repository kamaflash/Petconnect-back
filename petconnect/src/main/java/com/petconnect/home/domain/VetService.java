package com.petconnect.home.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vet_services")
public class VetService extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "icon_class", nullable = false)
    private String icon;

    @Column(nullable = false)
    private boolean active;

    protected VetService() {
        super();
    }

    public VetService(UUID id, String title, String description, String icon) {
        super(id);
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.active = true;
    }

    public VetService(UUID id, String title, String description, String icon, LocalDateTime createdAt) {
        super(id, createdAt);
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.active = true;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}