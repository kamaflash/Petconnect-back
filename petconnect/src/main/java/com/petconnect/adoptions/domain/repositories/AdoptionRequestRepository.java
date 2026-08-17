package com.petconnect.adoptions.domain.repositories;

import com.petconnect.adoptions.domain.AdoptionRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdoptionRequestRepository {
    Optional<AdoptionRequest> findById(UUID id);

    List<AdoptionRequest> findByAdopterId(UUID adopterId);

    List<AdoptionRequest> findByOwnerId(UUID ownerId);

    List<AdoptionRequest> findByPetId(UUID petId);

    AdoptionRequest save(AdoptionRequest adoptionRequest);
}