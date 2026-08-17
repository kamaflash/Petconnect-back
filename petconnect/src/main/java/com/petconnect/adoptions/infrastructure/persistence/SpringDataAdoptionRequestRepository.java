package com.petconnect.adoptions.infrastructure.persistence;

import com.petconnect.adoptions.domain.AdoptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataAdoptionRequestRepository extends JpaRepository<AdoptionRequest, UUID> {
    List<AdoptionRequest> findByAdopterIdOrderByCreatedAtDesc(UUID adopterId);

    List<AdoptionRequest> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    List<AdoptionRequest> findByPetIdOrderByCreatedAtDesc(UUID petId);
}