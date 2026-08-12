package com.petconnect.marketplace.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID sellerId,
        String name,
        String description,
        BigDecimal price,
        BigDecimal originalPrice,
        String category,
        String subcategory,
        String imageUrl,
        List<String> imageUrls,
        Integer stock,
        String brand,
        BigDecimal rating,
        Integer reviews,
        boolean active,
        Integer discount,
        String tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
