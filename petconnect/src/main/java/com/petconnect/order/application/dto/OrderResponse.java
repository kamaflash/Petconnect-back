package com.petconnect.order.application.dto;

import com.petconnect.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID sellerId,
        UUID buyerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String shippingAddress,
        String paymentMethod,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}