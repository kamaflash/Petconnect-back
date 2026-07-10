package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @Column(nullable = false)
    private UUID authorId;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 1000)
    private String caption;

    @Column(nullable = false)
    private int likesCount;

    @Column(nullable = false)
    private int commentsCount;

    @Column(nullable = false)
    private boolean active;

    protected Post() {
        super();
    }

    public Post(UUID authorId, String imageUrl, String caption) {
        super();
        this.authorId = authorId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.likesCount = 0;
        this.commentsCount = 0;
        this.active = true;
    }

    public UUID getAuthorId() { return authorId; }
    public String getImageUrl() { return imageUrl; }
    public String getCaption() { return caption; }
    public int getLikesCount() { return likesCount; }
    public int getCommentsCount() { return commentsCount; }
    public boolean isActive() { return active; }
}