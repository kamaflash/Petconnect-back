package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.Adoption;
import com.petconnect.home.domain.repositories.AdoptionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}