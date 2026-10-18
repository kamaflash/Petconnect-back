package com.petconnect.marketplace.infrastructure.persistence;

import com.petconnect.marketplace.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    Page<Product> findByCategoryAndActiveTrue(String category, Pageable pageable);

    /**
     * Filtros combinados en BD: categoría exacta, texto en nombre/descripción/
     * marca (insensible a mayúsculas), rango de precio y disponibilidad.
     * La ordenación la impone el {@code Pageable} (siempre {@code createdAt} DESC).
     */
    @Query(value = """
            SELECT p FROM Product p
            WHERE p.active = true
              AND (:category IS NULL OR p.category = :category)
              AND (:term IS NULL
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :term, '%'))
                OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :term, '%')))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:inStock IS NULL OR (:inStock = true AND p.stock > 0)
                OR (:inStock = false AND p.stock <= 0))
            """,
            countQuery = """
                    SELECT COUNT(p) FROM Product p
                    WHERE p.active = true
                      AND (:category IS NULL OR p.category = :category)
                      AND (:term IS NULL
                        OR LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%'))
                        OR LOWER(p.description) LIKE LOWER(CONCAT('%', :term, '%'))
                        OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :term, '%')))
                      AND (:minPrice IS NULL OR p.price >= :minPrice)
                      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                      AND (:inStock IS NULL OR (:inStock = true AND p.stock > 0)
                        OR (:inStock = false AND p.stock <= 0))
                    """)
    Page<Product> searchActive(
            @Param("category") String category,
            @Param("term") String term,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("inStock") Boolean inStock,
            Pageable pageable);
}
