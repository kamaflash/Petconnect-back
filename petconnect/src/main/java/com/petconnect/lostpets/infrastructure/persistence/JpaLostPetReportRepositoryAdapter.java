package com.petconnect.lostpets.infrastructure.persistence;

import com.petconnect.lostpets.domain.LostPetReport;
import com.petconnect.lostpets.domain.LostPetReportStatus;
import com.petconnect.lostpets.domain.repositories.LostPetReportRepository;
import com.petconnect.pets.domain.Species;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaLostPetReportRepositoryAdapter implements LostPetReportRepository {

    private final SpringDataLostPetReportRepository repository;

    public JpaLostPetReportRepositoryAdapter(SpringDataLostPetReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<LostPetReport> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Page<LostPetReport> searchActive(Species species, LostPetReportStatus status,
            LocalDate from, LocalDate to,
            Double latitude, Double longitude, double radiusKm,
            Pageable pageable) {
        return repository.searchActive(species, status, from, to,
                latitude, longitude, radiusKm, pageable);
    }

    @Override
    public Page<LostPetReport> findByReporterIdAndActiveTrue(UUID reporterId, Pageable pageable) {
        return repository.findByReporterIdAndActiveTrue(reporterId, pageable);
    }

    @Override
    public LostPetReport save(LostPetReport report) {
        return repository.save(report);
    }

    @Override
    public void delete(LostPetReport report) {
        repository.delete(report);
    }
}
