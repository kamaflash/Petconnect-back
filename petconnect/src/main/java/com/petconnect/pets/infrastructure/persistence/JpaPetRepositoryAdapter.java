package com.petconnect.pets.infrastructure.persistence;

import com.petconnect.pets.domain.Pet;
import com.petconnect.pets.domain.repositories.PetRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaPetRepositoryAdapter implements PetRepository {

    private final SpringDataPetRepository springDataPetRepository;

    public JpaPetRepositoryAdapter(SpringDataPetRepository springDataPetRepository) {
        this.springDataPetRepository = springDataPetRepository;
    }

    @Override
    public Optional<Pet> findById(UUID id) {
        return springDataPetRepository.findById(id);
    }

    @Override
    public List<Pet> findByOwnerId(UUID ownerId) {
        return springDataPetRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Pet> findBySpecies(String species) {
        return springDataPetRepository.findBySpecies(species);
    }

    @Override
    public Pet save(Pet pet) {
        return springDataPetRepository.save(pet);
    }

    @Override
    public void delete(Pet pet) {
        springDataPetRepository.delete(pet);
    }
}