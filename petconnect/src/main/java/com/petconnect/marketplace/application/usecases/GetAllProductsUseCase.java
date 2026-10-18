package com.petconnect.marketplace.application.usecases;

import com.petconnect.marketplace.application.dto.ProductResponse;
import com.petconnect.marketplace.domain.Product;
import com.petconnect.marketplace.domain.repositories.ProductRepository;
import com.petconnect.shared.presentation.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class GetAllProductsUseCase {

    /** Tamaño máximo de página admitido en los listados. */
    private static final int MAX_PAGE_SIZE = 50;

    private final ProductRepository productRepository;

    public GetAllProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Catálogo paginado (10×10 por defecto), siempre ordenado por fecha de
     * creación descendente (lo más actual primero). Todos los filtros se
     * aplican en base de datos.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> execute(String category, String term,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock,
            int page, int size) {
        Pageable pageable = newestFirst(page, size);
        Page<Product> result = productRepository.searchActive(
                blankToNull(category), blankToNull(term),
                minPrice, maxPrice, inStock, pageable);
        return PageResponse.from(result, this::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> executeBySeller(UUID sellerId, int page, int size) {
        Page<Product> result = productRepository.findBySellerId(sellerId, newestFirst(page, size));
        return PageResponse.from(result, this::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> executeByCategory(String category, int page, int size) {
        Page<Product> result = productRepository.findByCategoryAndActiveTrue(
                category, newestFirst(page, size));
        return PageResponse.from(result, this::toResponse);
    }

    private static Pageable newestFirst(int page, int size) {
        int safeSize = size < 1 ? 10 : Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(page, 0), safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSellerId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.getCategory(),
                product.getSubcategory(),
                product.getImageUrl(),
                product.getImageUrlsList(),
                product.getStock(),
                product.getBrand(),
                product.getRating(),
                product.getReviews(),
                product.isActive(),
                product.getDiscount(),
                product.getTags(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
