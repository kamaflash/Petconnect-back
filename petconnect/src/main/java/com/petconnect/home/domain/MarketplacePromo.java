package com.petconnect.home.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "market_promos")
public class MarketplacePromo extends BaseEntity {

    @Column(name = "category", nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "image_url", nullable = false)
    private String image;

    @Column(name = "link_url", nullable = false)
    private String link;

    @Column(nullable = true)
    private String badge;

    @Column(nullable = false)
    private boolean active;

    protected MarketplacePromo() {
        super();
    }

    public MarketplacePromo(UUID id, String type, String title, String description, String image, String link,
            String tag) {
        super(id);
        this.type = type;
        this.title = title;
        this.description = description;
        this.image = image;
        this.link = link;
        this.badge = tag;
        this.active = true;
    }

    public MarketplacePromo(UUID id, String type, String title, String description, String image, String link,
            String tag, LocalDateTime createdAt) {
        super(id, createdAt);
        this.type = type;
        this.title = title;
        this.description = description;
        this.image = image;
        this.link = link;
        this.badge = tag;
        this.active = true;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public String getLink() {
        return link;
    }

    public String getBadge() {
        return badge;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}