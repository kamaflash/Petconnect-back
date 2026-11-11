package com.petconnect.review.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "buyer_name", nullable = false, length = 100)
    private String buyerName;

    @Column(nullable = false)
    private Integer rating; // 1 a 5

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    protected Review() {
        super();
    }

    public Review(UUID productId, UUID buyerId, String buyerName, Integer rating, String comment) {
        super();
        this.productId = productId;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.rating = rating;
        this.comment = comment;
    }

    public UUID getProductId() { return productId; }
    public UUID getBuyerId() { return buyerId; }
    public String getBuyerName() { return buyerName; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
}