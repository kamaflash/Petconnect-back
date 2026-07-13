package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataSuggestionRepository extends JpaRepository<Suggestion, UUID> {
    List<Suggestion> findTop4ByOrderByCreatedAtDesc();
}
