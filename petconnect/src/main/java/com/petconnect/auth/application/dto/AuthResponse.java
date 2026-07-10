package com.petconnect.auth.application.dto;

import com.petconnect.users.application.dto.UserProfileResponse;

import java.util.UUID;

public record AuthResponse(
                UUID userId,
                String email,
                String role,
                String accessToken,
                String refreshToken,
                long expiresIn,
                UserProfileResponse profile) {
}
