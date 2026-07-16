package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.CommunityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpringDataGroupRepository extends JpaRepository<CommunityGroup, UUID> {
    List<CommunityGroup> findByActiveTrue();
}