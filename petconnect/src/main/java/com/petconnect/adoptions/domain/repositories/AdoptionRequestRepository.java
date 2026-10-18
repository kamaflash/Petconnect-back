package com.petconnect.adoptions.domain.repositories;

import com.petconnect.adoptions.domain.AdoptionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AdoptionRequestRepository {
    Optional<AdoptionRequest> findById(UUID id);

    Page<AdoptionRequest> findByAdopterId(UUID adopterId, Pageable pageable);

    Page<AdoptionRequest> findByOwnerId(UUID ownerId, Pageable pageable);

    Page<AdoptionRequest> findByPetId(UUID petId, Pageable pageable);

    AdoptionRequest save(AdoptionRequest adoptionRequest);
}