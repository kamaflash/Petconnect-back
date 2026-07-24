package com.petconnect.home.domain.repositories;

import com.petconnect.home.domain.Adoption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdoptionRepository {
    List<Adoption> findByStatus(String status);

    Optional<Adoption> findByPetIdAndStatus(UUID petId, String status);

    void delete(Adoption adoption);

    Adoption save(Adoption adoption);
}
