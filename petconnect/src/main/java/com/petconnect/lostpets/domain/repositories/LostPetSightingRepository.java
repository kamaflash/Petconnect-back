package com.petconnect.lostpets.domain.repositories;

import com.petconnect.lostpets.domain.LostPetSighting;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LostPetSightingRepository {

    Optional<LostPetSighting> findById(UUID id);

    List<LostPetSighting> findByReportIdOrderBySightedAtDesc(UUID reportId);

    LostPetSighting save(LostPetSighting sighting);

    void delete(LostPetSighting sighting);
}
