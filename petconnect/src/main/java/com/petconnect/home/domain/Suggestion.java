package com.petconnect.home.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "suggestions")
public class Suggestion extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "avatar_url", nullable = false)
    private String avatar;

    @Column(name = "followers_count", nullable = false)
    private int followers;

    protected Suggestion() {
        super();
    }

    public Suggestion(UUID id, String username, String avatar, int followers) {
        super(id);
        this.username = username;
        this.avatar = avatar;
        this.followers = followers;
    }

    public Suggestion(UUID id, String username, String avatar, int followers, LocalDateTime createdAt) {
        super(id, createdAt);
        this.username = username;
        this.avatar = avatar;
        this.followers = followers;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getFollowers() {
        return followers;
    }
}