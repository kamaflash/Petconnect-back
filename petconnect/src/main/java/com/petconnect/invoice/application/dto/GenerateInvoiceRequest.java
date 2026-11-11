package com.petconnect.invoice.application.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record GenerateInvoiceRequest(
        @NotNull UUID orderId,
        BigDecimal taxAmount
) {}