package com.petconnect.review.application.usecases;

import com.petconnect.review.application.dto.ReviewRequest;
import com.petconnect.review.application.dto.ReviewResponse;
import com.petconnect.review.domain.Review;
import com.petconnect.review.domain.repositories.ReviewRepository;
import com.petconnect.marketplace.domain.repositories.ProductRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateReviewUseCase {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserProfileRepository userProfileRepository;

    public CreateReviewUseCase(ReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserProfileRepository userProfileRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public ReviewResponse execute(UUID productId, UUID buyerId, ReviewRequest request) {
        // Validar que el producto exista
        if (productRepository.findById(productId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId);
        }

        // Evitar reseñas duplicadas del mismo comprador en el mismo producto
        if (reviewRepository.existsByProductIdAndBuyerId(productId, buyerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya has valorado este producto");
        }

        // Obtener nombre del comprador
        UserProfile buyer = userProfileRepository.findByAuthUserId(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer profile not found"));

        Review review = new Review(productId, buyerId, buyer.getFirstName() + " " + buyer.getLastName(),
                request.rating(), request.comment());
        Review saved = reviewRepository.save(review);

        return new ReviewResponse(
                saved.getId(),
                saved.getProductId(),
                saved.getBuyerId(),
                saved.getBuyerName(),
                saved.getRating(),
                saved.getComment(),
                saved.getCreatedAt());
    }
}