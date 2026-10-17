package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.CommunityGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface SpringDataGroupRepository extends JpaRepository<CommunityGroup, UUID> {
    List<CommunityGroup> findByActiveTrue();

    Page<CommunityGroup> findByActiveTrue(Pageable pageable);

    Page<CommunityGroup> findByActiveTrueAndTopic(String topic, Pageable pageable);

    /**
     * Búsqueda por texto. Se usa JPQL explícito en lugar de un nombre derivado
     * porque mezclar {@code And} con {@code Or} en un método generado produce una
     * precedencia ambigua y difícil de leer.
     */
    @Query("""
            SELECT g FROM CommunityGroup g
            WHERE g.active = true
              AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :term, '%'))
                   OR LOWER(g.description) LIKE LOWER(CONCAT('%', :term, '%'))
                   OR LOWER(g.topic) LIKE LOWER(CONCAT('%', :term, '%')))
            """)
    Page<CommunityGroup> searchActive(@Param("term") String term, Pageable pageable);

    /** Búsqueda por texto restringida a una temática concreta. */
    @Query("""
            SELECT g FROM CommunityGroup g
            WHERE g.active = true
              AND g.topic = :topic
              AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :term, '%'))
                   OR LOWER(g.description) LIKE LOWER(CONCAT('%', :term, '%')))
            """)
    Page<CommunityGroup> searchActiveByTopic(@Param("topic") String topic,
            @Param("term") String term, Pageable pageable);

    List<CommunityGroup> findByIdIn(List<UUID> ids);
}