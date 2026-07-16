package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "follows")
public class Follow extends BaseEntity {

    @Column(nullable = false)
    private UUID followerId;

    @Column(nullable = false)
    private UUID followingId;

    protected Follow() {
        super();
    }

    public Follow(UUID followerId, UUID followingId) {
        super();
        this.followerId = followerId;
        this.followingId = followingId;
    }

    public UUID getFollowerId() {
        return followerId;
    }

    public UUID getFollowingId() {
        return followingId;
    }
}