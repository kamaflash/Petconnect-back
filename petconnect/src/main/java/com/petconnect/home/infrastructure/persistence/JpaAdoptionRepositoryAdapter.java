package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.Adoption;
import com.petconnect.home.domain.repositories.AdoptionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaAdoptionRepositoryAdapter implements AdoptionRepository {

    private final SpringDataAdoptionRepository repository;

    public JpaAdoptionRepositoryAdapter(SpringDataAdoptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Adoption> findByStatus(String status) {
        return repository.findByStatus(status);
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
