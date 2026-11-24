package com.petconnect.marketplace.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.List;

/**
 * Petición de actualización parcial de producto. Todos los campos son opcionales:
 * si un campo es {@code null}, se conserva el valor actual en BD.
 * Las restricciones solo se aplican cuando el campo se envía (no nulo).
 */
public record UpdateProductRequest(
        @Size(max = 200, message = "Name must be at most 200 characters") String name,
        @Size(max = 2000, message = "Description must be at most 2000 characters") String description,
        @DecimalMin(value = "0", message = "Price must be >= 0") BigDecimal price,
        @DecimalMin(value = "0", message = "Original price must be >= 0") BigDecimal originalPrice,
        @Size(max = 100, message = "Category must be at most 100 characters") String category,
        @Size(max = 100, message = "Subcategory must be at most 100 characters") String subcategory,
        @URL(message = "Image URL must be a valid URL") @Size(max = 500, message = "Image URL must be at most 500 characters") String imageUrl,
        List<String> imageUrls,
        @Min(value = 0, message = "Stock must be >= 0") Integer stock,
        @Size(max = 100, message = "Brand must be at most 100 characters") String brand,
        @Min(value = 0, message = "Discount must be >= 0") @Max(value = 100, message = "Discount must be <= 100") Integer discount,
        @Size(max = 500, message = "Tags must be at most 500 characters") String tags,
        Boolean active
) {
}