package com.petconnect.pets.infrastructure.persistence;

import com.petconnect.pets.domain.PetCareShare;
import com.petconnect.pets.domain.PetCareShareStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataPetCareShareRepository extends JpaRepository<PetCareShare, UUID> {
    List<PetCareShare> findByPetIdOrderByCreatedAtAsc(UUID petId);

    List<PetCareShare> findByInvitedUserIdOrderByCreatedAtDesc(UUID invitedUserId);

    Optional<PetCareShare> findByIdAndPetId(UUID id, UUID petId);

    Optional<PetCareShare> findByPetIdAndInvitedUserId(UUID petId, UUID invitedUserId);

    boolean existsByPetIdAndInvitedUserIdAndStatus(UUID petId, UUID invitedUserId, PetCareShareStatus status);
}
