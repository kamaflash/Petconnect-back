package com.petconnect.marketplace.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Name is required") @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @NotNull(message = "Price is required") @DecimalMin(value = "0.01") BigDecimal price,
        BigDecimal originalPrice,
        @NotBlank(message = "Category is required") @Size(max = 100) String category,
        @Size(max = 100) String subcategory,
        @Size(max = 500) String imageUrl,
        @Size(max = 2000) String imageUrls,
        @NotNull(message = "Stock is required") Integer stock,
        @Size(max = 100) String brand,
        Integer discount,
        @Size(max = 500) String tags,
        MultipartFile image) {
}
