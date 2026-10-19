package com.petconnect.marketplace.domain.repositories;

import com.petconnect.marketplace.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

import java.math.BigDecimal;

public interface ProductRepository {
    Optional<Product> findById(UUID id);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    Page<Product> findByCategoryAndActiveTrue(String category, Pageable pageable);

    /**
     * Búsqueda con filtros combinados (categoría, marca, texto, precio, stock) en BD,
     * ordenada por fecha de creación descendente (vía {@code Pageable}).
     */
    Page<Product> searchActive(String category, String brand, String term,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock,
            Pageable pageable);

    Product save(Product product);

    void delete(Product product);
}
