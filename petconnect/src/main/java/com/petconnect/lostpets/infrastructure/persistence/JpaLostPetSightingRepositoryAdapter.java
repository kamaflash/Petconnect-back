package com.petconnect.lostpets.infrastructure.persistence;

import com.petconnect.lostpets.domain.LostPetSighting;
import com.petconnect.lostpets.domain.repositories.LostPetSightingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaLostPetSightingRepositoryAdapter implements LostPetSightingRepository {

    private final SpringDataLostPetSightingRepository repository;

    public JpaLostPetSightingRepositoryAdapter(SpringDataLostPetSightingRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<LostPetSighting> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Page<LostPetSighting> findByReportId(UUID reportId, Pageable pageable) {
        return repository.findByReportId(reportId, pageable);
    }

    @Override
    public LostPetSighting save(LostPetSighting sighting) {
        return repository.save(sighting);
    }

    @Override
    public void delete(LostPetSighting sighting) {
        repository.delete(sighting);
    }
}
