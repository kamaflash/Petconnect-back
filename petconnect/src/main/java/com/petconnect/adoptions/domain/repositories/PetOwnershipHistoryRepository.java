package com.petconnect.adoptions.domain.repositories;

import com.petconnect.adoptions.domain.PetOwnershipHistory;

import java.util.List;
import java.util.UUID;

public interface PetOwnershipHistoryRepository {
    List<PetOwnershipHistory> findByPetId(UUID petId);

    /**
     * Devuelve los cambios de propiedad de una mascota ordenados de forma
     * cronológica ascendente (más antiguo primero) para poder reconstruir
     * la línea de tiempo de propietarios.
     */
    List<PetOwnershipHistory> findByPetIdOrderByCreatedAtAsc(UUID petId);

    PetOwnershipHistory save(PetOwnershipHistory history);
}