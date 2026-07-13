package com.petconnect.home.infrastructure.persistence;

import com.petconnect.home.domain.VetService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataVetServiceRepository extends JpaRepository<VetService, UUID> {
    List<VetService> findByActiveTrue();
}