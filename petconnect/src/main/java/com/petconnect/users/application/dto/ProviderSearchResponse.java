package com.petconnect.users.application.dto;

import java.util.List;

/** Proveedores (vet/shelter/seller/trainer) que coinciden con los filtros de búsqueda. */
public record ProviderSearchResponse(
        List<UserProfileResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
