package com.petconnect.marketplace.domain.repositories;

import com.petconnect.marketplace.domain.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID id);

    List<Product> findAll();

    List<Product> findBySellerId(UUID sellerId);

    List<Product> findByCategory(String category);

    Product save(Product product);

    void delete(Product product);
}
