package com.petconnect.pets.infrastructure.persistence;

import com.petconnect.pets.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataPetRepository extends JpaRepository<Pet, UUID> {
    List<Pet> findByOwnerId(UUID ownerId);

    List<Pet> findBySpecies(String species);
}