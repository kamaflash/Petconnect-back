package com.petconnect.marketplace.infrastructure.persistence;

import com.petconnect.marketplace.domain.Product;
import com.petconnect.marketplace.domain.repositories.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public List<Product> findAll() {
        return springDataRepository.findByActiveTrue();
    }

    @Override
    public List<Product> findBySellerId(UUID sellerId) {
        return springDataRepository.findBySellerId(sellerId);
    }

    @Override
    public List<Product> findByCategory(String category) {
        return springDataRepository.findByCategory(category);
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
