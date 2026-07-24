package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.Adoption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAdoptionRepository extends JpaRepository<Adoption, UUID> {
    List<Adoption> findByStatus(String status);

    Optional<Adoption> findByPetIdAndStatus(UUID petId, String status);
}
