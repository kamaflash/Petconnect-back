package com.petconnect.adoptions.infrastructure.persistence;

import com.petconnect.adoptions.domain.AdoptionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataAdoptionRequestRepository extends JpaRepository<AdoptionRequest, UUID> {
    Page<AdoptionRequest> findByAdopterId(UUID adopterId, Pageable pageable);

    Page<AdoptionRequest> findByOwnerId(UUID ownerId, Pageable pageable);

    Page<AdoptionRequest> findByPetId(UUID petId, Pageable pageable);
}