package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.MarketplacePromo;
import com.petconnect.home.domain.repositories.MarketplacePromoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaMarketplacePromoRepositoryAdapter implements MarketplacePromoRepository {

    private final SpringDataMarketplacePromoRepository repository;

    public JpaMarketplacePromoRepositoryAdapter(SpringDataMarketplacePromoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MarketplacePromo> findByActiveTrue() {
        return repository.findByActiveTrue();
    }
}