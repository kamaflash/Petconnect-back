package com.petconnect.lostpets.infrastructure.persistence;

import com.petconnect.lostpets.domain.LostPetReport;
import com.petconnect.lostpets.domain.LostPetReportStatus;
import com.petconnect.pets.domain.Species;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface SpringDataLostPetReportRepository extends JpaRepository<LostPetReport, UUID> {

    /**
     * Búsqueda paginada con todos los filtros en base de datos (nada en memoria).
     * El filtro de distancia usa la fórmula del semiverseno en SQL; los avisos
     * sin coordenadas quedan excluidos solo cuando se filtra por proximidad.
     * La ordenación la impone el {@code Pageable} (siempre {@code createdAt} DESC).
     */
    @Query(value = """
            SELECT r FROM LostPetReport r
            WHERE r.active = true
              AND (:species IS NULL OR r.species = :species)
              AND (:status IS NULL OR r.status = :status)
              AND (CAST(:from AS date) IS NULL OR r.lostDate >= :from)
              AND (CAST(:to AS date) IS NULL OR r.lostDate <= :to)
              AND (
                CAST(:latitude AS double) IS NULL OR CAST(:longitude AS double) IS NULL
                OR (r.latitude IS NOT NULL AND r.longitude IS NOT NULL
                    AND (6371 * acos(least(1.0, greatest(-1.0,
                        cos(radians(:latitude)) * cos(radians(r.latitude))
                        * cos(radians(r.longitude) - radians(:longitude))
                        + sin(radians(:latitude)) * sin(radians(r.latitude)))))) <= :radiusKm)
              )
            """,
            countQuery = """
                    SELECT COUNT(r) FROM LostPetReport r
                    WHERE r.active = true
                      AND (:species IS NULL OR r.species = :species)
                      AND (:status IS NULL OR r.status = :status)
                      AND (CAST(:from AS date) IS NULL OR r.lostDate >= :from)
                      AND (CAST(:to AS date) IS NULL OR r.lostDate <= :to)
                      AND (
                        CAST(:latitude AS double) IS NULL OR CAST(:longitude AS double) IS NULL
                        OR (r.latitude IS NOT NULL AND r.longitude IS NOT NULL
                            AND (6371 * acos(least(1.0, greatest(-1.0,
                                cos(radians(:latitude)) * cos(radians(r.latitude))
                                * cos(radians(r.longitude) - radians(:longitude))
                                + sin(radians(:latitude)) * sin(radians(r.latitude)))))) <= :radiusKm)
                      )
                    """)
    Page<LostPetReport> searchActive(
            @Param("species") Species species,
            @Param("status") LostPetReportStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") double radiusKm,
            Pageable pageable);

    Page<LostPetReport> findByReporterIdAndActiveTrue(UUID reporterId, Pageable pageable);
}

