package com.petconnect.home.domain.repositories;

import com.petconnect.home.domain.Suggestion;
import java.util.List;

public interface SuggestionRepository {
    List<Suggestion> findAll();

    List<Suggestion> findTop4ByOrderByCreatedAtDesc();
}