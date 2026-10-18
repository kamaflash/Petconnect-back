package com.petconnect.marketplace.infrastructure.persistence;

import com.petconnect.marketplace.domain.Product;
import com.petconnect.marketplace.domain.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    public Page<Product> searchActive(String category, String term,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock,
            Pageable pageable) {
        return springDataRepository.searchActive(category, term,
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
