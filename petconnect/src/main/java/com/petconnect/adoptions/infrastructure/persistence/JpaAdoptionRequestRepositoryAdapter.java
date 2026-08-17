package com.petconnect.adoptions.infrastructure.persistence;

import com.petconnect.adoptions.domain.AdoptionRequest;
import com.petconnect.adoptions.domain.repositories.AdoptionRequestRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaAdoptionRequestRepositoryAdapter implements AdoptionRequestRepository {

    private final SpringDataAdoptionRequestRepository repository;

    public JpaAdoptionRequestRepositoryAdapter(SpringDataAdoptionRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AdoptionRequest> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<AdoptionRequest> findByAdopterId(UUID adopterId) {
        return repository.findByAdopterIdOrderByCreatedAtDesc(adopterId);
    }

    @Override
    public List<AdoptionRequest> findByOwnerId(UUID ownerId) {
        return repository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Override
    public List<AdoptionRequest> findByPetId(UUID petId) {
        return repository.findByPetIdOrderByCreatedAtDesc(petId);
    }

    @Override
    public AdoptionRequest save(AdoptionRequest adoptionRequest) {
        return repository.save(adoptionRequest);
    }
}