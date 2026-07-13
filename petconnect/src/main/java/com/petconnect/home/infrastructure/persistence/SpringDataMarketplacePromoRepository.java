package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.MarketplacePromo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataMarketplacePromoRepository extends JpaRepository<MarketplacePromo, UUID> {
    List<MarketplacePromo> findByActiveTrue();
}