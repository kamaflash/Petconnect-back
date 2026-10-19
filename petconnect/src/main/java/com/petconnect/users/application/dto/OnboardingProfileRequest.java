package com.petconnect.users.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Datos del onboarding post-registro: tipo de cuenta y datos del perfil.
 *
 * <p>
 * Solo son obligatorios el tipo, el nombre y los apellidos, más los campos
 * propios del tipo elegido (nº de colegiado y servicios para {@code vet},
 * nº de registro para {@code shelter}, especialidad para {@code trainer},
 * nombre de tienda para {@code seller}).
 * </p>
 */
public record OnboardingProfileRequest(
                @NotBlank(message = "Profile type is required") @Size(max = 20) String profileType,

                @NotBlank(message = "First name is required") @Size(max = 50) String firstName,

                @NotBlank(message = "Last name is required") @Size(max = 50) String lastName,

                @Size(max = 20) String phone,

                @Size(max = 500) String bio,

                LocalDate dateOfBirth,

                @Size(max = 500) String location,

                Double latitude,

                Double longitude,

                @Size(max = 100) String specialty,

                @Size(max = 500) String services,

                @Size(max = 50) String licenseNumber,

                @Size(max = 100) String charityNumber,

                @Size(max = 100) String storeName,

                @Size(max = 255) String website) {
}