package com.petconnect.marketplace.infrastructure.persistence;

import com.petconnect.marketplace.domain.Product;
import com.petconnect.marketplace.domain.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductRepository springDataRepository;

    public JpaProductRepositoryAdapter(SpringDataProductRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Page<Product> findByActiveTrue(Pageable pageable) {
        return springDataRepository.findByActiveTrue(pageable);
    }

    @Override
    public Page<Product> findBySellerId(UUID sellerId, Pageable pageable) {
        return springDataRepository.findBySellerId(sellerId, pageable);
    }

    @Override
    public Page<Product> findByCategoryAndActiveTrue(String category, Pageable pageable) {
        return springDataRepository.findByCategoryAndActiveTrue(category, pageable);
    }

    @Override
    public Page<Product> searchActive(String category, String brand, String term,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock,
            Pageable pageable) {
        // El patrón LIKE se construye aquí y NUNCA es null: si no hay término
        // de búsqueda se envía "%%" (coincide con todo, la misma semántica que
        // la anterior rama ":term IS NULL"). Un String null viajaba dentro de
        // CONCAT('%', :term, '%') bindeado como bytea y PostgreSQL respondía
        // "lower(bytea) does not exist" (HTTP 500).
        String pattern = term == null || term.isBlank()
                ? "%%"
                : "%" + term.trim() + "%";
        // La marca se compara como LOWER(p.brand) = :brand, así que el parámetro
        // se lleva a minúsculas aquí (null si no hay filtro). De este modo el
        // parámetro nunca pasa por LOWER() en la query, evitando de nuevo la
        // inferencia de tipo bytea con null.
        String brandLower = brand == null || brand.isBlank()
                ? null
                : brand.trim().toLowerCase(Locale.ROOT);
        return springDataRepository.searchActive(category, brandLower, pattern,
                minPrice, maxPrice, inStock, pageable);
    }

    @Override
    public Product save(Product product) {
        return springDataRepository.save(product);
    }

    @Override
    public void delete(Product product) {
        springDataRepository.delete(product);
    }
}
