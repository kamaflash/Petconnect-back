package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.Suggestion;
import com.petconnect.home.domain.repositories.SuggestionRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class JpaSuggestionRepositoryAdapter implements SuggestionRepository {

    private final SpringDataSuggestionRepository repository;

    public JpaSuggestionRepositoryAdapter(SpringDataSuggestionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Suggestion> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Suggestion> findTop4ByOrderByCreatedAtDesc() {
        return repository.findTop4ByOrderByCreatedAtDesc();
    }
}
