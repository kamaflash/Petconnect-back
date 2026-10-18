package com.petconnect.lostpets.domain.repositories;

import com.petconnect.lostpets.domain.LostPetSighting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface LostPetSightingRepository {

    Optional<LostPetSighting> findById(UUID id);

    Page<LostPetSighting> findByReportId(UUID reportId, Pageable pageable);

    LostPetSighting save(LostPetSighting sighting);

    void delete(LostPetSighting sighting);
}
