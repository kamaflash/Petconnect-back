package com.petconnect.adoptions.infrastructure.persistence;

import com.petconnect.adoptions.domain.AdoptionRequestEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAdoptionRequestEventRepository extends JpaRepository<AdoptionRequestEvent, UUID> {
    List<AdoptionRequestEvent> findByRequestIdOrderByCreatedAtAsc(UUID requestId);
}
