package com.petconnect.adoptions.infrastructure.persistence;

import com.petconnect.adoptions.domain.PetOwnershipHistory;
import com.petconnect.adoptions.domain.repositories.PetOwnershipHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaPetOwnershipHistoryRepositoryAdapter implements PetOwnershipHistoryRepository {

    private final SpringDataPetOwnershipHistoryRepository repository;

    public JpaPetOwnershipHistoryRepositoryAdapter(SpringDataPetOwnershipHistoryRepository repository) {
        this.repository = repository;
    }

        @Override
    public List<PetOwnershipHistory> findByPetId(UUID petId) {
        return repository.findByPetIdOrderByCreatedAtDesc(petId);
    }

    @Override
    public List<PetOwnershipHistory> findByPetIdOrderByCreatedAtAsc(UUID petId) {
        return repository.findByPetIdOrderByCreatedAtAsc(petId);
    }

    @Override
    public PetOwnershipHistory save(PetOwnershipHistory history) {
        return repository.save(history);
    }
}