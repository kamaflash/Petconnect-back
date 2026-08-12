package com.petconnect.marketplace.infrastructure.persistence;

import com.petconnect.marketplace.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findBySellerId(UUID sellerId);

    List<Product> findByCategory(String category);

    List<Product> findByActiveTrue();
}
