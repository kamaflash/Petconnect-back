package com.petconnect.social.domain;

public enum PostAudience {
    PUBLIC,
    FOLLOWERS,
    ONLY_ME;

    public static PostAudience parse(String value) {
        if (value == null || value.isBlank()) {
            return PUBLIC;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid post audience: " + value, exception);
        }
    }
}
