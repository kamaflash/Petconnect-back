package com.petconnect.users.application.dto;

import java.util.UUID;

public record BlockedUserResponse(
        UUID userId,
        String firstName,
        String lastName,
        String avatarUrl) {
}
