package com.petconnect.marketplace.application.usecases;

import com.petconnect.marketplace.application.dto.ProductResponse;
import com.petconnect.marketplace.domain.Product;
import com.petconnect.marketplace.domain.repositories.ProductRepository;
import com.petconnect.shared.presentation.dto.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

import java.util.UUID;

@Service
public class GetProductByIdUseCase {

    private final ProductRepository productRepository;

    public GetProductByIdUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ProductResponse execute(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));
        return toResponse(product);
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