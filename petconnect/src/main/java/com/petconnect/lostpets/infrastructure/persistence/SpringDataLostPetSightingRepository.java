package com.petconnect.lostpets.infrastructure.persistence;

import com.petconnect.lostpets.domain.LostPetSighting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataLostPetSightingRepository extends JpaRepository<LostPetSighting, UUID> {

    List<LostPetSighting> findByReportIdOrderBySightedAtDesc(UUID reportId);
}
