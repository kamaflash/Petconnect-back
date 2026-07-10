package com.petconnect.home.domain;

import java.util.UUID;

public class Suggestion {
    private UUID id;
    private String username;
    private String avatar;
    private int followers;

    public Suggestion() {
    }

    public Suggestion(UUID id, String username, String avatar, int followers) {
        this.id = id;
        this.username = username;
        this.avatar = avatar;
        this.followers = followers;
    }

    public UUID getId() {
        return id;
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