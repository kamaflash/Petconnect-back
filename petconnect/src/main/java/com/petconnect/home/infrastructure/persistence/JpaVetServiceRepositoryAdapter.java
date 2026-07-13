package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.VetService;
import com.petconnect.home.domain.repositories.VetServiceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaVetServiceRepositoryAdapter implements VetServiceRepository {

    private final SpringDataVetServiceRepository repository;

    public JpaVetServiceRepositoryAdapter(SpringDataVetServiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<VetService> findByActiveTrue() {
        return repository.findByActiveTrue();
    }
}