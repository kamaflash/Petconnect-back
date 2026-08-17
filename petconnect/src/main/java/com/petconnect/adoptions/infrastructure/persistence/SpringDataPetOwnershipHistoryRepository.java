package com.petconnect.adoptions.infrastructure.persistence;

import com.petconnect.adoptions.domain.PetOwnershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataPetOwnershipHistoryRepository extends JpaRepository<PetOwnershipHistory, UUID> {
    List<PetOwnershipHistory> findByPetIdOrderByCreatedAtDesc(UUID petId);
    List<PetOwnershipHistory> findByPetIdOrderByCreatedAtAsc(UUID petId);
}