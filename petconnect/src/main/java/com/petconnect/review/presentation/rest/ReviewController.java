package com.petconnect.review.presentation.rest;

import com.petconnect.review.application.dto.ReviewRequest;
import com.petconnect.review.application.dto.ReviewResponse;
import com.petconnect.review.application.usecases.CreateReviewUseCase;
import com.petconnect.review.application.usecases.GetReviewsByProductUseCase;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/marketplace/products/{productId}/reviews")
public class ReviewController {

    private final CreateReviewUseCase createReviewUseCase;
    private final GetReviewsByProductUseCase getReviewsByProductUseCase;

    public ReviewController(CreateReviewUseCase createReviewUseCase,
            GetReviewsByProductUseCase getReviewsByProductUseCase) {
        this.createReviewUseCase = createReviewUseCase;
        this.getReviewsByProductUseCase = getReviewsByProductUseCase;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID productId,
            @RequestBody ReviewRequest request) {
        ReviewResponse review = createReviewUseCase.execute(productId, userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @PathVariable UUID productId) {
        List<ReviewResponse> reviews = getReviewsByProductUseCase.execute(productId);
        return ResponseEntity.ok(reviews);
    }
}