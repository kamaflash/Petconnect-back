package com.petconnect.invoice.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID orderId,
        String invoiceNumber,
        LocalDateTime issueDate,
        BigDecimal totalAmount,
        BigDecimal taxAmount,
        String status,
        String pdfUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}