package com.petconnect.pets.infrastructure.persistence;

import com.petconnect.pets.domain.PetCareActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataPetCareActivityRepository extends JpaRepository<PetCareActivity, UUID> {
    List<PetCareActivity> findTop100ByPetIdOrderByOccurredAtDesc(UUID petId);
}
