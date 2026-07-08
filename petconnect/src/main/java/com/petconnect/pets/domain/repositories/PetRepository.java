package com.petconnect.pets.domain.repositories;

import com.petconnect.pets.domain.Pet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PetRepository {
    Optional<Pet> findById(UUID id);

    List<Pet> findByOwnerId(UUID ownerId);

    List<Pet> findBySpecies(String species);

    Pet save(Pet pet);

    void delete(Pet pet);
}