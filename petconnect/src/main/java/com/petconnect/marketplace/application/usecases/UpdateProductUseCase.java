package com.petconnect.marketplace.application.usecases;

import com.petconnect.marketplace.application.dto.ProductResponse;
import com.petconnect.marketplace.application.dto.UpdateProductRequest;
import com.petconnect.marketplace.domain.Product;
import com.petconnect.marketplace.domain.exceptions.ProductNotFoundException;
import com.petconnect.marketplace.domain.exceptions.ProductNotOwnedException;
import com.petconnect.marketplace.domain.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateProductUseCase {

    private final ProductRepository productRepository;

    public UpdateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse execute(UUID productId, UUID sellerId, UpdateProductRequest request) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        if (!product.getSellerId().equals(sellerId)) {
            throw new ProductNotOwnedException("You are not the owner of this product");
        }

        // Actualiza solo los campos que vienen no nulos (actualización parcial).
        product.updatePartial(
                request.name(),
                request.description(),
                request.price(),
                request.originalPrice(),
                request.category(),
                request.subcategory(),
                request.imageUrl(),
                request.imageUrls() != null ? String.join(",", request.imageUrls()) : null,
                request.stock(),
                request.brand(),
                request.discount(),
                request.tags()
        );

        // active se envía explícitamente cuando se quiere activar/desactivar.
        if (request.active() != null) {
            product.setActive(request.active());
        }

        var saved = productRepository.save(product);
        return toResponse(saved);
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
                product.getUpdatedAt()
        );
    }
}