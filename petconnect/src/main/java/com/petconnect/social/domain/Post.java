package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @Column(name = "author_id", nullable = false)
    private UUID authorId; // This is the authUserId, not the UserProfile ID

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "caption", length = 1000)
    private String caption;

    @Column(name = "likes_count", nullable = false)
    private int likesCount;

    @Column(name = "comments_count", nullable = false)
    private int commentsCount;

    @Column(name = "active", nullable = false)
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

    public void incrementLikes() {
        this.likesCount++;
    }

    public void decrementLikes() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }

    public void incrementComments() {
        this.commentsCount++;
    }

    public void decrementComments() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
        }
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void softDelete() {
        this.active = false;
    }

    public void restore() {
        this.active = true;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public boolean isActive() {
        return active;
    }
}