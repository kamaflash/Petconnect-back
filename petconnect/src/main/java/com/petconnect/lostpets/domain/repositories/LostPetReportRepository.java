package com.petconnect.lostpets.domain.repositories;

import com.petconnect.lostpets.domain.LostPetReport;
import com.petconnect.lostpets.domain.LostPetReportStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LostPetReportRepository {

    Optional<LostPetReport> findById(UUID id);

    List<LostPetReport> findByActiveTrueOrderByCreatedAtDesc();

    List<LostPetReport> findByReporterIdOrderByCreatedAtDesc(UUID reporterId);

    List<LostPetReport> findByStatusAndActiveTrue(LostPetReportStatus status);

    List<LostPetReport> findBySpeciesAndActiveTrue(String species);

    List<LostPetReport> findByLostDateBetweenAndActiveTrue(LocalDate from, LocalDate to);

    LostPetReport save(LostPetReport report);

    void delete(LostPetReport report);
}
