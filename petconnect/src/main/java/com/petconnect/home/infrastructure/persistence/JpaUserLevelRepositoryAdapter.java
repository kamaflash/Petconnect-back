package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.UserLevel;
import com.petconnect.home.domain.repositories.UserLevelRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserLevelRepositoryAdapter implements UserLevelRepository {

    private final SpringDataUserLevelRepository repository;

    public JpaUserLevelRepositoryAdapter(SpringDataUserLevelRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserLevel> findFirstByOrderByIdDesc() {
        return repository.findFirstByOrderByIdDesc();
    }
}