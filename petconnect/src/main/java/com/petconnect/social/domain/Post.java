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

    @Column(name = "tags", length = 2000)
    private String tags;

    @Column(name = "pet_id")
    private UUID petId;

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
        this.tags = "";
        this.likesCount = 0;
        this.commentsCount = 0;
        this.active = true;
    }

    public Post(UUID authorId, String imageUrl, String caption, String tags) {
        super();
        this.authorId = authorId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.tags = tags;
        this.petId = null;
        this.likesCount = 0;
        this.commentsCount = 0;
        this.active = true;
    }

    public Post(UUID authorId, String imageUrl, String caption, String tags, UUID petId) {
        this(authorId, imageUrl, caption, tags);
        this.petId = petId;
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

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(UUID petId) {
        this.petId = petId;
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