package com.petconnect.shelter.infrastructure.persistence;

import com.petconnect.shelter.domain.ShelterVisitRequest;
import com.petconnect.shelter.domain.ShelterVisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;
import java.util.UUID;

public interface ShelterVisitRequestJpaRepository extends JpaRepository<ShelterVisitRequest, UUID> {
    List<ShelterVisitRequest> findByShelterUserIdOrderByVisitAtAsc(UUID shelterUserId);

    List<ShelterVisitRequest> findByShelterUserIdAndVisitorUserIdOrderByVisitAtAsc(
            UUID shelterUserId, UUID visitorUserId);

    boolean existsByShelterUserIdAndVisitorUserIdAndStatusIn(
            UUID shelterUserId, UUID visitorUserId, Collection<ShelterVisitStatus> statuses);
}
