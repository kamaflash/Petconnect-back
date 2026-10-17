package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.CommunityGroupMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataCommunityGroupMemberRepository extends JpaRepository<CommunityGroupMember, UUID> {

    Optional<CommunityGroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    List<CommunityGroupMember> findByUserId(UUID userId);

    Page<CommunityGroupMember> findByUserId(UUID userId, Pageable pageable);

    List<CommunityGroupMember> findByGroupId(UUID groupId);

    long countByGroupId(UUID groupId);

    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);
}
