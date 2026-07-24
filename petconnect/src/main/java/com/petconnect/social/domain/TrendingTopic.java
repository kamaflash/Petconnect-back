package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "trending_topics")
public class TrendingTopic extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private int postsCount;

    @Column(nullable = false)
    private boolean active;

    protected TrendingTopic() {
        super();
    }

    public TrendingTopic(String name, int postsCount) {
        super();
        this.name = name;
        this.postsCount = postsCount;
        this.active = true;
    }

    public void incrementPostsCount() {
        this.postsCount++;
    }

    public String getName() {
        return name;
    }

    public int getPostsCount() {
        return postsCount;
    }

    public boolean isActive() {
        return active;
    }
}
