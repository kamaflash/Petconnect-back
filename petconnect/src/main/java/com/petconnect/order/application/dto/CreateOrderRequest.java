package com.petconnect.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID buyerId,
        @NotEmpty List<OrderItemRequest> items,
        @NotBlank String shippingAddress,
        @NotBlank String paymentMethod
) {
    public record OrderItemRequest(
            @NotNull UUID productId,
            @NotBlank String productName,
            String productImageUrl,
            @NotNull @Positive BigDecimal unitPrice,
            @NotNull @Positive Integer quantity
    ) {}
}