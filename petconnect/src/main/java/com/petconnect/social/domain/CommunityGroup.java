package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private boolean active;

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

    public int getMembersCount() {
        return membersCount;
    }

    public boolean isActive() {
        return active;
    }
}