package com.petconnect.users.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank(message = "First name is required") @Size(max = 50) String firstName,
        @NotBlank(message = "Last name is required") @Size(max = 50) String lastName,
        @Size(max = 20) String phone,
        @Size(max = 500) String bio,
        LocalDate dateOfBirth,
        @Size(max = 100) String city,
        @Size(max = 100) String country) {
}