package com.petconnect.lostpets.domain.repositories;

import com.petconnect.lostpets.domain.LostPetReport;
import com.petconnect.lostpets.domain.LostPetReportStatus;
import com.petconnect.pets.domain.Species;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface LostPetReportRepository {

    Optional<LostPetReport> findById(UUID id);

    /**
     * Búsqueda con filtros combinados y paginación en base de datos,
     * ordenada por fecha de creación descendente (vía {@code Pageable}).
     * Un radio negativo desactiva el filtro de distancia.
     */
    Page<LostPetReport> searchActive(Species species, LostPetReportStatus status,
            LocalDate from, LocalDate to,
            Double latitude, Double longitude, double radiusKm,
            Pageable pageable);

    Page<LostPetReport> findByReporterIdAndActiveTrue(UUID reporterId, Pageable pageable);

    LostPetReport save(LostPetReport report);

    void delete(LostPetReport report);
}
