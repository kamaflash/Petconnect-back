package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "community_groups")
public class CommunityGroup extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 100)
    private String category;

    @Column(nullable = false)
    private int membersCount;

    /**
     * Temática del círculo (ubicación, especie, cuidados, adopción...).
     * Nullable para no romper los círculos creados antes de esta funcionalidad.
     */
    @Column(name = "topic", length = 50)
    private String topic;

    @Column(nullable = false)
    private boolean active;

    /**
     * authUserId del usuario que creó el círculo. Es la única base para decidir
     * quién puede editar o eliminar. Puede ser nulo en los círculos existentes
     * anteriores a la migración de propiedad: esos círculos quedan sin dueño y
     * por tanto no editables.
     */
    @Column(name = "created_by")
    private UUID createdBy;

    protected CommunityGroup() {
        super();
    }

    public CommunityGroup(String name, String description, String avatarUrl, String category) {
        super();
        this.name = name;
        this.description = description;
        this.avatarUrl = avatarUrl;
        this.category = category;
        this.membersCount = 0;
        this.active = true;
    }

    public void incrementMembers() {
        this.membersCount++;
    }

    public void decrementMembers() {
        if (this.membersCount > 0)
            this.membersCount--;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getCategory() {
        return category;
    }

    /** Temática del círculo; puede ser nula en círculos antiguos. */
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    // La validación (longitudes, temática admitida) vive en CommunityService;
    // aquí solo se aplica el valor ya saneado.
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /** authUserId del creador; puede ser nulo en círculos antiguos. */
    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
}