package com.petconnect.review.infrastructure.persistence;

import com.petconnect.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByProductId(UUID productId);
    Optional<Review> findByProductIdAndBuyerId(UUID productId, UUID buyerId);
}