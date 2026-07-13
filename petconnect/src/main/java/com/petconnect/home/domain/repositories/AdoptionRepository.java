package com.petconnect.home.domain.repositories;

import com.petconnect.home.domain.Adoption;
import java.util.List;

public interface AdoptionRepository {
    List<Adoption> findByStatus(String status);
}
