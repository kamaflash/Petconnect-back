package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId; // This is the authUserId, not the UserProfile ID

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected Comment() {
        super();
    }

    public Comment(UUID userId, UUID targetId, String targetType, String content) {
        super();
        this.userId = userId; // This is the authUserId
        this.targetId = targetId;
        this.targetType = targetType;
        this.content = content;
        this.active = true;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.active = false;
    }

    public void restore() {
        this.active = true;
    }

    public UUID getUserId() {
        return userId; // Returns authUserId
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getContent() {
        return content;
    }

    public boolean isActive() {
        return active;
    }
}