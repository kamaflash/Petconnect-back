package com.petconnect.marketplace.application.usecases;

import com.petconnect.marketplace.application.dto.CreateProductRequest;
import com.petconnect.marketplace.application.dto.ProductResponse;
import com.petconnect.marketplace.domain.Product;
import com.petconnect.marketplace.domain.repositories.ProductRepository;
import com.petconnect.shared.infrastructure.services.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CreateProductUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateProductUseCase.class);

    private final ProductRepository productRepository;
    private final Optional<CloudinaryService> cloudinaryService;

    public CreateProductUseCase(ProductRepository productRepository,
            Optional<CloudinaryService> cloudinaryService) {
        this.productRepository = productRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public ProductResponse execute(UUID sellerId, CreateProductRequest request) {
        var product = new Product(
                sellerId,
                request.name(),
                request.description(),
                request.price(),
                request.category(),
                request.subcategory(),
                request.imageUrl(),
                request.stock(),
                request.brand(),
                request.tags());

        product.updateDetails(
                request.name(),
                request.description(),
                request.price(),
                request.originalPrice(),
                request.category(),
                request.subcategory(),
                request.imageUrl(),
                request.imageUrls(),
                request.stock(),
                request.brand(),
                request.discount(),
                request.tags());

        // Handle image upload to Cloudinary
        if (request.image() != null && !request.image().isEmpty() && cloudinaryService.isPresent()) {
            try {
                String imageUrl = cloudinaryService.get().uploadImage(request.image());
                product.updateDetails(
                        request.name(),
                        request.description(),
                        request.price(),
                        request.originalPrice(),
                        request.category(),
                        request.subcategory(),
                        imageUrl,
                        request.imageUrls(),
                        request.stock(),
                        request.brand(),
                        request.discount(),
                        request.tags());
                log.info("Image uploaded successfully for product: {}", product.getName());
            } catch (IOException e) {
                log.error("Failed to upload image for product", e);
            }
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
                product.getUpdatedAt());
    }
}
