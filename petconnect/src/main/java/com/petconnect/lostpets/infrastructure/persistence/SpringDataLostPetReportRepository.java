package com.petconnect.lostpets.infrastructure.persistence;

import com.petconnect.lostpets.domain.LostPetReport;
import com.petconnect.lostpets.domain.LostPetReportStatus;
import com.petconnect.pets.domain.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataLostPetReportRepository extends JpaRepository<LostPetReport, UUID> {

    List<LostPetReport> findByActiveTrueOrderByCreatedAtDesc();

    List<LostPetReport> findByReporterIdOrderByCreatedAtDesc(UUID reporterId);

    List<LostPetReport> findByStatusAndActiveTrue(LostPetReportStatus status);

    List<LostPetReport> findBySpeciesAndActiveTrue(Species species);

    List<LostPetReport> findByLostDateBetweenAndActiveTrue(LocalDate from, LocalDate to);
}
