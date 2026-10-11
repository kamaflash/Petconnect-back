package com.petconnect.lostpets.infrastructure.persistence;

import com.petconnect.lostpets.domain.LostPetReport;
import com.petconnect.lostpets.domain.LostPetReportStatus;
import com.petconnect.lostpets.domain.repositories.LostPetReportRepository;
import com.petconnect.lostpets.application.LostPetService;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
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
    public List<LostPetReport> findByActiveTrueOrderByCreatedAtDesc() {
        return repository.findByActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public List<LostPetReport> findByReporterIdOrderByCreatedAtDesc(UUID reporterId) {
        return repository.findByReporterIdOrderByCreatedAtDesc(reporterId);
    }

    @Override
    public List<LostPetReport> findByStatusAndActiveTrue(LostPetReportStatus status) {
        return repository.findByStatusAndActiveTrue(status);
    }

    @Override
    public List<LostPetReport> findBySpeciesAndActiveTrue(String species) {
        return LostPetService.parseSpecies(species)
                .map(repository::findBySpeciesAndActiveTrue)
                .orElseGet(List::of);
    }

    @Override
    public List<LostPetReport> findByLostDateBetweenAndActiveTrue(LocalDate from, LocalDate to) {
        return repository.findByLostDateBetweenAndActiveTrue(from, to);
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
