package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stories")
public class Story extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private boolean seen;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    protected Story() {
        super();
    }

    public Story(UUID id, UUID userId, String imageUrl, boolean seen, LocalDateTime expiresAt) {
        super(id);
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.seen = seen;
        this.expiresAt = expiresAt;
    }

    public Story(UUID id, LocalDateTime createdAt, LocalDateTime updatedAt, UUID userId, String imageUrl,
            boolean seen, LocalDateTime expiresAt) {
        super(id, createdAt);
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.seen = seen;
        this.expiresAt = expiresAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isSeen() {
        return seen;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void markAsSeen() {
        this.seen = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}