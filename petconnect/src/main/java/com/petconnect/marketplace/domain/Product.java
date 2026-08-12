package com.petconnect.marketplace.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(length = 100)
    private String subcategory;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 100)
    private String brand;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column
    private Integer reviews = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column
    private Integer discount;

    @Column(length = 500)
    private String tags;

    protected Product() {
        super();
    }

    public Product(UUID sellerId, String name, String description, BigDecimal price,
            String category, String subcategory, String imageUrl, Integer stock,
            String brand, String tags) {
        super();
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.subcategory = subcategory;
        this.imageUrl = imageUrl;
        this.stock = stock != null ? stock : 0;
        this.brand = brand;
        this.tags = tags;
        this.active = true;
    }

    public void updateDetails(String name, String description, BigDecimal price,
            BigDecimal originalPrice, String category, String subcategory,
            String imageUrl, String imageUrls, Integer stock,
            String brand, Integer discount, String tags) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.originalPrice = originalPrice;
        this.category = category;
        this.subcategory = subcategory;
        this.imageUrl = imageUrl;
        this.imageUrls = imageUrls;
        this.stock = stock != null ? stock : 0;
        this.brand = brand;
        this.discount = discount;
        this.tags = tags;
    }

    public void updateStock(Integer stock) {
        this.stock = stock;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getImageUrlsList() {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(imageUrls.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public void setImageUrlsList(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            this.imageUrls = null;
        } else {
            this.imageUrls = String.join(",", urls);
        }
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public String getCategory() {
        return category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public Integer getStock() {
        return stock;
    }

    public String getBrand() {
        return brand;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public Integer getReviews() {
        return reviews;
    }

    public boolean isActive() {
        return active;
    }

    public Integer getDiscount() {
        return discount;
    }

    public String getTags() {
        return tags;
    }
}
