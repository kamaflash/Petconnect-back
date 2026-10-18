package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.Adoption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAdoptionRepository extends JpaRepository<Adoption, UUID> {
    Page<Adoption> findByStatus(String status, Pageable pageable);

    Optional<Adoption> findByPetIdAndStatus(UUID petId, String status);
}
