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
     * Filtros combinados en BD: categoría exacta, marca exacta (el parámetro
     * llega ya en minúsculas y se compara contra LOWER(p.brand), sin envolver
     * el parámetro en LOWER() para no repetir el problema de tipo bytea con
     * null), texto en nombre/descripción/
     * marca (insensible a mayúsculas), rango de precio y disponibilidad.
     * La ordenación la impone el {@code Pageable} (siempre {@code createdAt} DESC).
     *
     * <p>{@code :pattern} es el patrón LIKE completo ({@code %texto%}) y se
     * construye en el adapter, nunca null (sin filtro de texto se envía
     * {@code %%}). No se usa {@code CONCAT('%', :term, '%')} en la query porque
     * Hibernate+pgjdbc bindean un parámetro String null dentro de CONCAT como
     * bytea y PostgreSQL falla con {@code lower(bytea) does not exist}.
     */
    @Query(value = """
            SELECT p FROM Product p
            WHERE p.active = true
              AND (:category IS NULL OR p.category = :category)
              AND (:brand IS NULL OR LOWER(p.brand) = :brand)
              AND (LOWER(p.name) LIKE LOWER(:pattern)
                OR LOWER(p.description) LIKE LOWER(:pattern)
                OR LOWER(p.brand) LIKE LOWER(:pattern))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:inStock IS NULL OR (:inStock = true AND p.stock > 0)
                OR (:inStock = false AND p.stock <= 0))
            """,
            countQuery = """
                    SELECT COUNT(p) FROM Product p
                    WHERE p.active = true
                      AND (:category IS NULL OR p.category = :category)
                      AND (:brand IS NULL OR LOWER(p.brand) = :brand)
                      AND (LOWER(p.name) LIKE LOWER(:pattern)
                        OR LOWER(p.description) LIKE LOWER(:pattern)
                        OR LOWER(p.brand) LIKE LOWER(:pattern))
                      AND (:minPrice IS NULL OR p.price >= :minPrice)
                      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                      AND (:inStock IS NULL OR (:inStock = true AND p.stock > 0)
                        OR (:inStock = false AND p.stock <= 0))
                    """)
    Page<Product> searchActive(
            @Param("category") String category,
            @Param("brand") String brand,
            @Param("pattern") String pattern,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("inStock") Boolean inStock,
            Pageable pageable);
}
