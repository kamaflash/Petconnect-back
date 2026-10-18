package com.petconnect.home.domain.repositories;

import com.petconnect.home.domain.Adoption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AdoptionRepository {
    /** Adopciones disponibles, paginadas por fecha de creación descendente. */
    Page<Adoption> findByStatus(String status, Pageable pageable);

    Optional<Adoption> findByPetIdAndStatus(UUID petId, String status);

    void delete(Adoption adoption);

    Adoption save(Adoption adoption);
}
