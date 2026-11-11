package com.petconnect.review.infrastructure.persistence;

import com.petconnect.review.domain.Review;
import com.petconnect.review.domain.repositories.ReviewRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaReviewRepositoryAdapter implements ReviewRepository {

    private final SpringDataReviewRepository repository;

    public JpaReviewRepositoryAdapter(SpringDataReviewRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Review save(Review review) {
        return repository.save(review);
    }

    @Override
    public List<Review> findByProductId(UUID productId) {
        return repository.findByProductId(productId);
    }

    @Override
    public boolean existsByProductIdAndBuyerId(UUID productId, UUID buyerId) {
        return repository.findByProductIdAndBuyerId(productId, buyerId).isPresent();
    }
}