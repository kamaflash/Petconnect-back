package com.petconnect.adoptions.infrastructure.persistence;

import com.petconnect.adoptions.domain.AdoptionRequest;
import com.petconnect.adoptions.domain.repositories.AdoptionRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
    public Page<AdoptionRequest> findByAdopterId(UUID adopterId, Pageable pageable) {
        return repository.findByAdopterId(adopterId, pageable);
    }

    @Override
    public Page<AdoptionRequest> findByOwnerId(UUID ownerId, Pageable pageable) {
        return repository.findByOwnerId(ownerId, pageable);
    }

    @Override
    public Page<AdoptionRequest> findByPetId(UUID petId, Pageable pageable) {
        return repository.findByPetId(petId, pageable);
    }

    @Override
    public AdoptionRequest save(AdoptionRequest adoptionRequest) {
        return repository.save(adoptionRequest);
    }
}