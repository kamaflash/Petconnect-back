package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private boolean read;

    private UUID referenceId;

    @Column(length = 50)
    private String referenceType;

    private UUID actorId;

    protected Notification() {
        super();
    }

    public Notification(UUID userId, String type, String content, UUID actorId) {
        super();
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.read = false;
        this.actorId = actorId;
    }

    public void markAsRead() {
        this.read = true;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public boolean isRead() {
        return read;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UUID getActorId() {
        return actorId;
    }
}