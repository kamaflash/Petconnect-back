package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpringDataEventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByActiveTrueOrderByEventDateAsc();
}