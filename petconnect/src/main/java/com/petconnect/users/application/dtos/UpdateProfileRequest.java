package com.petconnect.users.application.dtos;

import java.time.LocalDate;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String phone,
        String bio,
        LocalDate dateOfBirth,
        String city,
        String country,
        String website) {
}