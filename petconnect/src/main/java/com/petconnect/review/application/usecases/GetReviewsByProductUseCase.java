package com.petconnect.review.application.usecases;

import com.petconnect.review.application.dto.ReviewResponse;
import com.petconnect.review.domain.Review;
import com.petconnect.review.domain.repositories.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetReviewsByProductUseCase {

    private final ReviewRepository reviewRepository;

    public GetReviewsByProductUseCase(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> execute(UUID productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(r -> new ReviewResponse(
                        r.getId(),
                        r.getProductId(),
                        r.getBuyerId(),
                        r.getBuyerName(),
                        r.getRating(),
                        r.getComment(),
                        r.getCreatedAt()))
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .collect(Collectors.toList());
    }
}