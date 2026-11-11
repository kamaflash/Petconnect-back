package com.petconnect.review.domain.repositories;

import com.petconnect.review.domain.Review;
import java.util.List;
import java.util.UUID;

public interface ReviewRepository {
    Review save(Review review);
    List<Review> findByProductId(UUID productId);
    boolean existsByProductIdAndBuyerId(UUID productId, UUID buyerId);
}