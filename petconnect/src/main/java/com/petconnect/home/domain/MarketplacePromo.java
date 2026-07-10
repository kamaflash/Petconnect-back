package com.petconnect.home.domain;

import java.util.UUID;

public class MarketplacePromo {
    private UUID id;
    private String type;
    private String title;
    private String description;
    private String image;
    private String link;
    private String tag;

    public MarketplacePromo() {
    }

    public MarketplacePromo(UUID id, String type, String title, String description, String image, String link,
            String tag) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.image = image;
        this.link = link;
        this.tag = tag;
    }

    public UUID getId() {
        return id;
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

    public String getTag() {
        return tag;
    }
}