package com.petconnect.home.domain;

import java.util.UUID;

public class Story {
    private UUID id;
    private UUID userId;
    private boolean seen;

    public Story() {
    }

    public Story(UUID id, UUID userId, boolean seen) {
        this.id = id;
        this.userId = userId;
        this.seen = seen;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isSeen() {
        return seen;
    }
}