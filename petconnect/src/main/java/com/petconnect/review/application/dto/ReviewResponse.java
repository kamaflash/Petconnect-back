package com.petconnect.review.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID productId,
        UUID buyerId,
        String buyerName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}