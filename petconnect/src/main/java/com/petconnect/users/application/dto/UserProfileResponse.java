package com.petconnect.users.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UserProfileResponse(
                UUID id,
                UUID authUserId,
                String firstName,
                String lastName,
                String phone,
                String bio,
                String avatarUrl,
                String coverImageUrl,
                LocalDate dateOfBirth,
                String location,
                Double latitude,
                Double longitude,
                boolean profilePublic,
                boolean notificationsEnabled,
                String profileType,
                String specialty,
                String licenseNumber,
                String charityNumber,
                String storeName,
                String website) {
}
