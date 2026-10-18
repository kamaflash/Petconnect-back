package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.Adoption;
import com.petconnect.home.domain.repositories.AdoptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaAdoptionRepositoryAdapter implements AdoptionRepository {

    private final SpringDataAdoptionRepository repository;

    public JpaAdoptionRepositoryAdapter(SpringDataAdoptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Adoption> findByStatus(String status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    @Override
    public Optional<Adoption> findByPetIdAndStatus(java.util.UUID petId, String status) {
        return repository.findByPetIdAndStatus(petId, status);
    }

    @Override
    public void delete(Adoption adoption) {
        repository.delete(adoption);
    }

    @Override
    public Adoption save(Adoption adoption) {
        return repository.save(adoption);
    }
}
