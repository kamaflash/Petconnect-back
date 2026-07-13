package com.petconnect.home.domain.repositories;

import com.petconnect.home.domain.MarketplacePromo;
import java.util.List;

public interface MarketplacePromoRepository {
    List<MarketplacePromo> findByActiveTrue();
}
